package com.onyx.wereaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.onyx.android.sdk.utils.DateTimeUtil;
import com.onyx.android.sdk.utils.RxTimerUtil;
import com.onyx.wereaddemo.databinding.ActivityDatetimeDemoBinding;
import com.onyx.wereaddemo.model.DateTimeDemoModel;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateTimeDemoActivity extends AppCompatActivity {

    private static final String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD_HHMMSS = new SimpleDateFormat(DATE_PATTERN_YYYYMMDD_HHMMSS, Locale.getDefault());

    private ActivityDatetimeDemoBinding binding;
    private DateTimeDemoModel dateTimeDemoModel;

    private RxTimerUtil.TimerObserver timerObserver;
    private boolean initEditDateTime = false;



    private BroadcastReceiver broadcastReceiver;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_datetime_demo);
        dateTimeDemoModel = new DateTimeDemoModel(this, binding);
        binding.setModel(dateTimeDemoModel);
        updateTime();
        doRegisterReceiver();
    }

    private void updateTime() {
        timerObserver = new RxTimerUtil.TimerObserver() {
            @Override
            public void onNext(Long aLong) {
                dateTimeDemoModel.currentDatetime.set(DateTimeUtil.formatDate(GregorianCalendar.getInstance().getTime(), DATE_FORMAT_YYYYMMDD_HHMMSS));
                if (!initEditDateTime) {
                    initEditDateTime = true;
                    dateTimeDemoModel.etDateTime.set(dateTimeDemoModel.currentDatetime.get());
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
                        dateTimeDemoModel.updateTimezone();
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
    

}
