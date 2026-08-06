package com.onyx.android.eink.pen.demo.ui.popup

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.onyx.android.eink.pen.demo.action.PopupChangeAction

open class BasePopup(protected var context: Context?) : PopupWindow(context) {
    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        onPopupWindowChange(true)
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    override fun dismiss() {
        super.dismiss()
        onPopupWindowChange(false)
    }

    private fun onPopupWindowChange(show: Boolean) {
        PopupChangeAction(show).execute()
    }
}
