package com.onyx.android.eink.pen.demo.ui.popup;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import com.onyx.android.eink.pen.demo.action.PopupChangeAction;

public class BasePopup extends PopupWindow {
    protected Context context;

    public BasePopup(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        onPopupWindowChange(true);
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        onPopupWindowChange(false);
    }

    private void onPopupWindowChange(boolean show) {
        new PopupChangeAction(show).execute();
    }

}
