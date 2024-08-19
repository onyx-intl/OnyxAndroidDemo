package com.onyx.android.eink.pen.demo.ui.view;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.event.ApplyFastModeEvent;
import com.onyx.android.eink.pen.demo.event.FloatMenuStateChangeEvent;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.utils.EventBusUtils;
import com.onyx.android.sdk.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class FloatingMenuDragHandler implements View.OnTouchListener {
    private final View floatingMenu;
    private Rect limitRect;
    private TouchPoint lastMovePoint;
    private Size measureSize;

    private static final long LONG_PRESS_THRESHOLD = 500; // 长按阈值，单位毫秒
    private boolean isLongPressed;
    private long downTime;

    public FloatingMenuDragHandler(View floatingMenu) {
        this.floatingMenu = floatingMenu;
    }

    public FloatingMenuDragHandler setLimitRect(Rect limitRect) {
        this.limitRect = limitRect;
        return this;
    }

    public int getViewStart(View view) {
        if (view == null) {
            return 0;
        }
        return view.getLeft();
    }

    public int getViewTop() {
        return floatingMenu.getTop();
    }

    protected int getMaxPosX() {
        return limitRect.width() - getViewWidth();
    }

    protected int getViewWidth() {
        if (floatingMenu == null) {
            return 0;
        }
        return measureSize.getWidth();
    }

    protected int getViewHeight() {
        if (floatingMenu == null) {
            return 0;
        }
        return measureSize.getHeight();
    }

    private EventBus getEventBus() {
        return getPenBundle().getEventBus();
    }


    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downTime = SystemClock.elapsedRealtime();
                break;
            case MotionEvent.ACTION_UP:
                if (!isLongPressed) {
                    break;
                }
                isLongPressed = false;
                lastMovePoint = null;

                List<Rect> excludeRectList = new ArrayList<>();
                Rect funcMenuExcludeRect = ViewUtils.relativelyParentRect(floatingMenu);
                excludeRectList.add(funcMenuExcludeRect);
                getPenBundle().setExcludeRectList(excludeRectList);

                EventBusUtils.safelyPostEvent(getEventBus(), new ApplyFastModeEvent(false));
                EventBusUtils.safelyPostEvent(getEventBus(), new FloatMenuStateChangeEvent(false));
                break;
            case MotionEvent.ACTION_MOVE:
                if (SystemClock.elapsedRealtime() - downTime <= LONG_PRESS_THRESHOLD) {
                    break;
                }
                if (!isLongPressed) {
                    isLongPressed = true;
                    EventBusUtils.safelyPostEvent(getEventBus(), new FloatMenuStateChangeEvent(true));
                    EventBusUtils.safelyPostEvent(getEventBus(), new ApplyFastModeEvent(true));
                }
                int currentX = (int) event.getRawX();
                int currentY = (int) event.getRawY();
                measureSize = ViewUtils.getMeasureSize(floatingMenu);
                int moveX = lastMovePoint == null ? 0 : (int) (currentX - lastMovePoint.x);
                int moveY = lastMovePoint == null ? 0 : (int) (currentY - lastMovePoint.y);
                int dragX = getViewStart(floatingMenu) + moveX;
                int dragY = getViewTop() + moveY;
                int posX = Math.min(dragX, limitRect.width() - getViewWidth());
                if (posX == limitRect.width() - getViewWidth()) {
                    posX = getMaxPosX();
                }
                int posY = Math.min(dragY, limitRect.height() - getViewHeight());
                posX = Math.max(0, posX);
                posY = Math.max(0, posY);

                ViewUtils.updateRelativeLayoutViewPosition(floatingMenu, posX, posY);
                lastMovePoint = new TouchPoint(currentX, currentY);
                break;
        }
        return true;
    }
}