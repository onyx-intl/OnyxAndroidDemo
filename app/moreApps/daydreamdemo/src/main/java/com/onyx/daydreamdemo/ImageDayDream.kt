package com.onyx.daydreamdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.dreams.DreamService
import android.text.TextUtils
import android.widget.ImageView
import com.onyx.android.sdk.common.request.WakeLockHolder
import com.onyx.daydreamdemo.utils.ScreenUtils
import java.util.Random

class ImageDayDream(private val service: DreamService) {
    private val alarmManager: AlarmManager = service.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        service, 0, Intent(ACTION_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT
    )

    private var refreshReceiver: BroadcastReceiver? = null

    private val paint = Paint()

    private val wakeLockHolder = WakeLockHolder()

    init {

        paint.color = Color.BLACK
        paint.textSize = 50.0f
    }

    val contentViewId: Int
        get() = R.layout.layout_image_daydream

    fun onDreamingStarted() {
        registerRefreshBroadcast()
        startBitmapAnimation()
    }

    fun onDreamingStopped() {
        alarmManager.cancel(pendingIntent)
        unregisterRefreshBroadcast()
    }

    private fun startBitmapAnimation() {
        try { // acquire wakelock from WakeLockHolder to prevent device from idle
            wakeLockHolder.acquireWakeLock(
                service, WakeLockHolder.FULL_FLAGS, javaClass.simpleName
            )
            showNextBitmap()

            // use alarm manager to wake up device from idle/standby
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, (5 * 1000).toLong(), pendingIntent)
        } finally {
            wakeLockHolder.releaseWakeLock()
        }
    }

    private fun showNextBitmap() {
        val imageView = service.findViewById<ImageView>(R.id.imageView_screensaver)

        val size = ScreenUtils.getScreenSize(service)
        val bmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)

        val text = "Hello, Daydream!"
        val textWidth = paint.measureText(text)

        val random = Random()
        val x = random.nextInt(size.width - textWidth.toInt())
        val y = random.nextInt(size.height - paint.textSize.toInt())

        val canvas = Canvas(bmp)
        canvas.drawText(text, x.toFloat(), y.toFloat(), paint)

        imageView.setImageBitmap(bmp)
    }

    private fun registerRefreshBroadcast() {
        refreshReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (TextUtils.isEmpty(action) || action != ACTION_REFRESH) {
                    return
                }

                startBitmapAnimation()
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_REFRESH)
        service.registerReceiver(refreshReceiver, intentFilter)
    }

    private fun unregisterRefreshBroadcast() {
        service.unregisterReceiver(refreshReceiver)
    }

    companion object {
        private const val ACTION_REFRESH = "com.onyx.daydreamdemo.action.refresh"
    }
}
