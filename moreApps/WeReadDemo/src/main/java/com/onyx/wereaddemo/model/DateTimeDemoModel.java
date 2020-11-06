package com.onyx.wereaddemo.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.onyx.android.sdk.api.utils.StringUtils;
import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;
import com.onyx.wereaddemo.databinding.ActivityDatetimeDemoBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeDemoModel extends BaseObservable {

    private static final String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD_HHMMSS = new SimpleDateFormat(DATE_PATTERN_YYYYMMDD_HHMMSS, Locale.getDefault());

    private Context mContext;
    public ObservableField<String> currentDatetime = new ObservableField();
    public ObservableField<String> etDateTime = new ObservableField();
    public ObservableBoolean isAutoTimeEnabled = new ObservableBoolean();
    public ObservableBoolean is24HoursEnabled = new ObservableBoolean();
    public ObservableField<String> currentTimezone = new ObservableField<>();

    private ActivityDatetimeDemoBinding binding;

    private ArrayAdapter<KeyValueBean> timezoneAdapter = null;
    private KeyValueBean[] timezoneArr;

    private AdapterView.OnItemSelectedListener timezoneListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            KeyValueBean bean = (KeyValueBean)binding.spinnerTimezone.getSelectedItem();
            String key = bean.getKey();
            String value = bean.getValue();
            if (StringUtils.isNullOrEmpty(key)) {
                return;
            }
            OnyxSdk.getInstance().changeSystemTimeZone(key);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public DateTimeDemoModel(Context context, ActivityDatetimeDemoBinding binding) {
        mContext = context;
        this.binding = binding;

        initTimezoneData();
        initView();
        updateData();
    }

    private void initView() {

        String timezoneId = TimeZone.getDefault().getID();
        int index = getTimeZoneIndex(timezoneId);
        timezoneAdapter = new ArrayAdapter<KeyValueBean>(
                mContext,
                android.R.layout.simple_spinner_item,
                timezoneArr
        );
        binding.spinnerTimezone.setAdapter(timezoneAdapter);
        binding.spinnerTimezone.setSelection(index);
        binding.spinnerTimezone.setOnItemSelectedListener(timezoneListener);
    }

    public void updateData() {
        isAutoTimeEnabled.set(OnyxSdk.getInstance().isAutoTimeEnabled());
        is24HoursEnabled.set(OnyxSdk.getInstance().is24HourEnabled());
        updateTimezone();
    }

    public void updateTimezone() {
        String timezoneId = TimeZone.getDefault().getID();
        String displayName = TimeZone.getDefault().getDisplayName();
        currentTimezone.set(timezoneId + "-" + displayName);
    }

    public void onClickDateTimeSetting() {
        try {
            Date newDate = DATE_FORMAT_YYYYMMDD_HHMMSS.parse(etDateTime.get());
            boolean succcess = OnyxSdk.getInstance().changeSystemTime(newDate.getTime());
            Toast.makeText(mContext, succcess ? "日期时间修改成功" : "日期时间格式错误", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "日期时间格式错误", Toast.LENGTH_LONG).show();
        }
    }

    public void autoTimeToggle() {
        boolean isAutoTimeEnabled = OnyxSdk.getInstance().isAutoTimeEnabled();
        if (isAutoTimeEnabled) {
            OnyxSdk.getInstance().setAutoTimeEnabled(false);
        } else {
            OnyxSdk.getInstance().setAutoTimeEnabled(true);
        }
        updateData();
    }

    public void click24hoursToggle() {
        boolean is24HourEnabled = OnyxSdk.getInstance().is24HourEnabled();
        if (is24HourEnabled) {
            OnyxSdk.getInstance().set24HourEnabled(false);
        } else {
            OnyxSdk.getInstance().set24HourEnabled(true);
        }
        updateData();
    }

    private int getTimeZoneIndex(String timezoneId) {
        if (timezoneArr == null || StringUtils.isNullOrEmpty(timezoneId)) {
            return 0;
        }
        for (int i = 0; i < timezoneArr.length; i++) {
            if (timezoneId.equals(timezoneArr[i].getKey())) {
                return i;
            }
        }
        return 0;
    }

    private void initTimezoneData() {
        timezoneArr = new KeyValueBean[]{
                KeyValueBean.create("", ""),
                KeyValueBean.create("Asia/Shanghai", "中国标准时间 (北京)"),
                KeyValueBean.create("Asia/Hong_Kong", "香港时间 (香港)"),
                KeyValueBean.create("Asia/Taipei", "台北时间 (台北)"),
                KeyValueBean.create("America/Los_Angeles", "美国太平洋时间 (洛杉矶)"),
                KeyValueBean.create("Europe/London", "格林尼治标准时间 (伦敦)"),
                KeyValueBean.create("Asia/Seoul", "首尔"),
                KeyValueBean.create("Asia/Tokyo", "日本时间 (东京)")
        };
    }

}
