package com.onyx.android.eink.pen.demo.erase.input

import android.graphics.RectF
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.erase.EraseController
import com.onyx.android.eink.pen.demo.erase.EraseLifecycleCallbacks
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.data.TouchPointList

/**
 * Routes one raw-pen gesture to draw or erase.
 * Gesture mode is fixed at begin and does not follow mid-gesture tool UI changes.
 */
class DrawEraseInputHandler(
    private val penBundle: PenBundle,
    private val penManager: PenManager,
    lifecycleCallbacks: EraseLifecycleCallbacks,
    private val shapeCommitCallback: ShapeCommitCallback,
    private val eraseFinishedCallback: EraseFinishedCallback?,
) : RawInputCallback() {
    private enum class GestureMode {
        DRAW, ERASE
    }

    interface ShapeCommitCallback {
        fun onCommitShape(touchPointList: TouchPointList?)
    }

    interface EraseFinishedCallback {
        fun onEraseFinished()
    }

    private val eraseController: EraseController =
        EraseController(penBundle, penManager, lifecycleCallbacks)

    private var currentGestureMode = GestureMode.DRAW
    private var eraseContext: EraseContext? = null
    private var pendingPenUpRefreshRect: RectF? = null
    private var drawingStrokeCommitted = false

    /** True when system eraser button / pen tip erase starts while brush tool is selected.  */
    private var shortcutEraseGesture = false
    private var eraseParamsReady = false
    private val pendingEraseMoves: MutableList<TouchPoint?> = ArrayList<TouchPoint?>()

    override fun onBeginRawDrawing(shortcutDrawing: Boolean, touchPoint: TouchPoint) {
        if (penBundle.isEraseTool()) {
            beginEraseGesture(touchPoint)
            return
        }
        beginDrawGesture()
    }

    override fun onEndRawDrawing(outLimitRegion: Boolean, touchPoint: TouchPoint?) {
        if (currentGestureMode == GestureMode.ERASE) {
            endEraseGesture()
        }
    }

    override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {
        if (currentGestureMode == GestureMode.ERASE) {
            onEraseMove(touchPoint)
        }
    }

    override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList?) {
        if (currentGestureMode == GestureMode.ERASE) {
            finishEraseGesture(touchPointList)
            return
        }
        shapeCommitCallback.onCommitShape(touchPointList)
        drawingStrokeCommitted = true
        flushPendingPenUpRefresh()
    }

    override fun onBeginRawErasing(shortcutErasing: Boolean, point: TouchPoint) {
        beginEraseGesture(point)
    }

    override fun onEndRawErasing(outLimitRegion: Boolean, point: TouchPoint?) {
        endEraseGesture()
    }

    override fun onRawErasingTouchPointMoveReceived(point: TouchPoint) {
        onEraseMove(point)
    }

    override fun onRawErasingTouchPointListReceived(pointList: TouchPointList?) {
        finishEraseGesture(pointList)
    }

    override fun onPenUpRefresh(refreshRect: RectF) {
        if (!penBundle.isEnablePenUpRefresh()) {
            return
        }
        if (currentGestureMode != GestureMode.DRAW || shortcutEraseGesture || eraseContext != null) {
            return
        }
        if (drawingStrokeCommitted) {
            performPenUpRefresh(refreshRect)
        } else {
            pendingPenUpRefreshRect = RectF(refreshRect)
        }
    }

    private fun beginDrawGesture() {
        currentGestureMode = GestureMode.DRAW
        shortcutEraseGesture = false
        drawingStrokeCommitted = false
        pendingPenUpRefreshRect = null
    }

    private fun beginEraseGesture(point: TouchPoint) {
        currentGestureMode = GestureMode.ERASE
        val temporaryErase = !penBundle.isEraseTool()
        shortcutEraseGesture = temporaryErase
        removeEraseObserver()
        eraseParamsReady = false
        pendingEraseMoves.clear()
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            eraseContext = eraseController.begin(point, temporaryErase, null)
            eraseParamsReady = true
            return
        }
        eraseContext = eraseController.begin(
            point, temporaryErase
        ) { onEraseParamsReady(point) }
    }

    private fun endEraseGesture() {
        shortcutEraseGesture = false
    }

    private fun onEraseMove(point: TouchPoint) {
        eraseContext?.addErasePoint(point)
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            return
        }
        if (!eraseParamsReady) {
            pendingEraseMoves.add(TouchPoint(point))
            return
        }
        dispatchEraseMove(point)
    }

    private fun finishEraseGesture(pointList: TouchPointList?) {
        eraseContext?.let { context ->
            context.addErasePoints(pointList)
            context.setFinishing(true)
        }
        removeEraseObserver()
        val finishingContext = eraseContext
        eraseController.finish(finishingContext) {
            eraseContext = null
            eraseParamsReady = false
            pendingEraseMoves.clear()
            shortcutEraseGesture = false
            currentGestureMode = GestureMode.DRAW
            eraseFinishedCallback?.onEraseFinished()
        }
    }

    private fun onEraseParamsReady(downPoint: TouchPoint) {
        val context = eraseContext
        if (context == null || context.isFinishing()) {
            return
        }
        eraseParamsReady = true
        if (penBundle.getCurrentEraseType() != EraseTypes.ERASER_AREA) {
            eraseController.openMoveEraseBuffer(context)
            val firstPoints = TouchPointList()
            firstPoints.add(TouchPoint(downPoint))
            eraseController.onErasing(firstPoints, context)
            for (pending in pendingEraseMoves) {
                dispatchEraseMove(pending)
            }
            pendingEraseMoves.clear()
        }
    }

    private fun dispatchEraseMove(point: TouchPoint?) {
        if (point == null) {
            return
        }
        eraseController.offerMoveErasePoint(point)
    }

    private fun flushPendingPenUpRefresh() {
        val refreshRect = pendingPenUpRefreshRect ?: return
        performPenUpRefresh(refreshRect)
        pendingPenUpRefreshRect = null
    }

    private fun performPenUpRefresh(refreshRect: RectF) {
        penManager.launchPen {
            refreshPartial(refreshRect)
        }
    }

    private fun removeEraseObserver() {
        eraseController.closeMoveEraseBuffer()
    }
}
