package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;
import com.onyx.wereaddemo.utils.KeyValueBeanUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SystemSettingsDemoActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = SystemSettingsDemoActivity.class.getSimpleName();

    @Bind(R.id.tv_screen_off)
    public TextView tvScreenOff;

    @Bind(R.id.spinner_screen_off)
    public Spinner screenOffSpinner;

    @Bind(R.id.tv_power_off)
    public TextView tvPowerOff;

    @Bind(R.id.spinner_power_off)
    public Spinner powerOffSpinner;

    private ArrayAdapter<KeyValueBean> screenOffAdapter = null;
    private KeyValueBean[] screenOffArr;

    private ArrayAdapter<KeyValueBean> powerOffAdapter = null;
    private KeyValueBean[] powerOffArr;

    @Bind(R.id.checkbox_adb)
    public CheckBox adbCheckbox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_settings_demo);
        ButterKnife.bind(this);

        initResourceData();
        initData();
        screenOffAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                screenOffArr
        );
        screenOffSpinner.setAdapter(screenOffAdapter);
        screenOffSpinner.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getScreenOffTimeout()), screenOffArr));
        screenOffSpinner.setOnItemSelectedListener(this);

        powerOffAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                powerOffArr
        );
        powerOffSpinner.setAdapter(powerOffAdapter);
        powerOffSpinner.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getPowerOffTimeout()), powerOffArr));
        powerOffSpinner.setOnItemSelectedListener(this);

        adbCheckbox.setChecked(OnyxSdk.getInstance().isEnableADB());
        adbCheckbox.setOnCheckedChangeListener(this);
    }

    private void initData() {
        updateUiData();
    }

    private void updateUiData() {
        int screenOffTimeout = OnyxSdk.getInstance().getScreenOffTimeout();
        tvScreenOff.setText(KeyValueBeanUtils.getValue(String.valueOf(screenOffTimeout), screenOffArr));

        int powerOffTimeout = OnyxSdk.getInstance().getPowerOffTimeout();
        tvPowerOff.setText(KeyValueBeanUtils.getValue(String.valueOf(powerOffTimeout), powerOffArr));
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
    }

}
