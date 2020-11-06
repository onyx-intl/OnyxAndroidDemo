package com.onyx.wereaddemo.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;
import com.onyx.wereaddemo.databinding.ActivitySystemSettingsDemoBinding;
import com.onyx.wereaddemo.utils.KeyValueBeanUtils;

public class SystemSettingsDemoModel extends BaseObservable {

    private static final String TAG = SystemSettingsDemoModel.class.getSimpleName();

    public ObservableBoolean adbEnable = new ObservableBoolean();
    public ObservableField<String> screenOffTimeout = new ObservableField<>();
    public ObservableField<String> powerOffTimeout = new ObservableField<>();
    public ObservableField<String> workLowpowerWakelockTimeoutTimeout = new ObservableField<>();
    public ObservableBoolean swipeFromLeftEnable = new ObservableBoolean();

    private Context mContext;
    private ActivitySystemSettingsDemoBinding binding;

    private ArrayAdapter<KeyValueBean> screenOffAdapter = null;
    private KeyValueBean[] screenOffArr;

    private ArrayAdapter<KeyValueBean> powerOffAdapter = null;
    private KeyValueBean[] powerOffArr;

    private ArrayAdapter<KeyValueBean> workLowpowerWakelockTimeoutAdapter = null;
    private KeyValueBean[] workLowpowerWakelockTimeoutArr;


    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (compoundButton == binding.checkboxAdb) {
                OnyxSdk.getInstance().setADBEnabled(isChecked);
            } else if (compoundButton == binding.checkboxSwipeFromLeft) {
                OnyxSdk.getInstance().setSwipeFromLeftEnable(isChecked);
            }
        }
    };

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            KeyValueBean keyValueBean = (KeyValueBean) adapterView.getSelectedItem();
            String key = keyValueBean.getKey();;
            String value = keyValueBean.getValue();

            if (adapterView == binding.spinnerScreenOff) {
                android.util.Log.e(TAG, "spinner_screen_off");
                OnyxSdk.getInstance().setScreenOffTimeout(Integer.valueOf(key));
            } else if (adapterView == binding.spinnerPowerOff) {
                android.util.Log.e(TAG, "spinner_power_off");
                OnyxSdk.getInstance().setPowerOffTimeout(Integer.valueOf(key));
            } else if (adapterView == binding.spinnerWorkLowpowerWakelockTimeout) {
                android.util.Log.e(TAG, "spinner_work_lowpower_wakelock_timeout");
                OnyxSdk.getInstance().setWorkLowPowerWakelockTimeout(Integer.valueOf(key));
            }

            updateData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    public SystemSettingsDemoModel(Context context, ActivitySystemSettingsDemoBinding binding) {
        mContext = context;
        this.binding = binding;

        initResourceData();
        initView();
        initData();
    }

    private void initView() {
        screenOffAdapter = new ArrayAdapter<KeyValueBean>(
                mContext,
                android.R.layout.simple_spinner_item,
                screenOffArr
        );

        binding.spinnerScreenOff.setAdapter(screenOffAdapter);
        binding.spinnerScreenOff.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getScreenOffTimeout()), screenOffArr));
        binding.spinnerScreenOff.setOnItemSelectedListener(onItemSelectedListener);

        powerOffAdapter = new ArrayAdapter<KeyValueBean>(
                mContext,
                android.R.layout.simple_spinner_item,
                powerOffArr
        );

        binding.spinnerPowerOff.setAdapter(powerOffAdapter);
        binding.spinnerPowerOff.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getPowerOffTimeout()), powerOffArr));
        binding.spinnerPowerOff.setOnItemSelectedListener(onItemSelectedListener);

        workLowpowerWakelockTimeoutAdapter = new ArrayAdapter<KeyValueBean>(
                mContext,
                android.R.layout.simple_spinner_item,
                workLowpowerWakelockTimeoutArr
        );

        binding.spinnerWorkLowpowerWakelockTimeout.setAdapter(workLowpowerWakelockTimeoutAdapter);
        binding.spinnerWorkLowpowerWakelockTimeout.setSelection(KeyValueBeanUtils.getIndex(String.valueOf(OnyxSdk.getInstance().getWorkLowPowerWakelockTimeout()), workLowpowerWakelockTimeoutArr));
        binding.spinnerWorkLowpowerWakelockTimeout.setOnItemSelectedListener(onItemSelectedListener);

        binding.checkboxAdb.setOnCheckedChangeListener(checkedChangeListener);
        binding.checkboxSwipeFromLeft.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void initData() {
        updateData();
    }

    private void updateData() {
        screenOffTimeout.set(KeyValueBeanUtils.getValue(String.valueOf(OnyxSdk.getInstance().getScreenOffTimeout()), screenOffArr));
        powerOffTimeout.set(KeyValueBeanUtils.getValue(String.valueOf(OnyxSdk.getInstance().getPowerOffTimeout()), powerOffArr));
        workLowpowerWakelockTimeoutTimeout.set(KeyValueBeanUtils.getValue(String.valueOf(OnyxSdk.getInstance().getWorkLowPowerWakelockTimeout()), workLowpowerWakelockTimeoutArr));

        adbEnable.set(OnyxSdk.getInstance().isEnableADB());
        swipeFromLeftEnable.set(OnyxSdk.getInstance().isSwipeFromLeftEnable());
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
