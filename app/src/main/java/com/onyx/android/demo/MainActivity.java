package com.onyx.android.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onyx.android.demo.note.NoteDemoActivity;
import com.onyx.android.demo.scribble.ScribbleDemoActivity;
import com.onyx.android.sdk.api.device.epd.EpdController;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        final View view = findViewById(android.R.id.content);
        EpdController.enablePost(view, 1);
    }


    @OnClick(R.id.button_epd)
    public void button_epd() {
        go(EpdDemoActivity.class);
    }

    @OnClick(R.id.button_front_light)
    public void button_front_light() {
        go(FrontLightDemoActivity.class);
    }

    @OnClick(R.id.button_full_screen)
    public  void button_full_screen() {
        go(FullScreenDemoActivity.class);
    }

    @OnClick(R.id.button_environment)
    public void button_environment() {
        go(EnvironmentDemoActivity.class);
    }

    @OnClick(R.id.button_scribble_demo)
    public void button_scribble_demo() {
        go(ScribbleDemoActivity.class);
    }

    @OnClick(R.id.btn_dict_query)
    public void btn_dict_query(){
        go(DictionaryActivity.class);
    }
    
    @OnClick(R.id.button_reader)
    public void btn_reader() {
        go(ReaderDemoActivity.class);
    }

    @OnClick(R.id.btn_screen_saver)
    public void btn_screen_saver() {
        go(ScreensaverActivity.class);
    }

    @OnClick(R.id.btn_open_setting)
    public void btn_open_setting() {
        go(OpenSettingActivity.class);
    }

    @OnClick(R.id.btn_webview_optimize)
    public void btn_webview_optimize() {
        go(WebViewOptimizeActivity.class);
    }

    @OnClick(R.id.btn_open_kcb)
    public void btn_open_kcb() {
        go(OpenKcbActivity.class);
    }

    @OnClick(R.id.btn_open_ota)
    public void btn_open_ota() {
        go(OTADemoActivity.class);
    }

    @OnClick(R.id.button_note_demo)
    public void noteDemo() {
        go(NoteDemoActivity.class);
    }

    @OnClick(R.id.button_refresh_mode)
    public void onClickButtonRefreshMode() {
        go(RefreshModeDemoActivity.class);
    }

    @OnClick(R.id.button_eac)
    public void onClickButtonEacDemo() {
        go(EacDemoActivity.class);
    }

    private void go(Class<?> activityClass){
        startActivity(new Intent(this, activityClass));
    }
}
