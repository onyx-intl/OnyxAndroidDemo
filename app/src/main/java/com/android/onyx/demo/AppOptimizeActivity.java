package com.android.onyx.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.databinding.DataBindingUtil;


import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityAppOptimizeBinding;

/**
 * Created by Administrator on 2018/3/26 17:35.
 */

public class AppOptimizeActivity extends Activity {
    private EditText etIsFull;
    private EditText etPkgName;
    private Button btnSendBroadcast;
    private ActivityAppOptimizeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_optimize);


    }

    public void onClick(View v) {
        boolean isfull;
        String isfullTxt = etIsFull.getText().toString();
        if (isfullTxt.equals("false")) {
            isfull = false;
        } else {
            isfull = true;
        }
        String pkgnameTxt = etPkgName.getText().toString();
        Intent intent = new Intent();
        intent.setAction("com.onyx.app.optimize.setting");
        intent.putExtra("optimize_fullScreen", isfull);
        intent.putExtra("optimize_pkgName", pkgnameTxt);
        sendBroadcast(intent);
    }

}

