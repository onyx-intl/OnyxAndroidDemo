package com.android.onyx.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.android.onyx.demo.scribble.ScribbleDemoActivity;
import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityMainBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final View view = binding.getRoot();
        binding.setActivityMain(this);
        EpdController.enablePost(view, 1);
    }

    public void button_epd(View view) {
        go(EpdDemoActivity.class);
    }

    public void button_front_light(View view) {
        go(FrontLightDemoActivity.class);
    }

    public void button_full_screen(View view) {
        go(FullScreenDemoActivity.class);
    }

    public void button_environment(View view) {
        go(EnvironmentDemoActivity.class);
    }

    public void button_scribble_demo(View view) {
        go(ScribbleDemoActivity.class);
    }

    public void btn_dict_query(View view) {
        go(DictionaryActivity.class);
    }

    public void btn_reader(View view) {
        go(ReaderDemoActivity.class);
    }

    public void btn_screen_saver(View view) {
        go(ScreensaverActivity.class);
    }

    public void btn_open_setting(View view) {
        go(OpenSettingActivity.class);
    }

    public void btn_webview_optimize(View view) {
        go(WebViewOptimizeActivity.class);
    }

    public void btn_open_kcb(View view) {
        go(OpenKcbActivity.class);
    }

    public void btn_open_ota(View view) {
        go(OTADemoActivity.class);
    }

    public void onClickButtonRefreshMode(View view) {
        go(RefreshModeDemoActivity.class);
    }

    public void onClickButtonEacDemo(View view) {
        go(EacDemoActivity.class);
    }

    public void openBooxSettingDemo(View view) {
        go(BooxSettingsDemoActivity.class);
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

}
