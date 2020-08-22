package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.sdk.utils.DateTimeUtil;
import com.onyx.android.sdk.utils.RxTimerUtil;
import com.onyx.weread.api.OnyxSdk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    private RxTimerUtil.TimerObserver timerObserver;
    private boolean initEditDateTime = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datetime_demo);
        ButterKnife.bind(this);
        updateTime();
    }

    @OnClick(R.id.btn_date_time_setting)
    public void onClickDateTimeSetting() {
        try {
            Date newDate = DATE_FORMAT_YYYYMMDD_HHMMSS.parse(etDateTime.getText().toString());
            OnyxSdk.changeSystemTime(this, newDate.getTime());
            Toast.makeText(this, "日期时间修改成功", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "日期时间格式错误", Toast.LENGTH_LONG).show();
        }
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
}
