package com.onyx.android.eink.pen.demo.ui.view

import android.graphics.Rect
import android.os.SystemClock
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.event.ApplyFastModeEvent
import com.onyx.android.eink.pen.demo.event.DemoFloatMenuStateChangeEvent
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.utils.EventBusUtils
import com.onyx.android.sdk.utils.ViewUtils
import org.greenrobot.eventbus.EventBus
import kotlin.math.max
import kotlin.math.min

class FloatingMenuDragHandler(private val floatingMenu: View) : OnTouchListener {
    private var limitRect: Rect? = null
    private var lastMovePoint: TouchPoint? = null
    private var measureSize: Size? = null

    private var isLongPressed = false
    private var downTime: Long = 0

    fun setLimitRect(limitRect: Rect): FloatingMenuDragHandler {
        this.limitRect = limitRect
        return this
    }

    fun getViewStart(view: View?): Int {
        if (view == null) {
            return 0
        }
        return view.left
    }

    val viewTop: Int
        get() = floatingMenu.top

    protected val maxPosX: Int
        get() {
            val rect = limitRect ?: throw NullPointerException("limitRect")
            return rect.width() - this.viewWidth
        }

    protected val viewWidth: Int
        get() {
            val size = measureSize ?: throw NullPointerException("measureSize")
            return size.width
        }

    protected val viewHeight: Int
        get() {
            val size = measureSize ?: throw NullPointerException("measureSize")
            return size.height
        }

    private val eventBus: EventBus
        get() = this.penBundle.getEventBus()

    private val penBundle: PenBundle
        get() = PenBundle.getInstance()

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> downTime = SystemClock.elapsedRealtime()
            MotionEvent.ACTION_UP -> {
                if (isLongPressed) {
                    isLongPressed = false
                    lastMovePoint = null

                    val excludeRectList: MutableList<Rect> = ArrayList()
                    val funcMenuExcludeRect = ViewUtils.relativelyParentRect(floatingMenu)
                    excludeRectList.add(funcMenuExcludeRect)
                    this.penBundle.setExcludeRectList(excludeRectList)

                    EventBusUtils.safelyPostEvent(this.eventBus, ApplyFastModeEvent(false))
                    EventBusUtils.safelyPostEvent(this.eventBus, DemoFloatMenuStateChangeEvent(false))
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (SystemClock.elapsedRealtime() - downTime > LONG_PRESS_THRESHOLD) {
                    if (!isLongPressed) {
                        isLongPressed = true
                        EventBusUtils.safelyPostEvent(
                            this.eventBus, DemoFloatMenuStateChangeEvent(true)
                        )
                        EventBusUtils.safelyPostEvent(this.eventBus, ApplyFastModeEvent(true))
                    }
                    val currentX = event.rawX.toInt()
                    val currentY = event.rawY.toInt()
                    val size = ViewUtils.getMeasureSize(floatingMenu)
                    measureSize = size
                    val last = lastMovePoint
                    val moveX = if (last == null) 0 else (currentX - last.x).toInt()
                    val moveY = if (last == null) 0 else (currentY - last.y).toInt()
                    val dragX = getViewStart(floatingMenu) + moveX
                    val dragY = this.viewTop + moveY
                    val rect = limitRect ?: throw NullPointerException("limitRect")
                    var posX = min(dragX, rect.width() - this.viewWidth)
                    if (posX == rect.width() - this.viewWidth) {
                        posX = this.maxPosX
                    }
                    var posY = min(dragY, rect.height() - this.viewHeight)
                    posX = max(0, posX)
                    posY = max(0, posY)

                    ViewUtils.updateRelativeLayoutViewPosition(floatingMenu, posX, posY)
                    lastMovePoint = TouchPoint(currentX.toFloat(), currentY.toFloat())
                }
            }
        }
        return true
    }

    companion object {
        private const val LONG_PRESS_THRESHOLD: Long = 500 // 长按阈值，单位毫秒
    }
}
