package com.onyx.android.eink.pen.demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.ActivityMainBinding;
import com.onyx.android.eink.pen.demo.scribble.ui.ScribbleDemoActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.buttonScribbleDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(ScribbleDemoActivity.class);
            }
        });
        binding.buttonPenDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(PenDemoActivity.class);
            }
        });
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

}
