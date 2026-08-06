package com.onyx.android.eink.pen.demo

import android.graphics.Color
import android.graphics.Rect
import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.data.ShapeType
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper
import com.onyx.android.eink.pen.demo.util.PenInfoUtils
import com.onyx.android.sdk.data.note.PenTexture
import org.greenrobot.eventbus.EventBus

class PenBundle private constructor() {
    var penLineWidthMap: MutableMap<Int, Float> = HashMap()

    private var penManager: PenManager? = null
    private var eventBus: EventBus? = null

    private var currentShapeType: Int = ShapeFactory.SHAPE_BRUSH_SCRIBBLE
    private var currentStrokeColor: Int = Color.BLACK
    private var currentTexture: Int = PenTexture.CHARCOAL_SHAPE_V1
    private var currentStrokeWidth: Float = 0f

    private var erasing: Boolean = false
    private var currentEraseType = EraseTypes.ERASER_STROKE
    private var lastEraseType: Int = EraseTypes.ERASER_STROKE
    private val eraseWidthMap: MutableMap<Int, Float> = HashMap()
    private val displayEraseTrackMap: MutableMap<Int, Boolean> = HashMap()

    private var displayEraseTrack = false
    private var enablePenUpRefresh = false
    private var penUpRefreshTimeMs: Int = 500

    private var excludeRectList: MutableList<Rect>? = null

    init {
        initDefaultPenLineWidth()
        initDefaultEraseSettings()
        setCurrentStrokeWidth(getPenLineWidth(currentShapeType))
    }

    private fun initDefaultPenLineWidth() {
        for (style in ShapeType.entries) {
            val shapeType = style.getValue()
            penLineWidthMap[shapeType] = PenInfoUtils.getShapeDefaultStrokeWidth(shapeType)
        }
    }

    private fun initDefaultEraseSettings() {
        val eraseTypes = intArrayOf(
            EraseTypes.ERASER_STROKE, EraseTypes.ERASER_MOVE, EraseTypes.ERASER_AREA
        )
        for (eraseType in eraseTypes) {
            eraseWidthMap[eraseType] = EraseUnits.getDefaultEraseWidth(eraseType)
            displayEraseTrackMap[eraseType] = EraserTrackHelper.defaultTrackEnabled(eraseType)
        }
        displayEraseTrack = isDisplayEraseTrack(EraseTypes.ERASER_STROKE)
    }

    fun getPenManager(): PenManager {
        val manager = penManager
        if (manager != null) {
            return manager
        }
        return PenManager(getEventBus()).also { penManager = it }
    }

    fun getEventBus(): EventBus {
        val bus = eventBus
        if (bus != null) {
            return bus
        }
        return EventBus().also { eventBus = it }
    }

    fun getCurrentShapeType(): Int = currentShapeType

    fun setCurrentShapeType(currentShapeType: Int) {
        this.currentShapeType = currentShapeType
    }

    fun getCurrentStrokeWidth(): Float = currentStrokeWidth

    fun setCurrentStrokeWidth(currentStrokeWidth: Float) {
        this.currentStrokeWidth = currentStrokeWidth
    }

    fun getCurrentStrokeColor(): Int = currentStrokeColor

    fun setCurrentStrokeColor(currentStrokeColor: Int) {
        this.currentStrokeColor = currentStrokeColor
    }

    fun getCurrentTexture(): Int = currentTexture

    fun setCurrentTexture(currentTexture: Int) {
        this.currentTexture = currentTexture
    }

    fun savePenLineWidth(shapeType: Int, lineWidth: Float) {
        penLineWidthMap[shapeType] = lineWidth
    }

    fun getPenLineWidth(shapeType: Int): Float {
        return penLineWidthMap[shapeType]
            ?: PenInfoUtils.getShapeDefaultStrokeWidth(shapeType)
    }

    fun isErasing(): Boolean = erasing

    fun setErasing(erasing: Boolean) {
        this.erasing = erasing
    }

    fun resetToolToBrushOnSessionEnd() {
        erasing = false
    }

    fun isEraseTool(): Boolean = isErasing()

    fun getCurrentEraseType(): Int = currentEraseType

    fun setCurrentEraseType(currentEraseType: Int) {
        this.currentEraseType = currentEraseType
        this.lastEraseType = currentEraseType
        displayEraseTrack = isDisplayEraseTrack(currentEraseType)
    }

    fun getLastEraseType(): Int = lastEraseType

    fun selectEraseType(eraseType: Int) {
        setCurrentEraseType(eraseType)
    }

    fun getEraseWidth(eraseType: Int): Float {
        val width = eraseWidthMap[eraseType]
        if (width != null) {
            return width
        }
        val defaultWidth = EraseUnits.getDefaultEraseWidth(eraseType)
        eraseWidthMap[eraseType] = defaultWidth
        return defaultWidth
    }

    fun setEraseWidth(eraseType: Int, eraseWidth: Float) {
        eraseWidthMap[eraseType] = EraseUnits.clampEraseWidth(eraseWidth, eraseType)
    }

    fun getCurrentEraseWidth(): Float = getEraseWidth(currentEraseType)

    fun setCurrentEraseWidth(currentEraseWidth: Float) {
        setEraseWidth(currentEraseType, currentEraseWidth)
    }

    fun isDisplayEraseTrack(): Boolean = isDisplayEraseTrack(currentEraseType)

    fun isDisplayEraseTrack(eraseType: Int): Boolean {
        val displayTrack = displayEraseTrackMap[eraseType]
        if (displayTrack != null) {
            return displayTrack
        }
        val defaultValue = EraserTrackHelper.defaultTrackEnabled(eraseType)
        displayEraseTrackMap[eraseType] = defaultValue
        return defaultValue
    }

    fun setDisplayEraseTrack(displayEraseTrack: Boolean) {
        this.displayEraseTrack = displayEraseTrack
        setDisplayEraseTrack(currentEraseType, displayEraseTrack)
    }

    fun setDisplayEraseTrack(eraseType: Int, displayEraseTrack: Boolean) {
        displayEraseTrackMap[eraseType] = displayEraseTrack
        if (eraseType == currentEraseType) {
            this.displayEraseTrack = displayEraseTrack
        }
    }

    fun isEnablePenUpRefresh(): Boolean = enablePenUpRefresh

    fun setEnablePenUpRefresh(enablePenUpRefresh: Boolean) {
        this.enablePenUpRefresh = enablePenUpRefresh
    }

    fun getPenUpRefreshTimeMs(): Int = penUpRefreshTimeMs

    fun setPenUpRefreshTimeMs(penUpRefreshTimeMs: Int) {
        this.penUpRefreshTimeMs = penUpRefreshTimeMs
    }

    fun getExcludeRectList(): MutableList<Rect>? = excludeRectList

    fun setExcludeRectList(excludeRectList: MutableList<Rect>?) {
        this.excludeRectList = excludeRectList
    }

    companion object {
        @Volatile
        private var instance: PenBundle? = null

        @JvmStatic
        fun getInstance(): PenBundle {
            val existing = instance
            if (existing != null) {
                return existing
            }
            return synchronized(this) {
                val again = instance
                again ?: PenBundle().also { instance = it }
            }
        }
    }
}
