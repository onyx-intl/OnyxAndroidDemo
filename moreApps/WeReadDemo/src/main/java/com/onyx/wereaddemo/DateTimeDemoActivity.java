package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.sdk.api.utils.StringUtils;
import com.onyx.android.sdk.utils.DateTimeUtil;
import com.onyx.android.sdk.utils.RxTimerUtil;
import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.data.KeyValueBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DateTimeDemoActivity extends AppCompatActivity {

    private static final String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD_HHMMSS = new SimpleDateFormat(DATE_PATTERN_YYYYMMDD_HHMMSS, Locale.getDefault());

    @Bind(R.id.tv_current_datetime)
    public TextView dateTimeTextView;

    @Bind(R.id.et_date_time)
    public EditText etDateTime;

    @Bind(R.id.auto_time_state)
    public TextView auto_time_state;

    @Bind(R.id.state_12_24)
    public TextView state_12_24;

    @Bind(R.id.timezone)
    public TextView timezone;

    @Bind(R.id.spinner_timezone)
    public Spinner timezoneSpinner;

    private RxTimerUtil.TimerObserver timerObserver;
    private boolean initEditDateTime = false;

    private ArrayAdapter<KeyValueBean> timezoneAdapter = null;
    private KeyValueBean[] timezoneArr;

    private AdapterView.OnItemSelectedListener timezoneListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            KeyValueBean bean = (KeyValueBean)timezoneSpinner.getSelectedItem();
            String key = bean.getKey();
            String value = bean.getValue();
            if (StringUtils.isNullOrEmpty(key)) {
                return;
            }
            OnyxSdk.getInstance().changeSystemTimeZone(key);
            updateTimezone();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datetime_demo);
        ButterKnife.bind(this);
        updateTime();
        updateIsAutoTime();
        updateIs24Hour();
        updateTimezone();

        initTimezoneData();
        String timezoneId = TimeZone.getDefault().getID();
        int index = getTimeZoneIndex(timezoneId);

        timezoneAdapter = new ArrayAdapter<KeyValueBean>(
                this,
                android.R.layout.simple_spinner_item,
                timezoneArr
        );

        timezoneSpinner.setAdapter(timezoneAdapter);
        timezoneSpinner.setSelection(index);
        timezoneSpinner.setOnItemSelectedListener(timezoneListener);

    }

    @OnClick(R.id.btn_date_time_setting)
    public void onClickDateTimeSetting() {
        try {
            Date newDate = DATE_FORMAT_YYYYMMDD_HHMMSS.parse(etDateTime.getText().toString());
            boolean succcess = OnyxSdk.getInstance().changeSystemTime(newDate.getTime());
            Toast.makeText(this, succcess ? "日期时间修改成功" : "日期时间格式错误", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "日期时间格式错误", Toast.LENGTH_LONG).show();
        }
    }


    @OnClick(R.id.btn_auto_time_toggle)
    public void aa() {
        boolean isAutoTimeEnabled = OnyxSdk.getInstance().isAutoTimeEnabled();
        if (isAutoTimeEnabled) {
            OnyxSdk.getInstance().setAutoTimeEnabled(false);
        } else {
            OnyxSdk.getInstance().setAutoTimeEnabled(true);
        }
        updateIsAutoTime();
    }

    private void updateIsAutoTime() {
        auto_time_state.setText(OnyxSdk.getInstance().isAutoTimeEnabled() ? "是" : "否");
    }

    @OnClick(R.id.btn_12_24_toggle)
    public void bb() {
        boolean is24HourEnabled = OnyxSdk.getInstance().is24HourEnabled();
        if (is24HourEnabled) {
            OnyxSdk.getInstance().set24HourEnabled(false);
        } else {
            OnyxSdk.getInstance().set24HourEnabled(true);
        }
        updateIs24Hour();
    }

    private void updateIs24Hour() {
        state_12_24.setText(OnyxSdk.getInstance().is24HourEnabled() ? "是" : "否");
    }

    private void updateTimezone() {
        String timezoneId = TimeZone.getDefault().getID();
        String displayName = TimeZone.getDefault().getDisplayName();
        timezone.setText(timezoneId + "-" + displayName);
    }

    private void updateTime() {
        timerObserver = new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                dateTimeTextView.setText(DateTimeUtil.formatDate(GregorianCalendar.getInstance().getTime(), DATE_FORMAT_YYYYMMDD_HHMMSS));
                if (!initEditDateTime) {
                    initEditDateTime = true;
                    etDateTime.setText(dateTimeTextView.getText());
                }
            }
        };
        RxTimerUtil.timer(0, 1000, timerObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxTimerUtil.cancel(timerObserver);
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
