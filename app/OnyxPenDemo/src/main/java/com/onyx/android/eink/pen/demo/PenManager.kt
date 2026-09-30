package com.onyx.android.eink.pen.demo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.SurfaceView
import android.view.View
import androidx.annotation.WorkerThread
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.PenInfoUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.data.PenConstant
import com.onyx.android.sdk.device.Device
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.style.StrokeStyle
import com.onyx.android.sdk.utils.BitmapUtils
import com.onyx.android.sdk.utils.Debug
import com.onyx.android.sdk.utils.ResManager
import com.onyx.android.sdk.utils.ViewUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class PenManager(private val eventBus: EventBus) {
    private val penDispatcher = Dispatchers.IO.limitedParallelism(1, "PenManager")
    private val penScope = CoroutineScope(SupervisorJob() + penDispatcher)
    private var rendererHelper: RendererHelper? = null
    private var surfaceView: SurfaceView? = null
    private var touchHelper: TouchHelper? = null

    fun getEventBus(): EventBus = eventBus

    fun getRendererHelper(): RendererHelper {
        val existing = rendererHelper
        if (existing != null) {
            return existing
        }
        return RendererHelper().also { rendererHelper = it }
    }

    suspend fun <T> withPen(block: suspend PenManager.() -> T): T =
        withContext(penDispatcher) { block() }

    suspend fun <T> withPenCatching(
        onFailure: (Throwable) -> Unit = { it.printStackTrace() },
        block: suspend PenManager.() -> T,
    ): Result<T> {
        return try {
            Result.success(withPen(block))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            onFailure(e)
            Result.failure(e)
        }
    }

    suspend fun <T> withPenPaused(
        pauseRender: Boolean = true,
        pauseInput: Boolean = true,
        renderToScreen: Boolean = true,
        block: suspend PenManager.() -> T,
    ): T = withPen {
        applyPauseFlags(pauseRender, pauseInput)
        val result = block()
        if (renderToScreen) {
            renderToScreen()
        }
        result
    }

    /**
     * Fire-and-forget on the pen thread (same role as former createObservable().subscribe).
     * [onSuccess] runs on Main after the pen block completes successfully.
     */
    fun launchPen(
        onFailure: (Throwable) -> Unit = { it.printStackTrace() },
        onSuccess: (() -> Unit)? = null,
        block: suspend PenManager.() -> Unit,
    ): Job = penScope.launch {
        withPenCatching(onFailure, block).onSuccess {
            if (onSuccess != null) {
                withContext(Dispatchers.Main.immediate) { onSuccess() }
            }
        }
    }

    @WorkerThread
    private fun applyPauseFlags(pauseRender: Boolean, pauseInput: Boolean) {
        if (pauseRender && pauseInput) {
            setRawDrawingEnabled(false)
            return
        }
        if (pauseRender) {
            setRawDrawingRenderEnabled(false)
        }
        if (pauseInput) {
            setRawInputReaderEnable(false)
        }
    }

    fun getSurfaceView(): SurfaceView? = surfaceView

    fun setSurfaceView(surfaceView: SurfaceView?) {
        this.surfaceView = surfaceView
    }

    fun getTouchHelper(): TouchHelper? = touchHelper
    private var hostSurfaceAttached = false
    private var rawSessionNeedsRestart = false

    @get:WorkerThread
    private var currentMode: InteractiveMode = InteractiveMode.SCRIBBLE

    @WorkerThread
    fun getCurrentMode(): InteractiveMode = currentMode

    private val drawShapes: MutableList<Shape> = ArrayList()

    fun getDrawShape(): MutableList<Shape> = drawShapes

    fun destroy() {
        getDrawShape().clear()
        getRenderContext().eraseArgs = null
        getRenderContext().recycleBitmap()
        touchHelper?.closeRawDrawing()
        touchHelper = null
        surfaceView = null
        hostSurfaceAttached = false
        rawSessionNeedsRestart = false
        currentMode = InteractiveMode.SCRIBBLE
        penScope.coroutineContext[Job]?.cancelChildren()
    }

    @WorkerThread
    fun clearDrawShapes() {
        getDrawShape().clear()
        getRenderContext().eraseArgs = null
        val clearBitmap = getRenderContext().bitmap
        if (clearBitmap != null) {
            activeRenderMode(InteractiveMode.SCRIBBLE)
            clearBitmap.eraseColor(Color.WHITE)
        }
    }

    @WorkerThread
    fun releaseRawSession() {
        rawSessionNeedsRestart = true
        hostSurfaceAttached = false
        touchHelper?.closeRawDrawing()
    }

    fun needsRawSessionRestart(): Boolean {
        return rawSessionNeedsRestart
    }

    fun attachHostView(
        view: SurfaceView,
        floatMenuLayout: View,
        hostViewFocused: Boolean,
        callback: RawInputCallback?,
    ) {
        check(!(view.width == 0 || view.height == 0)) { "can not start when view width or height is 0" }
        val preserveBitmap = surfaceView != null && surfaceView === view && BitmapUtils.isValid(
            getRenderContext().bitmap
        )
        if (hostSurfaceAttached && preserveBitmap && isHostSurfaceValid(view)) {
            Debug.i(javaClass, "not attach for note view not changed")
            return
        }
        surfaceView = view
        if (!preserveBitmap) {
            getRenderContext().bitmap = createBitmap()
            bindCanvasToBitmap()
        }
        var helper = touchHelper
        if (helper == null) {
            helper = TouchHelper.create(view, callback)
            touchHelper = helper
            helper.setPostInputEvent(true)
        } else {
            helper.bindHostView(view, callback)
        }
        val limitRect = ViewUtils.localVisibleRect(view)
        val funcMenuExcludeRect = ViewUtils.relativelyParentRect(floatMenuLayout)
        val excludeRectList: MutableList<Rect?> = ArrayList()
        excludeRectList.add(funcMenuExcludeRect)
        helper.setLimitRect(limitRect, excludeRectList)

        helper.openRawDrawing()
        hostSurfaceAttached = true
        rawSessionNeedsRestart = false
        if (hostViewFocused) {
            helper.forceSetRawDrawingEnabled(false)
        }
        if (!preserveBitmap) {
            restoreDrawShapesToBitmap()
        }
    }

    fun setViewPoint(renderView: View) {
        val rect = ViewUtils.globalVisibleRect(renderView)
        getRenderContext().viewPoint = Point(rect.left, rect.top)
    }

    fun getViewRect(): Rect {
        val rect = Rect()
        val view = surfaceView ?: return rect
        view.getLocalVisibleRect(rect)
        return rect
    }

    fun createBitmap(): Bitmap? {
        val surface = surfaceView ?: return null
        val limitRect = Rect()
        surface.getLocalVisibleRect(limitRect)
        val bitmap =
            Bitmap.createBitmap(limitRect.width(), limitRect.height(), Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        return bitmap
    }

    fun getCanvas(): Canvas? {
        val canvas = getRenderContext().canvas
        if (canvas == null) {
            bindCanvasToBitmap()
            return getRenderContext().canvas
        }
        return canvas
    }

    private fun bindCanvasToBitmap() {
        val bitmap = getRenderContext().bitmap
        if (bitmap == null) {
            getRenderContext().canvas = null
            return
        }
        getRenderContext().canvas = Canvas(bitmap)
    }

    @WorkerThread
    fun restoreDrawShapesToBitmap() {
        if (getDrawShape().isEmpty() || getRenderContext().bitmap == null) {
            return
        }
        activeRenderMode(InteractiveMode.SCRIBBLE)
        renderToBitmap(ArrayList(getDrawShape()))
    }

    @WorkerThread
    fun getLimitNoteRect(): Rect = getViewRect()

    @WorkerThread
    fun setDrawLimitRect(limitRectList: MutableList<Rect>?) {
        val helper = touchHelper ?: return
        helper.setLimitRect(limitRectList)
    }

    @WorkerThread
    fun setDrawExcludeRect(excludeRectList: MutableList<Rect>?) {
        val helper = touchHelper ?: return
        helper.setExcludeRect(excludeRectList)
    }

    @WorkerThread
    fun setStrokeWidth(penWidth: Float) {
        val helper = touchHelper ?: return
        // Convert mm to px for TouchHelper
        val penWidthPx = mmToPx(penWidth)
        helper.setStrokeWidth(penWidthPx)
    }

    private fun mmToPx(mm: Float): Float {
        return mm * ResManager.getAppContext().resources.displayMetrics.densityDpi / MM_OF_ONE_INCH
    }

    @WorkerThread
    fun setStrokeStyle(style: Int) {
        val helper = touchHelper ?: return
        helper.setStrokeStyle(style)
    }

    @WorkerThread
    fun setStrokeColor(color: Int) {
        val helper = touchHelper ?: return
        helper.setStrokeColor(color)
    }

    @WorkerThread
    fun setPenUpRefreshTimeMs(time: Int) {
        val helper = touchHelper ?: return
        helper.setPenUpRefreshTimeMs(time)
    }

    @WorkerThread
    fun setRawDrawingEnabled(enable: Boolean) {
        val helper = touchHelper ?: return
        helper.setRawDrawingEnabled(enable)
        Log.e("zzzzwb", "setRawDrawingEnabled:  enable = $enable")
    }

    @WorkerThread
    fun setEraserRawDrawingEnabled(enabled: Boolean, eraserStrokeStyle: Int) {
        val helper = touchHelper ?: return
        helper.setEraserRawDrawingEnabled(enabled, eraserStrokeStyle)
    }

    @WorkerThread
    fun setErasePathDrawing(drawing: Boolean, eraseType: Int) {
        setEraserRawDrawingEnabled(drawing, EraserTrackHelper.eraserStrokeStyle(eraseType))
    }

    @WorkerThread
    fun prepareAppTrackEraseBegin() {
        if (!isRawDrawingRenderEnabled()) {
            return
        }
        activeRenderMode(InteractiveMode.SCRIBBLE)
        getRenderContext().eraseArgs = null
        renderToScreen()
    }

    @WorkerThread
    fun applyAreaErasePreviewParams() {
        val penBundle: PenBundle = PenBundle.getInstance()
        if (EraserTrackHelper.useSfTrack(penBundle, EraseTypes.ERASER_AREA)) {
            applySfAreaTouchHelperParams()
            warmDashDeviceParameters(penBundle)
            applyCapEraseStrokeConfig(penBundle)
            forceRawDrawingEnabled()
        } else {
            applyLegacyEraseTouchHelperParams()
            warmDashDeviceParameters(penBundle)
            setRawInputReaderEnable(true)
        }
    }

    @WorkerThread
    fun applyStrokeMoveErasePreviewParams() {
        val penBundle: PenBundle = PenBundle.getInstance()
        applyEraseTouchHelperParams(penBundle)
        warmDashDeviceParameters(penBundle)
        applyCapEraseStrokeConfig(penBundle)
        if (EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType())) {
            forceRawDrawingEnabled()
        }
    }

    @WorkerThread
    fun applyErasePenParams() {
        activeRenderMode(InteractiveMode.SCRIBBLE)
        val penBundle: PenBundle = PenBundle.getInstance()
        if (!penBundle.isEraseTool()) {
            applyBrushTouchHelperParams(penBundle)
            warmDashDeviceParameters(penBundle)
            applyCapEraseStrokeConfig(penBundle)
            return
        }
        applyEraseTouchHelperParams(penBundle)
        warmDashDeviceParameters(penBundle)
        applyEraseResumeAttrs(penBundle)
        if (EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType())) {
            forceRawDrawingEnabled()
        }
    }

    @WorkerThread
    fun applyCurrentPenState() {
        val helper = touchHelper ?: return
        if (!helper.isRawDrawingCreated) {
            helper.restartRawDrawing()
        }
        setRawDrawingEnabled(true)
        applyErasePenParams()
        setRawInputReaderEnable(true)
        val penBundle: PenBundle = PenBundle.getInstance()
        if (!penBundle.isEraseTool() || EraserTrackHelper.shouldForceRawDrawing(
                penBundle, penBundle.getCurrentEraseType()
            )
        ) {
            forceRawDrawingEnabled()
        }
    }

    /**
     * Tool brush/erase switch: keep shape bitmap, apply current pen attrs, then blit to screen.
     */
    @WorkerThread
    fun applyToolSwitchWithRefresh() {
        setRawDrawingRenderEnabled(false)
        getRenderContext().eraseArgs = null
        activeRenderMode(InteractiveMode.SCRIBBLE)
        redrawAllShapesToBitmap()
        applyErasePenParams()
        renderToScreen()
    }

    @WorkerThread
    fun redrawAllShapesToBitmap() {
        val redrawBitmap = getRenderContext().bitmap ?: return
        activeRenderMode(InteractiveMode.SCRIBBLE)
        redrawBitmap.eraseColor(Color.WHITE)
        if (!getDrawShape().isEmpty()) {
            renderToBitmap(ArrayList(getDrawShape()))
        }
    }

    @WorkerThread
    @Throws(Exception::class)
    fun refreshPartial(refreshRect: RectF) {
        val context = getRenderContext()
        val bitmap = context.bitmap
        if (bitmap != null && shouldUseFullRefresh(
                refreshRect, bitmap.width, bitmap.height
            )
        ) {
            context.clipRect = null
            activeRenderMode(InteractiveMode.SCRIBBLE)
            renderToScreen()
            return
        }
        try {
            EpdController.setViewDefaultUpdateMode(
                this.surfaceView, UpdateMode.HAND_WRITING_REPAINT_MODE
            )
            context.clipRect = refreshRect
            activeRenderMode(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH)
            renderToScreen()
        } finally {
            context.clipRect = null
            EpdController.resetViewUpdateMode(this.surfaceView)
        }
    }

    private fun applySfAreaTouchHelperParams() {
        setBrushRawDrawingEnabled(true)
        setRawDrawingEnabled(true)
        setStrokeStyle(TouchHelper.STROKE_STYLE_DASH)
        setStrokeWidthPx(PenConstant.DASH_STROKE_WIDTH)
        setStrokeColor(Color.BLACK)
        setRawDrawingRenderEnabled(true)
    }

    private fun applyEraseTouchHelperParams(penBundle: PenBundle) {
        val eraseType = penBundle.getCurrentEraseType()
        if (!EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            applyLegacyEraseTouchHelperParams()
            return
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            applySfAreaTouchHelperParams()
        } else {
            applySfMoveStrokeTouchHelperParams(penBundle)
        }
    }

    private fun applySfMoveStrokeTouchHelperParams(penBundle: PenBundle) {
        val eraseType = penBundle.getCurrentEraseType()
        val eraseWidth = penBundle.getEraseWidth(eraseType)
        setBrushRawDrawingEnabled(true)
        setRawDrawingEnabled(true)
        setStrokeStyle(StrokeStyle.SOFT_ERASER)
        setStrokeWidthPx(eraseWidth)
        setStrokeColor(Color.BLACK)
        setRawDrawingRenderEnabled(true)
        setRawInputReaderEnable(true)
        setEraserRawDrawingEnabled(true, StrokeStyle.SOFT_ERASER)
    }

    private fun applyLegacyEraseTouchHelperParams() {
        setEraserRawDrawingEnabled(false, StrokeStyle.SOFT_ERASER)
        setBrushRawDrawingEnabled(false)
        setRawDrawingRenderEnabled(false)
        setRawInputReaderEnable(true)
    }

    private fun applyBrushTouchHelperParams(penBundle: PenBundle) {
        setBrushRawDrawingEnabled(true)
        val shapeType = penBundle.getCurrentShapeType()
        val strokeStyle = ShapeFactory.getStrokeStyle(shapeType, penBundle.getCurrentTexture())
        setStrokeStyle(strokeStyle)
        setStrokeWidth(penBundle.getCurrentStrokeWidth())
        setStrokeColor(penBundle.getCurrentStrokeColor())
        applyStrokeParameters(shapeType, strokeStyle)
        setRawDrawingRenderEnabled(true)
        setRawInputReaderEnable(true)
    }

    fun applyStrokeParameters(shapeType: Int, strokeStyle: Int) {
        val merged = PenInfoUtils.mergeStrokeParameters(
            shapeType, Device.currentDevice().getStrokeParameters(strokeStyle)
        )
        if (merged == null) {
            return
        }
        Device.currentDevice().setStrokeParameters(strokeStyle, merged)
    }

    private fun applyCapEraseStrokeConfig(penBundle: PenBundle) {
        val eraseType =
            penBundle.getCurrentEraseType() // While brush is selected, SF track still arms side-button Soft Eraser.
        setEraserRawDrawingEnabled(
            EraserTrackHelper.useSfTrack(penBundle, eraseType),
            EraserTrackHelper.eraserStrokeStyle(eraseType)
        )
    }

    private fun applyEraseResumeAttrs(penBundle: PenBundle) {
        val eraseType = penBundle.getCurrentEraseType()
        val sfTrack = EraserTrackHelper.useSfTrack(penBundle, eraseType)
        setErasePathDrawing(sfTrack, eraseType)
        if (!sfTrack) {
            setBrushRawDrawingEnabled(false)
        }
    }

    @WorkerThread
    fun setBrushRawDrawingEnabled(enabled: Boolean) {
        val helper = touchHelper ?: return
        helper.setBrushRawDrawingEnabled(enabled)
    }

    private fun warmDashDeviceParameters(penBundle: PenBundle) {
        Device.currentDevice().setStrokeParameters(
            TouchHelper.STROKE_STYLE_DASH, floatArrayOf(PenConstant.DASH_STROKE_WIDTH)
        )
        val eraseType = penBundle.getCurrentEraseType()
        val eraseWidth = penBundle.getEraseWidth(
            if (EraseTypes.isMoveOrStrokeErase(eraseType)) eraseType else EraseTypes.ERASER_MOVE
        )
        Device.currentDevice().setStrokeParameters(
            StrokeStyle.SOFT_ERASER,
            floatArrayOf(eraseWidth, ERASE_PEN_OPACITY, ERASE_PEN_BLACK_OPACITY)
        )
    }

    private fun setStrokeWidthPx(strokeWidthPx: Float) {
        val helper = touchHelper ?: return
        helper.setStrokeWidth(strokeWidthPx)
    }

    private fun forceRawDrawingEnabled() {
        val helper = touchHelper ?: return
        helper.forceSetRawDrawingEnabled(true)
    }

    @WorkerThread
    fun setRawInputReaderEnable(enable: Boolean) {
        val helper = touchHelper ?: return
        helper.setRawInputReaderEnable(enable)
        Log.e("zzzzwb", "setRawInputReaderEnable:  enable = $enable")
    }

    @WorkerThread
    fun isRawDrawingInputEnabled(): Boolean {
        return touchHelper?.isRawDrawingInputEnabled == true
    }

    @WorkerThread
    fun isRawDrawingRenderEnabled(): Boolean {
        return touchHelper?.isRawDrawingRenderEnabled == true
    }

    @WorkerThread
    fun setRawDrawingRenderEnabled(enable: Boolean) {
        val helper = touchHelper ?: return
        helper.isRawDrawingRenderEnabled = enable
        Log.e("zzzzwb", "setRawDrawingRenderEnabled:  enable = $enable")
    }

    @WorkerThread
    fun activeRenderMode(mode: InteractiveMode) {
        if (currentMode == mode) {
            return
        }
        getRendererHelper().getRenderer(currentMode)?.onDeactivate(surfaceView)
        currentMode = mode
        getRendererHelper().getRenderer(currentMode)?.onActive(surfaceView)
    }

    @WorkerThread
    fun getRenderContext(): RendererHelper.RenderContext = getRendererHelper().getRenderContext()

    @WorkerThread
    fun renderToScreen() {
        getRendererHelper().renderToScreen(currentMode, this.surfaceView, getRenderContext())
    }

    @WorkerThread
    fun renderToBitmap() {
        getRendererHelper().renderToBitmap(currentMode, this.surfaceView, getRenderContext())
    }

    @WorkerThread
    fun renderToBitmap(shapes: MutableList<Shape>?) {
        getRendererHelper().renderToBitmap(currentMode, shapes)
    }

    companion object {
        private const val PARTIAL_REFRESH_MAX_AREA_RATIO = 0.6f
        private const val ERASE_PEN_OPACITY = 0.5f
        private const val ERASE_PEN_BLACK_OPACITY = 0.1f

        private fun isHostSurfaceValid(view: SurfaceView?): Boolean {
            if (view == null) {
                return false
            }
            try {
                return view.holder.surface.isValid
            } catch (e: Exception) {
                return false
            }
        }

        private const val MM_OF_ONE_INCH = 25.4f

        private fun shouldUseFullRefresh(refreshRect: RectF?, bitmapW: Int, bitmapH: Int): Boolean {
            if (bitmapW <= 0 || bitmapH <= 0) {
                return true
            }
            val clipped = RectF(refreshRect)
            clipped.intersect(0f, 0f, bitmapW.toFloat(), bitmapH.toFloat())
            if (clipped.isEmpty) {
                return true
            }
            val bitmapArea = bitmapW * bitmapH.toFloat()
            return (clipped.width() * clipped.height()) / bitmapArea >= PARTIAL_REFRESH_MAX_AREA_RATIO
        }
    }
}
