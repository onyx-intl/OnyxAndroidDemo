package com.onyx.android.eink.pen.demo.erase

import android.graphics.Color
import android.graphics.RectF
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.erase.bean.EraseArgs
import com.onyx.android.eink.pen.demo.erase.bean.EraseBean
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.eink.pen.demo.erase.util.EraseRedrawUtils
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper
import com.onyx.android.eink.pen.demo.erase.util.ShapeSplitter
import com.onyx.android.eink.pen.demo.event.PenEvent
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.eink.pen.demo.util.ShapeUtils
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.rx.ObservableHolder
import com.onyx.android.sdk.utils.RectUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

class EraseController(
    private val penBundle: PenBundle,
    private val penManager: PenManager,
    private val lifecycleCallbacks: EraseLifecycleCallbacks
) {
    fun begin(
        point: TouchPoint,
        temporaryErase: Boolean,
        onPenParamsReady: Runnable?
    ): EraseContext {
        val eraseType = penBundle.getCurrentEraseType()
        if (eraseType == EraseTypes.ERASER_AREA) {
            return beginAreaErase(point, temporaryErase)
        }
        return beginMoveStrokeErase(point, onPenParamsReady)
    }

    private fun beginAreaErase(point: TouchPoint, temporaryErase: Boolean): EraseContext {
        submit(PenTask { pm: PenManager? -> applyEraseBegin() }, null)
        if (!temporaryErase) {
            submit(PenTask { pm: PenManager? ->
                val manager = pm ?: return@PenTask
                manager.applyErasePenParams()
            }, null)
        }
        val context = EraseContext()
        context.addErasePoint(point)
        return context
    }

    private fun beginMoveStrokeErase(
        point: TouchPoint,
        onPenParamsReady: Runnable?
    ): EraseContext {
        val context = EraseContext()
        context.addErasePoint(point)
        submit(PenTask { pm: PenManager? -> applyEraseBegin() }, onPenParamsReady)
        return context
    }

    fun openMoveEraseBuffer(eraseContext: EraseContext): ObservableHolder<TouchPoint>? {
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            return null
        }
        val holder = ObservableHolder<TouchPoint>()
        holder.setDisposable(
            holder.getObservable().buffer(MOVE_BUFFER_MS, TimeUnit.MILLISECONDS)
                .subscribe(Consumer { touchPoints: MutableList<TouchPoint>? ->
                    if (eraseContext.isFinishing()) {
                        return@Consumer
                    }
                    val points = touchPoints ?: return@Consumer
                    val pointList = TouchPointList()
                    for (touchPoint in points) {
                        pointList.add(TouchPoint(touchPoint))
                    }
                    onErasing(pointList, eraseContext)
                })
        )
        return holder
    }

    fun onErasing(pointList: TouchPointList, eraseContext: EraseContext?) {
        if (eraseContext == null || eraseContext.isFinishing()) {
            return
        }
        val eraseArgs = createEraseArgs(pointList, eraseContext)
        val eraseType = penBundle.getCurrentEraseType()
        when (eraseType) {
            EraseTypes.ERASER_MOVE -> {
                submit(PenTask { pm: PenManager? ->
                    performOverlayErasing(
                        eraseContext, eraseArgs
                    )
                }, null)
                return
            }

            EraseTypes.ERASER_AREA -> {
                submit(PenTask { pm: PenManager? -> beginAreaErasePreview(eraseArgs) }, null)
                return
            }

            else -> submit(PenTask { pm: PenManager? -> performStrokeErasing(eraseArgs) }, null)
        }
    }

    fun finish(eraseContext: EraseContext?, onFinished: Runnable?) {
        val eraseType = penBundle.getCurrentEraseType()
        eraseContext?.setFinishing(true)
        penManager.createObservable().map<PenManager?>(Function { pm: PenManager? ->
            performFinish(eraseType, eraseContext)
            pm
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer { _: PenManager? ->
            lifecycleCallbacks.resumePenAfterErase()
            onFinished?.run()
        }, Consumer { error: Throwable? -> error?.printStackTrace() })
    }

    @Throws(Exception::class)
    private fun performFinish(eraseType: Int, eraseContext: EraseContext?) {
        when (eraseType) {
            EraseTypes.ERASER_MOVE -> performOverlayFinish(eraseContext)
            EraseTypes.ERASER_AREA -> performAreaFinish(eraseContext)
            else -> performStrokeFinish()
        }
    }

    private fun applyEraseBegin() {
        val eraseType = penBundle.getCurrentEraseType()
        if (EraseTypes.isMoveOrStrokeErase(eraseType) && EraserTrackHelper.useAppTrack(
                penBundle, eraseType
            )
        ) {
            penManager.prepareAppTrackEraseBegin()
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            penManager.applyAreaErasePreviewParams()
        } else if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            penManager.applyStrokeMoveErasePreviewParams()
        }
        if (shouldResumeRawDrawing(eraseType)) {
            penManager.getEventBus().post(PenEvent.resumeRawDrawingImmediately())
        }
    }

    private fun shouldResumeRawDrawing(eraseType: Int): Boolean {
        if (!penBundle.isEraseTool()) {
            return false
        }
        return eraseType == EraseTypes.ERASER_AREA || EraserTrackHelper.useSfTrack(
            penBundle, eraseType
        )
    }

    private fun beginAreaErasePreview(eraseArgs: EraseArgs) {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE)
        penManager.getRenderContext().eraseArgs = eraseArgs
    }

    private fun performOverlayErasing(eraseContext: EraseContext, eraseArgs: EraseArgs) {
        if (eraseContext.isFinishing()) {
            return
        }
        val eraseType = penBundle.getCurrentEraseType()
        if (EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            splitShapesForOverlayErase(eraseContext, eraseArgs)
            return
        }
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE)
        penManager.getRenderContext().eraseArgs = eraseArgs
        splitShapesForOverlayErase(eraseContext, eraseArgs)
        penManager.renderToScreen()
    }

    private fun splitShapesForOverlayErase(eraseContext: EraseContext, eraseArgs: EraseArgs) {
        val trackPoints = eraseArgs.eraseTrackPoints
        if (trackPoints == null || trackPoints.isEmpty()) {
            return
        }
        val eraseRect = ShapeUtils.getBoundingRect(trackPoints)
        if (eraseRect == null) {
            return
        }
        RectUtils.expand(eraseRect, eraseArgs.drawRadius)

        val drawShape = penManager.getDrawShape()
        val candidates: MutableList<Shape> = ArrayList<Shape>(drawShape)
        val removed: MutableList<Shape> = ArrayList()
        val segments: MutableList<Shape> = ArrayList()

        for (shape in candidates) {
            val bounds = shape.getBoundingRect() ?: continue
            val shapeRect = RectF(bounds)
            RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f)
            if (!RectUtils.intersects(eraseRect, shapeRect)) {
                continue
            }
            val hitPoints: MutableList<TouchPoint> = ArrayList()
            for (erasePoint in trackPoints.getPoints()) {
                if (removed.contains(shape)) {
                    break
                }
                if (!shape.fastHitTest(erasePoint.x, erasePoint.y, eraseArgs.drawRadius)) {
                    continue
                }
                if (!shape.hitTest(erasePoint.x, erasePoint.y, eraseArgs.drawRadius)) {
                    continue
                }
                hitPoints.add(erasePoint)
            }
            if (hitPoints.isEmpty()) {
                continue
            }
            val eraseBean =
                EraseBean().setErasePoints(hitPoints).setEraseRadius(eraseArgs.drawRadius)
            val result = ShapeSplitter.split(shape, eraseBean)
            if (result.getSplitShapes().isNotEmpty()) {
                markRemoved(drawShape, removed, shape)
                segments.addAll(result.getSplitShapes().filterNotNull())
            } else if (result.isShapeErased()) {
                markRemoved(drawShape, removed, shape)
            }
        }

        drawShape.removeAll(removed.toSet())
        drawShape.addAll(segments)
        eraseContext.addSplitShapes(ArrayList(removed))
        eraseContext.unionEraseRect(eraseRect)
    }

    private fun performStrokeErasing(eraseArgs: EraseArgs) {
        val eraseType = penBundle.getCurrentEraseType()
        val removedShapeList: MutableList<Shape> = ArrayList<Shape>()
        removeShapesByTouchPointList(
            eraseArgs.eraseTrackPoints, eraseArgs.drawRadius, removedShapeList
        )
        if (EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            return
        }
        if (EraserTrackHelper.useAppTrack(penBundle, eraseType)) {
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE)
            penManager.getRenderContext().eraseArgs = eraseArgs
            penManager.renderToScreen()
            return
        }
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE_ERASE)
        penManager.renderToBitmap(removedShapeList)
        penManager.renderToScreen()
    }

    private fun removeShapesByTouchPointList(
        touchPointList: TouchPointList?,
        radius: Float,
        removedShapeList: MutableList<Shape>
    ) {
        if (touchPointList == null) {
            return
        }
        val shapeList = penManager.getDrawShape()
        val shapeSize = shapeList.size
        val eraseRect = ShapeUtils.getBoundingRect(touchPointList) ?: return
        RectUtils.expand(eraseRect, radius)

        val hitShapes = ArrayList<Shape>()
        for (i in shapeSize - 1 downTo 0) {
            val shape = shapeList.get(i)
            if (shape.getBoundingRect() == null) {
                continue
            }
            val shapeRect = RectF(shape.getBoundingRect())
            RectUtils.expand(shapeRect, shape.getRenderStrokeWidth() / 2f)
            if (RectUtils.intersects(eraseRect, shapeRect)) {
                hitShapes.add(shape)
            }
        }
        for (shape in hitShapes) {
            if (hitTestAndRemoveShape(shape, touchPointList, radius)) {
                removedShapeList.add(shape)
                shapeList.remove(shape)
            }
        }
    }

    private fun hitTestAndRemoveShape(
        shape: Shape,
        touchPointList: TouchPointList,
        radius: Float
    ): Boolean {
        if (shape.hitTestPoints(touchPointList, radius)) {
            shape.setTransparent(true)
            return true
        }
        return false
    }

    @Throws(Exception::class)
    private fun performOverlayFinish(eraseContext: EraseContext?) {
        val wholeTrack = if (eraseContext != null) eraseContext.getWholeEraseTrackPoints() else null
        val eraseWidth = penBundle.getEraseWidth(EraseTypes.ERASER_MOVE)
        EraseRedrawUtils.finishEraseAndRefresh(
            penManager, eraseContext, wholeTrack, eraseWidth, EraseTypes.ERASER_MOVE
        )
    }

    @Throws(Exception::class)
    private fun performAreaFinish(eraseContext: EraseContext?) {
        if (eraseContext == null) {
            EraseRedrawUtils.finishEraseAndRefresh(
                penManager, null, null, 0f, EraseTypes.ERASER_AREA
            )
            return
        }
        val wholeTrack = eraseContext.getWholeEraseTrackPoints()
        if (wholeTrack.size() < 2) {
            val pad = penBundle.getEraseWidth(EraseTypes.ERASER_AREA)
            EraseRedrawUtils.finishEraseAndRefresh(
                penManager, eraseContext, wholeTrack, pad, EraseTypes.ERASER_AREA
            )
            return
        }
        val areaShape = ShapeUtils.createAreaEraseShape(wholeTrack)
        val areaRect = if (areaShape.getBoundingRect() != null) RectF(areaShape.getBoundingRect())
        else null
        val shapeList = penManager.getDrawShape()
        val removed: MutableList<Shape> = ArrayList()
        val segments: MutableList<Shape> = ArrayList()

        for (shape in ArrayList(shapeList)) {
            val shapeBounds = shape.getBoundingRect()
            val areaBounds = areaShape.getBoundingRect()
            if (shapeBounds == null || areaBounds == null) {
                continue
            }
            if (!RectF.intersects(areaBounds, shapeBounds)) {
                continue
            }
            val eraseBean = EraseBean().setEraseShape(areaShape)
            val result = ShapeSplitter.split(shape, eraseBean)
            if (result.getSplitShapes().isNotEmpty()) {
                markRemoved(shapeList, removed, shape)
                segments.addAll(result.getSplitShapes().filterNotNull())
            } else if (result.isShapeErased()) {
                markRemoved(shapeList, removed, shape)
            }
        }
        shapeList.removeAll(removed.toSet())
        shapeList.addAll(segments)
        areaShape.recycle()
        if (areaRect != null) {
            eraseContext.unionEraseRect(areaRect)
        }
        eraseContext.addSplitShapes(ArrayList(removed))
        val pad = penBundle.getEraseWidth(EraseTypes.ERASER_AREA)
        EraseRedrawUtils.finishEraseAndRefresh(
            penManager, eraseContext, wholeTrack, pad, EraseTypes.ERASER_AREA
        )
    }

    private fun performStrokeFinish() {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE)
        penManager.getRenderContext().eraseArgs = null
        penManager.setErasePathDrawing(false, EraseTypes.ERASER_STROKE)
        penManager.getRenderContext().bitmap?.eraseColor(Color.WHITE)
        penManager.renderToBitmap(penManager.getDrawShape())
    }

    private fun markRemoved(
        drawShape: MutableList<Shape>,
        removed: MutableList<Shape>,
        shape: Shape
    ) {
        if (!removed.contains(shape)) {
            removed.add(shape)
        }
        drawShape.remove(shape)
    }

    private fun createEraseArgs(
        pointList: TouchPointList,
        eraseContext: EraseContext?
    ): EraseArgs {
        val eraseType = penBundle.getCurrentEraseType()
        val eraseWidth = penBundle.getEraseWidth(eraseType)
        val drawRadius = eraseWidth / 2f
        val showCircle = EraserTrackHelper.useAppTrack(penBundle, eraseType)
        val wholeTrack = if (eraseContext != null) eraseContext.getWholeEraseTrackPoints()
        else pointList
        return EraseArgs().setEraserWidth(eraseWidth).setEraseTrackPoints(pointList)
            .setWholeEraseTrackPoints(wholeTrack).setDrawRadius(drawRadius)
            .setShowEraseCircle(showCircle).setShowEraseLine(false)
    }

    private fun submit(task: PenTask, onComplete: Runnable?) {
        penManager.createObservable().map<PenManager?>(Function { pm: PenManager? ->
            task.run(pm)
            pm
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer { _: PenManager? ->
            onComplete?.run()
        }, Consumer { error: Throwable? -> error?.printStackTrace() })
    }

    private fun interface PenTask {
        @Throws(Exception::class)
        fun run(penManager: PenManager?)
    }

    companion object {
        private const val MOVE_BUFFER_MS: Long = 50
    }
}
