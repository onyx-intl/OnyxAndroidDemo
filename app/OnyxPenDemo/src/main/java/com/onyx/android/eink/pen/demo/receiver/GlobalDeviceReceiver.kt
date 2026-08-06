package com.onyx.android.eink.pen.demo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.event.FloatButtonChangedEvent
import com.onyx.android.eink.pen.demo.event.FloatButtonMenuStateChangedEvent
import com.onyx.android.eink.pen.demo.event.NotificationPanelChangeEvent
import com.onyx.android.eink.pen.demo.event.StatusBarChangeEvent
import com.onyx.android.sdk.utils.BroadcastHelper
import com.onyx.android.sdk.utils.EventBusUtils
import com.onyx.android.sdk.utils.StringUtils
import org.greenrobot.eventbus.EventBus

class GlobalDeviceReceiver : BroadcastReceiver() {
    fun enable(context: Context, enable: Boolean) {
        try {
            if (enable) {
                BroadcastHelper.ensureRegisterReceiver(context, this, intentFilter())
            } else {
                BroadcastHelper.ensureUnregisterReceiver(context, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun intentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(FLOAT_BUTTON_TOUCH_ACTION)
        filter.addAction(FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION)
        filter.addAction(NOTIFICAION_PANEL_OPEN_ACTION)
        filter.addAction(NOTIFICAION_PANEL_CLOSE_ACTION)
        filter.addAction(STATUS_BAR_SHOW_ACTION)
        filter.addAction(STATUS_BAR_HIDE_ACTION)
        return filter
    }

    private fun handleNotificationPanelChangeAction(intent: Intent) {
        val action = intent.action
        var open = false
        if (StringUtils.safelyEquals(action, NOTIFICAION_PANEL_OPEN_ACTION)) {
            open = true
        } else if (StringUtils.safelyEquals(action, NOTIFICAION_PANEL_CLOSE_ACTION)) {
            open = false
        }
        EventBusUtils.safelyPostEvent(this.eventBus, NotificationPanelChangeEvent(open))
    }

    private fun handleStatusBarChangeAction(intent: Intent) {
        val action = intent.action
        var show = false
        if (StringUtils.safelyEquals(action, STATUS_BAR_SHOW_ACTION)) {
            show = true
        } else if (StringUtils.safelyEquals(action, STATUS_BAR_HIDE_ACTION)) {
            show = false
        }
        EventBusUtils.safelyPostEvent(this.eventBus, StatusBarChangeEvent(show))
    }

    private fun handFloatButtonMenuStateChanged(intent: Intent) {
        val status = intent.getBooleanExtra(BroadcastHelper.FLOAT_BUTTON_MENU_STATE, false)
        EventBusUtils.safelyPostEvent(this.eventBus, FloatButtonMenuStateChangedEvent(status))
    }

    private fun handFloatTouch(intent: Intent) {
        val status = intent.getBooleanExtra(FLOAT_BUTTON_STATUS, false)
        EventBusUtils.safelyPostEvent(this.eventBus, FloatButtonChangedEvent(status))
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action ?: return
        if (StringUtils.safelyEquals(
                action, NOTIFICAION_PANEL_OPEN_ACTION
            ) || StringUtils.safelyEquals(action, NOTIFICAION_PANEL_CLOSE_ACTION)
        ) {
            handleNotificationPanelChangeAction(intent)
        } else if (StringUtils.safelyEquals(
                action, STATUS_BAR_SHOW_ACTION
            ) || StringUtils.safelyEquals(action, STATUS_BAR_HIDE_ACTION)
        ) {
            handleStatusBarChangeAction(intent)
        } else if (StringUtils.safelyEquals(action, FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION)) {
            handFloatButtonMenuStateChanged(intent)
        } else if (StringUtils.safelyEquals(action, FLOAT_BUTTON_TOUCH_ACTION)) {
            handFloatTouch(intent)
        }
    }

    private val eventBus: EventBus?
        get() = PenBundle.getInstance().getEventBus()

    companion object {
        const val FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION: String =
            BroadcastHelper.FLOAT_BUTTON_MENU_STATE_CHANGED_ACTION
        const val FLOAT_BUTTON_TOUCH_ACTION: String =
            BroadcastHelper.FLOAT_BUTTON_TOUCH_ACTION // float button config
        const val FLOAT_BUTTON_STATUS: String = "floatbutton_status"

        const val NOTIFICAION_PANEL_OPEN_ACTION: String =
            "com.android.systemui.NOTIFICAION_PANEL_OPEN_ACTION"
        const val NOTIFICAION_PANEL_CLOSE_ACTION: String =
            "com.android.systemui.NOTIFICAION_PANEL_CLOSE_ACTION"
        const val STATUS_BAR_SHOW_ACTION: String = "com.android.systemui.STATUS_BAR_SHOW_ACTION"
        const val STATUS_BAR_HIDE_ACTION: String = "com.android.systemui.STATUS_BAR_HIDE_ACTION"
    }
}
