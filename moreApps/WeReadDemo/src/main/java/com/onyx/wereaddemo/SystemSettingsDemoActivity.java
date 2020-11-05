package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;
import com.onyx.wereaddemo.databinding.ActivitySystemSettingsDemoBinding;
import com.onyx.wereaddemo.utils.KeyValueBeanUtils;

public class SystemSettingsDemoActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = SystemSettingsDemoActivity.class.getSimpleName();

    private ArrayAdapter<KeyValueBean> screenOffAdapter = null;
    private KeyValueBean[] screenOffArr;

    private ArrayAdapter<KeyValueBean> powerOffAdapter = null;
    private KeyValueBean[] powerOffArr;

    private ArrayAdapter<KeyValueBean> workLowpowerWakelockTimeoutAdapter = null;
    private KeyValueBean[] workLowpowerWakelockTimeoutArr;


    private ActivitySystemSettingsDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_system_settings_demo);

        initResourceData();
        initView();
        initData();
    }

    private void initView() {
        screenOffAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                screenOffArr
        );

        binding.spinnerScreenOff.setAdapter(screenOffAdapter);
        binding.spinnerScreenOff.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getScreenOffTimeout()), screenOffArr));
        binding.spinnerScreenOff.setOnItemSelectedListener(this);

        powerOffAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                powerOffArr
        );

        binding.spinnerPowerOff.setAdapter(powerOffAdapter);
        binding.spinnerPowerOff.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getPowerOffTimeout()), powerOffArr));
        binding.spinnerPowerOff.setOnItemSelectedListener(this);

        workLowpowerWakelockTimeoutAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                workLowpowerWakelockTimeoutArr
        );

        binding.spinnerWorkLowpowerWakelockTimeout.setAdapter(workLowpowerWakelockTimeoutAdapter);
        binding.spinnerWorkLowpowerWakelockTimeout.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getWorkLowPowerWakelockTimeout()), workLowpowerWakelockTimeoutArr));
        binding.spinnerWorkLowpowerWakelockTimeout.setOnItemSelectedListener(this);

        binding.checkboxAdb.setChecked(OnyxSdk.getInstance().isEnableADB());
        binding.checkboxAdb.setOnCheckedChangeListener(this);
    }

    private void initData() {
        updateUiData();
    }

    private void updateUiData() {
        int screenOffTimeout = OnyxSdk.getInstance().getScreenOffTimeout();
        binding.tvScreenOff.setText(KeyValueBeanUtils.getValue(String.valueOf(screenOffTimeout), screenOffArr));

        int powerOffTimeout = OnyxSdk.getInstance().getPowerOffTimeout();
        binding.tvPowerOff.setText(KeyValueBeanUtils.getValue(String.valueOf(powerOffTimeout), powerOffArr));

        int workLowpowerWakelockTimeout = OnyxSdk.getInstance().getWorkLowPowerWakelockTimeout();
        binding.tvWorkLowpowerWakelockTimeout.setText(KeyValueBeanUtils.getValue(String.valueOf(workLowpowerWakelockTimeout), workLowpowerWakelockTimeoutArr));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        KeyValueBean keyValueBean = (KeyValueBean) adapterView.getSelectedItem();
        String key = keyValueBean.getKey();;
        String value = keyValueBean.getValue();
        switch (adapterView.getId()) {
            case R.id.spinner_screen_off:
                android.util.Log.e(TAG, "spinner_screen_off");
                OnyxSdk.getInstance().setScreenOffTimeout(Integer.valueOf(key));
                break;
            case R.id.spinner_power_off:
                android.util.Log.e(TAG, "spinner_power_off");
                OnyxSdk.getInstance().setPowerOffTimeout(Integer.valueOf(key));
                break;
            case R.id.spinner_work_lowpower_wakelock_timeout:
                android.util.Log.e(TAG, "spinner_work_lowpower_wakelock_timeout");
                OnyxSdk.getInstance().setWorkLowPowerWakelockTimeout(Integer.valueOf(key));
                break;
        }
        updateUiData();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.checkbox_adb:
                OnyxSdk.getInstance().setADBEnabled(isChecked);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initResourceData() {
        screenOffArr = new KeyValueBean[]{
                KeyValueBean.create("180000", "3 分钟"),
                KeyValueBean.create("300000", "5 分钟"),
                KeyValueBean.create("600000", "10 分钟"),
                KeyValueBean.create("1800000", "30 分钟"),
                KeyValueBean.create(String.valueOf(Integer.MAX_VALUE), "永不")
        };

        powerOffArr = new KeyValueBean[]{
                KeyValueBean.create("900000", "15 分钟"),
                KeyValueBean.create("1800000", "30 分钟"),
                KeyValueBean.create("3600000", "1 小时"),
                KeyValueBean.create("43200000", "12 小时"),
                KeyValueBean.create("86400000", "1 天"),
                KeyValueBean.create("172800000", "2 天"),
                KeyValueBean.create(String.valueOf(Integer.MAX_VALUE), "永不")
        };

        workLowpowerWakelockTimeoutArr = new KeyValueBean[]{
                KeyValueBean.create("-1", "立刻"),
                KeyValueBean.create("300000", "5 分钟"),
                KeyValueBean.create("600000", "10 分钟"),
                KeyValueBean.create("1800000", "30 分钟"),
                KeyValueBean.create("3600000", "1 小时"),
                KeyValueBean.create("43200000", "12 小时"),
                KeyValueBean.create(String.valueOf(Integer.MAX_VALUE), "永不")
        };
    }

}
