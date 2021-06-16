package com.onyx.android.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.onyx.android.sdk.api.device.eac.SimpleEACManage;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.rx.RxUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EacDemoActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.hook_epdc)
    Button hookEpdc;
    @Bind(R.id.eac_enable)
    Button eacEnable;
    @Bind(R.id.allow_eac)
    Button allowEac;
    @Bind(R.id.disallow_eac)
    Button disallowEac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eac_demo);
        ButterKnife.bind(this);
        hookEpdc.setOnClickListener(this);
        eacEnable.setOnClickListener(this);
        allowEac.setOnClickListener(this);
        disallowEac.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(hookEpdc)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    boolean hookEpdc = SimpleEACManage.getInstance().isHookEpdc(getPackageName());
                }
            });
        } else if (v.equals(eacEnable)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    boolean appEACEnabled = SimpleEACManage.getInstance().isAppEACEnabled(getPackageName());
                }
            });
        } else if (v.equals(allowEac)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    Device.currentDevice().unregisterEACWhiteList(getApplicationContext());
                }
            });
        } else if (v.equals(disallowEac)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    Device.currentDevice().registerEACWhiteList(getApplicationContext());
                }
            });
        }
    }
}