package com.onyx.android.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onyx.android.sdk.api.device.EpdDeviceManager;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EpdDemoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EpdDemoActivity.class.getSimpleName();
    @Bind(R.id.button_partial_update)
    Button button_partial_update;
    @Bind(R.id.button_regal_partial)
    Button button_regal_partial;
    @Bind(R.id.button_enter_fast_mode)
    Button button_enter_fast_mode;
    @Bind(R.id.button_quit_fast_mode)
    Button button_quit_fast_mode;
    @Bind(R.id.button_screen_refresh)
    Button button_screen_refresh;
    @Bind(R.id.textview)
    TextView textView;
    @Bind(R.id.surfaceview)
    SurfaceView surfaceView;
    @Bind(R.id.button_enter_x_mode)
    Button button_enter_x_mode;
    @Bind(R.id.activity_main)
    RelativeLayout activityMain;
    @Bind(R.id.button_enter_normal_mode)
    Button button_enter_normal_mode;
    @Bind(R.id.button_enter_A2_mode)
    Button button_enter_A2_mode;
    @Bind(R.id.button_enter_du_mode)
    Button button_enter_du_mode;
    private boolean isFastMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epd_demo);

        ButterKnife.bind(this);
        button_partial_update.setOnClickListener(this);
        button_regal_partial.setOnClickListener(this);
        button_enter_fast_mode.setOnClickListener(this);
        button_quit_fast_mode.setOnClickListener(this);
        button_screen_refresh.setOnClickListener(this);
        button_enter_x_mode.setOnClickListener(this);
        button_enter_normal_mode.setOnClickListener(this);
        button_enter_A2_mode.setOnClickListener(this);
        button_enter_du_mode.setOnClickListener(this);

        // set full update after how many partial update
        EpdDeviceManager.setGcInterval(5);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(button_partial_update)) {
            updateTextView();
            EpdDeviceManager.applyWithGCIntervalWithoutRegal(textView);
        } else if (v.equals(button_regal_partial)) {
            updateTextView();
            EpdDeviceManager.applyWithGCIntervalWitRegal(textView, true);
        } else if (v.equals(button_screen_refresh)) {
            updateTextView();
            EpdController.repaintEveryThing(UpdateMode.GC);
        } else if (v.equals(button_enter_fast_mode)) {
            isFastMode = true;
            EpdDeviceManager.enterAnimationUpdate(true);
        } else if (v.equals(button_quit_fast_mode)) {
            EpdDeviceManager.exitAnimationUpdate(true);
            isFastMode = false;
        } else if (v.equals(button_enter_x_mode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.ANIMATION_X, Integer.MAX_VALUE);
        } else if (v.equals(button_enter_A2_mode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.ANIMATION_QUALITY, Integer.MAX_VALUE);
        } else if (v.equals(button_enter_normal_mode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, false, true);
        } else if (v.equals(button_enter_du_mode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.DU_QUALITY, Integer.MAX_VALUE);
        }
    }

    private void updateTextView() {
        StringBuilder sb = new StringBuilder();
        sb.append(textView.getText());
        sb.append("hello world!");
        textView.setText(sb.toString());
    }
}
