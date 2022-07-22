package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityEacDemoBinding;
import com.onyx.android.sdk.api.device.eac.SimpleEACManage;
import com.onyx.android.sdk.rx.RxUtils;

import java.util.concurrent.Callable;


import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * App optimize entrance:long press app to select the optimization option or FloatingButton optimization option.
 */

public class EacDemoActivity extends AppCompatActivity {
    private ActivityEacDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eac_demo);
        initData();
    }

    private void initData() {
        updateAllStatus();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.allow_eac:
                setSupportEAC(true);
                break;
            case R.id.disallow_eac:
                setSupportEAC(false);
                break;
            case R.id.update_status:
                updateAllStatus();
                break;
            case R.id.cb_refresh_config_enable:
                toggleRefreshConfig();
        }
    }

    /**
     * This API is targeted at 3.2 and above.
     */
    private void toggleRefreshConfig() {
        RxUtils.runInIO(() -> {
            boolean enable = binding.cbRefreshConfigEnable.isChecked();
            SimpleEACManage.getInstance().setEACRefreshConfigEnable(EacDemoActivity.this, enable);
            updateRefreshConfigEnableStatus();
        });
    }

    /**
     * If support EAC is turned off, the optimization setting will not be available.
     * Parameters Context use activity can realize EAC config Immediate effect.(version 3.1 and before not supported，take effect need reopen app)
     */
    public void setSupportEAC(boolean support) {
        RxUtils.runInIO(new Runnable() {
            @Override
            public void run() {
                SimpleEACManage.getInstance().setSupportEAC(EacDemoActivity.this, support);
                updateAllStatus();
            }
        });
    }

    /**
     * It`s app optimize switch status, not about with eac enable/disable.
     */
    private void updateEACSwitchStatus() {
        RxUtils.runWith(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SimpleEACManage.getInstance().isAppEACEnabled(getPackageName());
            }
        }, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean enable) throws Exception {
                binding.eacEnableStatus.setText(getString(R.string.eac_enable_format, enable + ""));
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
            public void accept(Boolean enable) throws Exception {
                binding.hookEpdcStatus.setText(getString(R.string.hook_epdc_format, enable + ""));
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
                binding.eacSupportStatus.setText(getString(R.string.eac_support_format, support + ""));
            }
        }, Schedulers.io());
    }

    private void updateRefreshConfigEnableStatus() {
        RxUtils.runWith(() -> SimpleEACManage.getInstance().isEACRefreshConfigEnable(getPackageName()),
                enable -> binding.cbRefreshConfigEnable.setChecked(enable),
                Schedulers.io());
    }

    private void updateAllStatus() {
        updateEACSupportStatus();
        updateEACSwitchStatus();
        updateHookEpdcStatus();
        updateRefreshConfigEnableStatus();
    }
}