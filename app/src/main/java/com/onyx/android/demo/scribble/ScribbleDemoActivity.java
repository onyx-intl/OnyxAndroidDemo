package com.onyx.android.demo.scribble;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.onyx.android.demo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleDemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sribble_demo);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.button_scribble_touch_helper)
    public void button_scribble_touch_helper() {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    @OnClick(R.id.button_surfaceview_stylus_scribble)
    public void button_surfaceview_stylus_scribble() {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    @OnClick(R.id.button_webview_stylus_scribble)
    public void button_webview_stylus_scribble() {
        go(ScribbleWebViewDemoActivity.class);
    }

    @OnClick(R.id.button_move_erase_scribble)
    public void button_move_erase_scribble() {
        go(ScribbleMoveEraserDemoActivity.class);
    }

    @OnClick(R.id.button_multiple_scribble)
    public void button_multiple_scribble() {
        go(ScribbleMultipleScribbleViewActivity.class);
    }

    @OnClick(R.id.button_hwr)
    public void button_hwr_scribble() {
        go(ScribbleHWRActivity.class);
    }

    @OnClick(R.id.button_save_points)
    public void button_save_points() {
        go(ScribbleSavePointsDemoActivity.class);
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
