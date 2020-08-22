package com.onyx.wereaddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_demo);
        initView();
    }

    private void initView() {
        Button openWifi = findViewById(R.id.open_wifi);
        openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(WifiDemoActivity.class);
            }
        });
    }

    public void startActivity(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
