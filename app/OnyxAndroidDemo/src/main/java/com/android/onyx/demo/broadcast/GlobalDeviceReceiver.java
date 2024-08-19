package com.android.onyx.demo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.onyx.android.sdk.utils.BroadcastHelper;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.DeviceReceiver;
import com.onyx.android.sdk.utils.StringUtils;

public class GlobalDeviceReceiver extends BroadcastReceiver {

    public static final String SYSTEM_UI_DIALOG_OPEN_ACTION = DeviceReceiver.SYSTEM_UI_DIALOG_OPEN_ACTION;
    public static final String SYSTEM_UI_DIALOG_CLOSE_ACTION = DeviceReceiver.SYSTEM_UI_DIALOG_CLOSE_ACTION;
    public static final String SYSTEM_SCREEN_ON_ACTION = Intent.ACTION_SCREEN_ON;
    public static final String DIALOG_TYPE_NOTIFICATION_PANEL = DeviceReceiver.DIALOG_TYPE_NOTIFICATION_PANEL;
    public static final String DIALOG_TYPE = DeviceReceiver.DIALOG_TYPE;

    public SystemNotificationPanelChangeListener systemNotificationPanelChangeListener;
    public SystemScreenOnListener systemScreenOnListener;

    public interface SystemNotificationPanelChangeListener {
        void onNotificationPanelChanged(boolean open);
    }

    public interface SystemScreenOnListener {
        void onScreenOn();
    }

    public GlobalDeviceReceiver setSystemNotificationPanelChangeListener(SystemNotificationPanelChangeListener listener) {
        this.systemNotificationPanelChangeListener = listener;
        return this;
    }

    public GlobalDeviceReceiver setSystemScreenOnListener(SystemScreenOnListener listener) {
        this.systemScreenOnListener = listener;
        return this;
    }

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
        filter.addAction(DeviceReceiver.STATUS_BAR_SHOW_ACTION);
        filter.addAction(DeviceReceiver.STATUS_BAR_HIDE_ACTION);
        filter.addAction(SYSTEM_UI_DIALOG_OPEN_ACTION);
        filter.addAction(SYSTEM_UI_DIALOG_OPEN_ACTION);
        filter.addAction(SYSTEM_UI_DIALOG_CLOSE_ACTION);
        filter.addAction(SYSTEM_SCREEN_ON_ACTION);
        return filter;
    }

    private void handleSystemUIDialogAction(Intent intent) {
        String action = intent.getAction();
        String dialogType = intent.getStringExtra(DIALOG_TYPE);
        boolean open = false;
        if (StringUtils.safelyEquals(dialogType, DIALOG_TYPE_NOTIFICATION_PANEL)) {
            if (StringUtils.safelyEquals(action, SYSTEM_UI_DIALOG_OPEN_ACTION)) {
                open = true;
            } else if (StringUtils.safelyEquals(action, SYSTEM_UI_DIALOG_CLOSE_ACTION)) {
                open = false;
            }
            if (systemNotificationPanelChangeListener != null) {
                systemNotificationPanelChangeListener.onNotificationPanelChanged(open);
            }
        }
    }

    private void handSystemScreenOnAction() {
        if (systemScreenOnListener != null) {
            systemScreenOnListener.onScreenOn();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Debug.e(getClass(), "zzzzwb onReceive action = " + action);
        if (action == null) {
            return;
        }
        if (StringUtils.safelyEquals(action, SYSTEM_UI_DIALOG_OPEN_ACTION)
                || StringUtils.safelyEquals(action, SYSTEM_UI_DIALOG_CLOSE_ACTION)) {
            handleSystemUIDialogAction(intent);
        } else if (StringUtils.safelyEquals(action, SYSTEM_SCREEN_ON_ACTION)) {
            handSystemScreenOnAction();
        }
    }
}
