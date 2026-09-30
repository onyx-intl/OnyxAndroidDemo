package com.onyx.android.eink.pen.demo

import android.graphics.Rect
import android.view.SurfaceView
import android.view.View
import com.onyx.android.eink.pen.demo.data.InteractiveMode
import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.event.PenEvent
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent
import com.onyx.android.eink.pen.demo.shape.Shape
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.utils.EventBusUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Pen-thread commands. Scheduling goes through [PenManager.launchPen] /
 * [PenManager.withPen], matching former createObservable() ownership.
 */
object PenCommands {
    private val bundle: PenBundle
        get() = PenBundle.getInstance()

    private val penManager: PenManager
        get() = bundle.getPenManager()

    fun changeStrokeWidth(shapeType: Int, width: Float) {
        penManager.launchPen {
            withPenPaused {
                setStrokeWidth(width)
            }
            bundle.setCurrentStrokeWidth(width)
            bundle.savePenLineWidth(shapeType, width)
        }
    }

    fun changeStrokeColor(color: Int) {
        penManager.launchPen {
            withPenPaused {
                setStrokeColor(color)
            }
            bundle.setCurrentStrokeColor(color)
        }
    }

    fun changeStrokeStyle(
        shapeType: Int,
        texture: Int,
        onDone: (() -> Unit)? = null,
    ) {
        val width = bundle.getPenLineWidth(shapeType)
        penManager.launchPen(onSuccess = onDone) {
            withPenPaused {
                val strokeStyle = ShapeFactory.getStrokeStyle(shapeType, texture)
                setStrokeStyle(strokeStyle)
                applyStrokeParameters(shapeType, strokeStyle)
                setStrokeWidth(width)
            }
            bundle.setCurrentShapeType(shapeType)
            bundle.setCurrentTexture(texture)
            bundle.setCurrentStrokeWidth(width)
        }
    }

    fun clearNote() {
        penManager.launchPen {
            withPenPaused {
                clearDrawShapes()
            }
            resumeRawDrawingSuspend(
                resumeRender = true,
                resumeInput = true,
                delayResumePenTimeMs = PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS
            )
        }
    }

    fun addShape(shape: Shape) {
        penManager.launchPen {
            activeRenderMode(InteractiveMode.SCRIBBLE)
            getDrawShape().add(shape)
            renderToBitmap(mutableListOf(shape))
        }
    }

    fun pauseRawDrawingRender() {
        penManager.launchPen {
            setRawDrawingRenderEnabled(false)
        }
    }

    fun pauseRawInputReader() {
        penManager.launchPen {
            setRawInputReaderEnable(false)
        }
    }

    fun pauseRawDrawing() {
        penManager.launchPen { // Strong pause: stops both brush writing and SF erase track.
            setRawDrawingEnabled(false)
        }
    }

    fun resumeRawDrawing(
        resumeRender: Boolean,
        resumeInput: Boolean,
        delayResumePenTimeMs: Int,
    ) {
        penManager.launchPen {
            resumeRawDrawingSuspend(resumeRender, resumeInput, delayResumePenTimeMs)
        }
    }

    suspend fun resumeRawDrawingSuspend(
        resumeRender: Boolean,
        resumeInput: Boolean,
        delayResumePenTimeMs: Int,
    ) {
        if (delayResumePenTimeMs > 0) {
            delay(delayResumePenTimeMs.toLong())
        }
        penManager.withPen {
            if (getTouchHelper() == null) {
                return@withPen
            }
            if (!resumeRender && !resumeInput) {
                setRawDrawingRenderEnabled(false)
                return@withPen
            }
            applyErasePenParams()
            setPenUpRefreshTimeMs(bundle.getPenUpRefreshTimeMs())
            setDrawExcludeRect(bundle.getExcludeRectList())
            if (resumeInput) {
                setRawInputReaderEnable(true)
            }
            setRawDrawingRenderEnabled(resumeRender)
        }
    }

    fun refreshScreen(
        pauseRawInputReader: Boolean = true,
        resumeRawDrawing: Boolean = true,
        delayResumePenTimeMs: Int = PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS,
        delayRefreshTimeMs: Int = 0,
    ) {
        penManager.launchPen {
            if (delayRefreshTimeMs > 0) {
                delay(delayRefreshTimeMs.toLong())
            }
            withPenPaused(
                pauseRender = true, pauseInput = pauseRawInputReader, renderToScreen = true
            ) {} // Resume via PenEvent so Activity can gate on status-bar / panel / popup / focus.
            if (resumeRawDrawing) {
                EventBusUtils.safelyPostEvent(
                    penManager.getEventBus(), PenEvent.resumeRawDrawing(delayResumePenTimeMs)
                )
            }
        }
    }

    /**
     * Popup show/dismiss pen handling.
     * Show: notify Activity (pause + gate) then hard-pause + refresh without resume.
     * Dismiss: refresh without resume, then notify Activity to gated-resume.
     */
    fun onPopupVisibilityChanged(show: Boolean) {
        if (show) {
            EventBusUtils.safelyPostEvent(
                penManager.getEventBus(), PopupWindowChangeEvent(true)
            )
        }
        penManager.launchPen {
            setRawDrawingEnabled(false)
            withPenPaused(
                pauseRender = true, pauseInput = true, renderToScreen = true
            ) {}
            if (!show) {
                withContext(Dispatchers.Main.immediate) {
                    EventBusUtils.safelyPostEvent(
                        penManager.getEventBus(), PopupWindowChangeEvent(false)
                    )
                }
            }
        }
    }

    suspend fun attachNoteView(
        hostView: SurfaceView,
        floatMenuLayout: View,
        callback: RawInputCallback?,
    ): Result<Rect> {
        return penManager.withPenCatching {
            setRawDrawingEnabled(false)
            attachHostView(hostView, floatMenuLayout, true, callback)
            setViewPoint(hostView)
            applyCurrentPenState()
            renderToScreen()
            getLimitNoteRect()
        }
    }
}
