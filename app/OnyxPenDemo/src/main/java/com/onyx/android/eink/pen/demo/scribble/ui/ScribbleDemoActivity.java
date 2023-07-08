package com.onyx.android.eink.pen.demo.scribble.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.ActivitySribbleDemoBinding;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleDemoActivity extends AppCompatActivity {
    private ActivitySribbleDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sribble_demo);
        binding.setActivitySribble(this);
    }

    public void button_scribble_touch_helper(View view) {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    public void button_surfaceview_stylus_scribble(View view) {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    public void button_webview_stylus_scribble(View view) {
        go(ScribbleWebViewDemoActivity.class);
    }

    public void button_move_erase_scribble(View view) {
        go(ScribbleMoveEraserDemoActivity.class);
    }

    public void button_multiple_scribble(View view) {
        go(ScribbleMultipleScribbleViewActivity.class);
    }

    public void button_pen_up_refresh(View view) {
        go(ScribblePenUpRefreshDemoActivity.class);
    }

    public void button_epd_controller(View view) {
        go(ScribbleEpdControllerDemoActivity.class);
    }

    public void gotoScribbleFingerTouchDemo(View view) {
        go(ScribbleFingerTouchDemoActivity.class);
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
