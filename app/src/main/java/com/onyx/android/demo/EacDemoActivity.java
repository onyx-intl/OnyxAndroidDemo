package com.onyx.android.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.rx.RxUtils;

import java.util.concurrent.Callable;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EacDemoActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.hook_epdc)
    Button hookEpdc;
    @Bind(R.id.hook_epdc_status)
    TextView hookEpdcStatus;
    @Bind(R.id.eac_enable)
    Button eacEnable;
    @Bind(R.id.eac_enable_status)
    TextView eacEnableStatus;
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
            updateHookEpdcStatus();
        } else if (v.equals(eacEnable)) {
            updateEacSwitchStatus();
        } else if (v.equals(allowEac)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    Device.currentDevice().unregisterEACWhiteList(getApplicationContext());
                    updateHookEpdcStatus();
                }
            });
        } else if (v.equals(disallowEac)) {
            RxUtils.runInIO(new Runnable() {
                @Override
                public void run() {
                    Device.currentDevice().registerEACWhiteList(getApplicationContext());
                    updateHookEpdcStatus();
                }
            });
        }
    }

    /**
     *It`s app optimize switch status, not about with eac enable/disable.
     */
    private void updateEacSwitchStatus() {
        RxUtils.runWith(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
//                return SimpleEACManage.getInstance().isAppEACEnabled(getPackageName());
                return false;
            }
        }, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                eacEnableStatus.setText(aBoolean + "");
            }
        }, Schedulers.io());
    }

    private void updateHookEpdcStatus() {
        RxUtils.runWith(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
//                return SimpleEACManage.getInstance().isHookEpdc(getPackageName());
                return false;
            }
        }, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                hookEpdcStatus.setText(aBoolean + "");
            }
        }, Schedulers.io());
    }
}