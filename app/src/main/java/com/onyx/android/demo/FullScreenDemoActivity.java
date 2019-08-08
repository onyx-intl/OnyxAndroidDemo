package com.onyx.android.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.onyx.android.sdk.utils.DeviceUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullScreenDemoActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.button_full_screen)
    Button buttonFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_demo);

        ButterKnife.bind(this);
        buttonFullScreen.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(buttonFullScreen)) {
            boolean fullscreen = !DeviceUtils.isFullScreen(this);
            DeviceUtils.setFullScreenOnResume(this, fullscreen);
        }
    }
}
