package com.android.onyx.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.utils.BitmapUtils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScreensaverActivity extends AppCompatActivity {
    @Bind(R.id.et_image)
    EditText etImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        ButterKnife.bind(this);
    }

    /**
     * set up screensaver steps:<br/>
     * 1. save pic under directory "/data/local/assets/images"<br/>
     * 2. file names format "standby-{num}.png", num starts from 1<br/>
     * 3. send broadcast with action "update_standby_pic"<br/>
     * PS.pic's width and height better matches the device's height and width, <br/>
     *    for example device's size is 2200x1650, pic's size should be 1650x2200, with upside towards left, <br/>
     *    and pic's file format should be png.
     */
    @OnClick(R.id.btn_set)
    public void setScreensaver() {
        String filePath = etImage.getText().toString();
        if ( "".equals(filePath) ) {
            Toast.makeText(this,R.string.enter_pic_path, Toast.LENGTH_SHORT).show();
            return;
        }
        File f = new File(filePath);
        if ( !f.exists() ) {
            Toast.makeText(this,R.string.invalid_path, Toast.LENGTH_SHORT).show();
            return;
        }

        String path = etImage.getText().toString();
        Bitmap temp = BitmapFactory.decodeFile(path);
        if (temp.getHeight() > temp.getWidth()) {
            temp = BitmapUtils.rotateBmp(temp, 270);
        }

        Point p = getScreenResolution(getApplicationContext());
        int h = p.y;
        int w = p.x;
        if (temp.getWidth() != h || temp.getHeight() != w) {
            temp = Bitmap.createScaledBitmap(temp, h, w, true);
        }
        //save file to target directory
        boolean success = false;
        String targetDir = "/data/local/assets/images";
        String targetPathString = targetDir + File.separator + "standby-1.png";//file name format: standby-{num}.png, num starts from 1
        if (path.contains("bmp")) {
            success = BitmapUtils.saveBitmapToFile(temp, targetDir, targetPathString, true);
        } else if (path.contains("png")) {
            success = BitmapUtils.savePngToFile(temp, targetDir, targetPathString, true);
        }
        //send broadcast
        if (success) {
            Intent intent = new Intent("update_standby_pic");
            sendBroadcast(intent);
        }
        Toast.makeText(this, success ? R.string.set_up_success : R.string.set_up_success, Toast.LENGTH_SHORT).show();
    }

    public static Point getScreenResolution(final Context context) {
        WindowManager w = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        if (Build.VERSION.SDK_INT < 17) {
            try {
                widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        }
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                widthPixels = realSize.x;
                heightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }
        return new Point(widthPixels, heightPixels);
    }
}
