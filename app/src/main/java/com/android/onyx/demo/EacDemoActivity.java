package com.android.onyx.demo;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityEacDemoBinding;
import com.onyx.android.sdk.api.device.eac.SimpleEACManage;
import com.onyx.android.sdk.rx.RxUtils;
import com.onyx.android.sdk.utils.DeviceUtils;
import com.onyx.android.sdk.utils.RotationUtils;
import com.onyx.android.sdk.utils.RxTimerUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * App optimize entrance:long press app to select the optimization option or FloatingButton optimization option.
 */

public class EacDemoActivity extends AppCompatActivity {
    private static final int UPDATE_EAC_STATUE_DELAY = 300;
    private final String[] rotationItemArray = new String[]{"rotation 0", "rotation 90", "rotation 180", "rotation 270"};
    private ActivityEacDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eac_demo);
        initView();
        initData();
    }

    private void initView() {
        binding.switchEacSupport.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setSupportEAC(isChecked);
        });
        binding.switchEacEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setEACEnable(isChecked);
        });
    }

    private void initData() {
        updateAllStatus();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.system_rotation:
                systemRotation();
                break;
            case R.id.app_rotation:
                appRotation();
                break;
            case R.id.cb_refresh_config_enable:
                toggleRefreshConfig();
                break;
        }
    }

    /**
     * This API is targeted at 3.2 and above.
     */
    private void toggleRefreshConfig() {
        RxUtils.runInIO(() -> {
            boolean enable = binding.cbRefreshConfigEnable.isChecked();
            SimpleEACManage.getInstance().setEACRefreshConfigEnable(EacDemoActivity.this, enable);
            binding.cbRefreshConfigEnable.setChecked(enable);
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
                updateAllStatusDelay();
            }
        });
    }

    public void setEACEnable(boolean enable) {
        RxUtils.runInIO(new Runnable() {
            @Override
            public void run() {
                SimpleEACManage.getInstance().setAppEACEnable(EacDemoActivity.this, enable);
                updateAllStatusDelay();
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
                binding.switchEacEnable.setChecked(enable);
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
                binding.switchEacSupport.setChecked(support);
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

    private void updateAllStatusDelay() {
        RxTimerUtil.timer(UPDATE_EAC_STATUE_DELAY, TimeUnit.MILLISECONDS, new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(@NonNull Long aLong) {
                updateAllStatus();
            }
        });
    }

    private void appRotation() {
        new AlertDialog.Builder(this)
                .setTitle("App Rotation")
                .setItems(rotationItemArray, (dialog, which) -> {
                    int orientation = getCurrentRotation();
                    switch (which) {
                        case 0:
                            orientation = computeNewRotation(getCurrentRotation(), 0);
                            break;
                        case 1:
                            orientation = computeNewRotation(getCurrentRotation(), 90);
                            break;
                        case 2:
                            orientation = computeNewRotation(getCurrentRotation(), 180);
                            break;
                        case 3:
                            orientation = computeNewRotation(getCurrentRotation(), 270);
                            break;
                    }
                    dialog.dismiss();
                    RotationUtils.setRequestedOrientation(EacDemoActivity.this,
                            orientation, false, RotationUtils.ROTATE_BY_APP);
                }).show();
    }

    private void systemRotation() {
        new AlertDialog.Builder(this)
                .setTitle("System Rotation")
                .setItems(rotationItemArray, (dialog, which) -> {
                    int orientation = getCurrentRotation();
                    switch (which) {
                        case 0:
                            orientation = computeNewRotation(getCurrentRotation(), 0);
                            break;
                        case 1:
                            orientation = computeNewRotation(getCurrentRotation(), 90);
                            break;
                        case 2:
                            orientation = computeNewRotation(getCurrentRotation(), 180);
                            break;
                        case 3:
                            orientation = computeNewRotation(getCurrentRotation(), 270);
                            break;
                    }
                    dialog.dismiss();
                    RotationUtils.setRequestedOrientation(EacDemoActivity.this,
                            orientation, true, RotationUtils.ROTATE_BY_APP);
                }).show();
    }

    private int getCurrentRotation() {
        return DeviceUtils.getScreenOrientation(this);
    }

    private int computeNewRotation(int currentOrientation, int rotationOperation) {
        switch (rotationOperation) {
            case 0:
                return currentOrientation;
            case 90:
                if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }
            case 270:
                if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                } else {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
            case 180:
                if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else {
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
            default:
                assert (false);
                return currentOrientation;
        }
    }

}