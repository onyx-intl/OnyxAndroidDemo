package com.onyx.android.eink.pen.demo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.event.FloatButtonChangedEvent;
import com.onyx.android.eink.pen.demo.event.FloatButtonMenuStateChangedEvent;
import com.onyx.android.eink.pen.demo.event.NotificationPanelChangeEvent;
import com.onyx.android.eink.pen.demo.event.StatusBarChangeEvent;
import com.onyx.android.sdk.utils.BroadcastHelper;
import com.onyx.android.sdk.utils.EventBusUtils;
import com.onyx.android.sdk.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;

import static com.onyx.android.sdk.utils.BroadcastHelper.FLOAT_BUTTON_MENU_STATE;

public class GlobalDeviceReceiver extends BroadcastReceiver {

    public static final String FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION = BroadcastHelper.FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION;
    public static final String FLOAT_BUTTON_TOUCH_ACTION = BroadcastHelper.FLOAT_BUTTON_TOUCH_ACTION; // float button config
    public static final String FLOAT_BUTTON_STATUS = "floatbutton_status";

    public static final String NOTIFICAION_PANEL_OPEN_ACTION = "com.android.systemui.NOTIFICAION_PANEL_OPEN_ACTION";
    public static final String NOTIFICAION_PANEL_CLOSE_ACTION = "com.android.systemui.NOTIFICAION_PANEL_CLOSE_ACTION";
    public static final String STATUS_BAR_SHOW_ACTION = "com.android.systemui.STATUS_BAR_SHOW_ACTION";
    public static final String STATUS_BAR_HIDE_ACTION = "com.android.systemui.STATUS_BAR_HIDE_ACTION";

    public void enable(Context context, boolean enable) {
        try {
            if (enable) {
                BroadcastHelper.ensureRegisterReceiver(context, this, intentFilter());
            } else {
                BroadcastHelper.ensureUnregisterReceiver(context, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IntentFilter intentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FLOAT_BUTTON_TOUCH_ACTION);
        filter.addAction(FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION);
        filter.addAction(NOTIFICAION_PANEL_OPEN_ACTION);
        filter.addAction(NOTIFICAION_PANEL_CLOSE_ACTION);
        filter.addAction(STATUS_BAR_SHOW_ACTION);
        filter.addAction(STATUS_BAR_HIDE_ACTION);
        return filter;
    }

    private void handleNotificationPanelChangeAction(Intent intent) {
        String action = intent.getAction();
        boolean open = false;
        if (StringUtils.safelyEquals(action, NOTIFICAION_PANEL_OPEN_ACTION)) {
            open = true;
        } else if (StringUtils.safelyEquals(action, NOTIFICAION_PANEL_CLOSE_ACTION)) {
            open = false;
        }
        EventBusUtils.safelyPostEvent(getEventBus(), new NotificationPanelChangeEvent(open));
    }

    private void handleStatusBarChangeAction(Intent intent) {
        String action = intent.getAction();
        boolean show = false;
        if (StringUtils.safelyEquals(action, STATUS_BAR_SHOW_ACTION)) {
            show = true;
        } else if (StringUtils.safelyEquals(action, STATUS_BAR_HIDE_ACTION)) {
            show = false;
        }
        EventBusUtils.safelyPostEvent(getEventBus(), new StatusBarChangeEvent(show));
    }

    private void handFloatButtonMenuStateChanged(Intent intent) {
        boolean status = intent.getBooleanExtra(FLOAT_BUTTON_MENU_STATE, false);
        EventBusUtils.safelyPostEvent(getEventBus(), new FloatButtonMenuStateChangedEvent(status));
    }

    private void handFloatTouch(Intent intent) {
        boolean status = intent.getBooleanExtra(FLOAT_BUTTON_STATUS, false);
        EventBusUtils.safelyPostEvent(getEventBus(), new FloatButtonChangedEvent(status));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (StringUtils.safelyEquals(action, NOTIFICAION_PANEL_OPEN_ACTION)
                || StringUtils.safelyEquals(action, NOTIFICAION_PANEL_CLOSE_ACTION)) {
            handleNotificationPanelChangeAction(intent);
        } else if (StringUtils.safelyEquals(action, STATUS_BAR_SHOW_ACTION)
                || StringUtils.safelyEquals(action, STATUS_BAR_HIDE_ACTION)) {
            handleStatusBarChangeAction(intent);
        } else if (StringUtils.safelyEquals(action, FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION)) {
            handFloatButtonMenuStateChanged(intent);
        } else if (StringUtils.safelyEquals(action, FLOAT_BUTTON_TOUCH_ACTION)) {
            handFloatTouch(intent);
        }
    }

    private EventBus getEventBus() {
        return getPenBundle().getEventBus();
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

}
