package com.onyx.android.eink.pen.demo.shape

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.sdk.pen.PenUtils
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.utils.ResManager
import kotlin.math.max
import kotlin.math.sqrt

open class Shape {
    private var shapeTypeField: Int = 0
    private var textureField: Int = 0
    private var strokeColorField: Int = 0
    private var strokeWidthField: Float = 0f
    private var transparentField: Boolean = false

    @JvmField
    var touchPointList: TouchPointList? = null

    private var boundingRect: RectF? = null
    private var originRect: RectF? = null

    constructor()

    constructor(touchPointList: TouchPointList?) {
        this.touchPointList = touchPointList
    }

    fun setTransparent(transparent: Boolean) {
        this.transparentField = transparent
    }

    fun isTransparent(): Boolean = transparentField

    fun setShapeType(shapeType: Int): Shape {
        this.shapeTypeField = shapeType
        return this
    }

    fun setTexture(texture: Int): Shape {
        this.textureField = texture
        return this
    }

    fun setStrokeColor(strokeColor: Int): Shape {
        this.strokeColorField = strokeColor
        return this
    }

    fun setStrokeWidth(strokeWidth: Float): Shape {
        this.strokeWidthField = strokeWidth
        return this
    }

    fun setTouchPointList(touchPointList: TouchPointList?): Shape {
        this.touchPointList = touchPointList
        return this
    }

    fun getBoundingRect(): RectF? = boundingRect

    fun getShapeType(): Int = shapeTypeField

    fun getTexture(): Int = textureField

    fun getTouchPointList(): TouchPointList? = touchPointList

    fun setBoundingRect(boundingRect: RectF?) {
        this.boundingRect = boundingRect
    }

    fun getOriginRect(): RectF? = originRect

    fun setOriginRect(originRect: RectF?) {
        this.originRect = originRect
    }

    fun getStrokeColor(): Int = strokeColorField

    fun getStrokeWidth(): Float = strokeWidthField

    fun updateShapeRect() {
        val list = touchPointList?.points ?: return
        for (touchPoint in list) {
            if (touchPoint == null) {
                continue
            }
            val rect = originRect
            if (rect == null) {
                originRect = RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y)
            } else {
                rect.union(touchPoint.x, touchPoint.y)
            }
        }
        boundingRect = if (originRect != null) RectF(originRect) else null
    }

    open fun render(renderContext: RendererHelper.RenderContext) {
    }

    fun applyStrokeStyle(renderContext: RendererHelper.RenderContext) {
        val paint = renderContext.paint
        paint.strokeWidth = getRenderStrokeWidth()
        paint.color = strokeColorField
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 4.0f
        paint.pathEffect = null
        if (isTransparent()) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            paint.xfermode = null
        }
    }

    fun getRenderStrokeWidth(): Float {
        val renderStrokeWidth = getBaseRenderStrokeWidth()
        return if (isTransparent()) {
            renderStrokeWidth + PenUtils.ERASE_EXTRA_STROKE_WIDTH
        } else {
            renderStrokeWidth
        }
    }

    protected fun getBaseRenderStrokeWidth(): Float = mmToPx(getStrokeWidth())

    protected fun mmToPx(mm: Float): Float {
        return mm * ResManager.getAppContext().resources.displayMetrics.densityDpi / MM_OF_ONE_INCH
    }

    fun hitTestPoints(pointList: TouchPointList, radius: Float): Boolean {
        val points = pointList.points
        if (points.isNullOrEmpty()) {
            return false
        }
        var prev = points[0] ?: return false
        if (hitTest(prev.x, prev.y, radius)) {
            return true
        }
        val step = max(radius, 1f)
        for (i in 1 until points.size) {
            val cur = points[i] ?: continue
            val dx = cur.x - prev.x
            val dy = cur.y - prev.y
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            val samples = max(1, kotlin.math.ceil((dist / step).toDouble()).toInt())
            for (s in 1..samples) {
                val t = s.toFloat() / samples
                if (hitTest(prev.x + dx * t, prev.y + dy * t, radius)) {
                    return true
                }
            }
            prev = cur
        }
        return false
    }

    fun fastHitTest(x: Float, y: Float, radius: Float): Boolean {
        val bounds = boundingRect ?: return false
        val hitRect = RectF(bounds)
        hitRect.inset(-radius, -radius)
        return hitRect.contains(x, y)
    }

    fun hitTest(x: Float, y: Float, radius: Float): Boolean {
        val limit = radius
        var hit = false
        val point = floatArrayOf(x, y)
        val invertMatrix = Matrix()
        invertMatrix.mapPoints(point)
        val points = touchPointList?.points ?: return false
        for (i in 0 until points.size - 1) {
            val first = points[i] ?: continue
            val second = points[i + 1] ?: continue
            val isIntersect = hitTest(
                first.x,
                first.y,
                second.x,
                second.y,
                point[0],
                point[1],
                limit
            )
            if (isIntersect) {
                hit = true
                break
            }
        }
        return hit
    }

    private fun hitTest(
        x1: Float, y1: Float, x2: Float,
        y2: Float, x: Float, y: Float, limit: Float
    ): Boolean {
        val value = distance(x1, y1, x2, y2, x, y)
        return value <= limit
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float): Float {
        val A = x - x1
        val B = y - y1
        val C = x2 - x1
        val D = y2 - y1

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        var param = -1.0f
        if (lenSq != 0f) {
            param = dot / lenSq
        }

        val xx: Float
        val yy: Float

        if (param < 0) {
            xx = x1
            yy = y1
        } else if (param > 1) {
            xx = x2
            yy = y2
        } else {
            xx = x1 + param * C
            yy = y1 + param * D
        }

        val dx = x - xx
        val dy = y - yy
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    companion object {
        // 1 inch = 25.4 mm (SI unit definition). Used in mmToPx() to convert
        // millimetre stroke widths to pixel-based rendering units.
        private const val MM_OF_ONE_INCH = 25.4f
    }
}
