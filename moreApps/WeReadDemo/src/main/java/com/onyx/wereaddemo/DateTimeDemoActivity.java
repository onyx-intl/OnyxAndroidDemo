package com.onyx.wereaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.onyx.android.sdk.api.utils.StringUtils;
import com.onyx.android.sdk.utils.DateTimeUtil;
import com.onyx.android.sdk.utils.RxTimerUtil;
import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;
import com.onyx.wereaddemo.databinding.ActivityDatetimeDemoBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeDemoActivity extends AppCompatActivity {

    private static final String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD_HHMMSS = new SimpleDateFormat(DATE_PATTERN_YYYYMMDD_HHMMSS, Locale.getDefault());

    private ActivityDatetimeDemoBinding binding;

    private RxTimerUtil.TimerObserver timerObserver;
    private boolean initEditDateTime = false;

    private ArrayAdapter<KeyValueBean> timezoneAdapter = null;
    private KeyValueBean[] timezoneArr;

    private BroadcastReceiver broadcastReceiver;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_datetime_demo);
        initView();
        initData();
        initTimezoneData();
        String timezoneId = TimeZone.getDefault().getID();
        int index = getTimeZoneIndex(timezoneId);
        timezoneAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                timezoneArr
        );
        binding.spinnerTimezone.setAdapter(timezoneAdapter);
        binding.spinnerTimezone.setSelection(index);
        binding.spinnerTimezone.setOnItemSelectedListener(timezoneListener);
        doRegisterReceiver();
    }

    private void initView() {
        binding.btnDateTimeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDateTimeSetting();
            }
        });
        binding.btnAutoTimeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoTimeToggle();
            }
        });
        binding.btn1224Toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeFormatToggle();
            }
        });
    }

    private void initData() {
        updateTime();
        updateIsAutoTime();
        updateIs24Hour();
        updateTimezone();
    }

    public void onClickDateTimeSetting() {
        try {
            Date newDate = DATE_FORMAT_YYYYMMDD_HHMMSS.parse(binding.etDateTime.getText().toString());
            boolean succcess = OnyxSdk.getInstance().changeSystemTime(newDate.getTime());
            Toast.makeText(this, succcess ? "日期时间修改成功" : "日期时间格式错误", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "日期时间格式错误", Toast.LENGTH_LONG).show();
        }
    }


    public void autoTimeToggle() {
        boolean isAutoTimeEnabled = OnyxSdk.getInstance().isAutoTimeEnabled();
        if (isAutoTimeEnabled) {
            OnyxSdk.getInstance().setAutoTimeEnabled(false);
        } else {
            OnyxSdk.getInstance().setAutoTimeEnabled(true);
        }
        updateIsAutoTime();
    }

    private void updateIsAutoTime() {
        binding.autoTimeState.setText(OnyxSdk.getInstance().isAutoTimeEnabled() ? "是" : "否");
    }

    public void timeFormatToggle() {
        boolean is24HourEnabled = OnyxSdk.getInstance().is24HourEnabled();
        if (is24HourEnabled) {
            OnyxSdk.getInstance().set24HourEnabled(false);
        } else {
            OnyxSdk.getInstance().set24HourEnabled(true);
        }
        updateIs24Hour();
    }

    private void updateIs24Hour() {
        binding.state1224.setText(OnyxSdk.getInstance().is24HourEnabled() ? "是" : "否");
    }

    private void updateTimezone() {
        String timezoneId = TimeZone.getDefault().getID();
        String displayName = TimeZone.getDefault().getDisplayName();
        binding.timezone.setText(timezoneId + "-" + displayName);
    }

    private void updateTime() {
        timerObserver = new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                binding.tvCurrentDatetime.setText(DateTimeUtil.formatDate(GregorianCalendar.getInstance().getTime(), DATE_FORMAT_YYYYMMDD_HHMMSS));
                if (!initEditDateTime) {
                    initEditDateTime = true;
                    binding.etDateTime.setText(binding.tvCurrentDatetime.getText());
                }
            }
        };
        RxTimerUtil.timer(0, 1000, timerObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxTimerUtil.cancel(timerObserver);
        doUnregisterReceiver();
    }

    private void doRegisterReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Intent.ACTION_TIMEZONE_CHANGED:
                        updateTimezone();
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void doUnregisterReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
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
