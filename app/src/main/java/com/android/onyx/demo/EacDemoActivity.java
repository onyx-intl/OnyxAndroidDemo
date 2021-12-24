package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.api.device.eac.SimpleEACManage;
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
    @Bind(R.id.eac_support)
    Button eacSupport;
    @Bind(R.id.eac_support_status)
    TextView eacSupportStatus;
    @Bind(R.id.allow_eac)
    Button allowEAC;
    @Bind(R.id.disallow_eac)
    Button disallowEAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eac_demo);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        updateAllStatus();
    }

    private void initView() {
        hookEpdc.setOnClickListener(this);
        eacEnable.setOnClickListener(this);
        eacSupport.setOnClickListener(this);
        allowEAC.setOnClickListener(this);
        disallowEAC.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hook_epdc:
                updateHookEpdcStatus();
                break;
            case R.id.eac_enable:
                updateEACSwitchStatus();
                break;
            case R.id.eac_support:
                updateEACSupportStatus();
                break;
            case R.id.allow_eac:
                setSupportEAC(true);
                break;
            case R.id.disallow_eac:
                setSupportEAC(false);
                break;
        }
    }

    /**
     * @param support
     * If support EAC is turned off, the optimization setting will not be available.
     * (App optimize entrance:long press app to select the optimization option or FloatingButton optimization option.)
     */
    public void setSupportEAC(boolean support) {
        RxUtils.runInIO(new Runnable() {
            @Override
            public void run() {
                SimpleEACManage.getInstance().setSupportEAC(getApplicationContext(), support);
                updateAllStatus();
            }
        });
    }

    /**
     *It`s app optimize switch status, not about with eac enable/disable.
     */
    private void updateEACSwitchStatus() {
        RxUtils.runWith(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SimpleEACManage.getInstance().isAppEACEnabled(getPackageName());
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
                return SimpleEACManage.getInstance().isHookEpdc(getPackageName());
            }
        }, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                hookEpdcStatus.setText(aBoolean + "");
            }
        }, Schedulers.io());
    }

    private void updateEACSupportStatus() {
        RxUtils.runWith(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SimpleEACManage.getInstance().isSupportEAC(getPackageName());
            }
        }, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean support) throws Exception {
                eacSupportStatus.setText(support + "");
            }
        }, Schedulers.io());
    }

    private void updateAllStatus() {
        updateEACSupportStatus();
        updateEACSwitchStatus();
        updateHookEpdcStatus();
    }
}