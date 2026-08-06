package com.onyx.daydreamdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.dreams.DreamService;
import android.text.TextUtils;
import android.util.Size;
import android.widget.ImageView;

import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.daydreamdemo.utils.ScreenUtils;

import java.util.Random;

public class ImageDayDream {
    private static final String ACTION_REFRESH = "com.onyx.daydreamdemo.action.refresh";

    private DreamService service;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private BroadcastReceiver refreshReceiver;

    private Paint paint = new Paint();

    private WakeLockHolder wakeLockHolder = new WakeLockHolder();

    public ImageDayDream(DreamService service) {
        this.service = service;

        alarmManager = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(service, 0, new Intent(ACTION_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT);

        paint.setColor(Color.BLACK);
        paint.setTextSize(50.0f);
    }

    public int getContentViewId() {
        return R.layout.layout_image_daydream;
    }

    public void onDreamingStarted() {
        registerRefreshBroadcast();
        startBitmapAnimation();
    }

    public void onDreamingStopped() {
        alarmManager.cancel(pendingIntent);
        unregisterRefreshBroadcast();
    }

    private void startBitmapAnimation() {
        try {
            // acquire wakelock from WakeLockHolder to prevent device from idle
            wakeLockHolder.acquireWakeLock(service, WakeLockHolder.FULL_FLAGS, getClass().getSimpleName());
            showNextBitmap();

            // use alarm manager to wake up device from idle/standby
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 5 * 1000, pendingIntent);
        } finally {
            wakeLockHolder.releaseWakeLock();
        }
    }

    private void showNextBitmap() {
        ImageView imageView = service.findViewById(R.id.imageView_screensaver);

        Size size = ScreenUtils.getScreenSize(service);
        Bitmap bmp = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE);

        String text = "Hello, Daydream!";
        float textWidth = paint.measureText(text);

        Random random = new Random();
        int x = random.nextInt(size.getWidth() - (int)textWidth);
        int y = random.nextInt(size.getHeight() - (int)paint.getTextSize());

        Canvas canvas = new Canvas(bmp);
        canvas.drawText(text, x, y, paint);

        imageView.setImageBitmap(bmp);
    }

    private void registerRefreshBroadcast() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.isEmpty(action) || !action.equals(ACTION_REFRESH)) {
                    return;
                }

                startBitmapAnimation();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REFRESH);
        service.registerReceiver(refreshReceiver, intentFilter);
    }

    private void unregisterRefreshBroadcast() {
        service.unregisterReceiver(refreshReceiver);
    }
}
