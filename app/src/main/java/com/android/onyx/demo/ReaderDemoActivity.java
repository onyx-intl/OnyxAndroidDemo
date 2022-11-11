package com.android.onyx.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;

import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityReaderDemoBinding;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.ServiceUtils;
import com.onyx.android.sdk.utils.StringUtils;

import java.io.File;


/**
 * Created by Administrator on 2018/4/25 17:23.
 */
public class ReaderDemoActivity extends Activity {

    private static final String READER_PROVIDER = "content://com.onyx.content.database.ContentProvider/Metadata";

    private ActivityReaderDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reader_demo);
        binding.setActivityReader(this);
    }

    public void btn_open(View view) {
        if (!FilePathValidation()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        ComponentName componentName = new ComponentName("com.onyx.kreader", "com.onyx.android.sdk.readerview.service.ReaderService");
        intent.setComponent(componentName);
        intent.setData(FileProvider.getUriForFile(this,
                getPackageName() + ".onyx.fileprovider",
                new File(binding.etFile.getText().toString())));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ComponentName result = ServiceUtils.startServiceSafely(this, intent);
        if (result == null) {
            Toast.makeText(getApplicationContext(), "Service does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    public void btn_query_progress(View view) {
        if (!FilePathValidation()) {
            return;
        }
        String progress = queryByPath(binding.etFile.getText().toString());
        if (!StringUtils.isNullOrEmpty(progress)) {
            binding.textViewProgress.setText(getString(R.string.reading_progress, progress));
        } else {
            Toast.makeText(getApplicationContext(), R.string.query_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void btn_delete_reader_data(View view) {
        if (!FilePathValidation()) {
            return;
        }
        try {
            //Handwritten notes have a cache, you need to restart Reader
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses("com.onyx.kreader");
            Toast.makeText(this, R.string.delete_reader_data_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.delete_reader_data_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public String queryByPath(String path) {
        Cursor cursor = null;
        String progress = "";
        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.parse(READER_PROVIDER);
            String md5 = FileUtils.computeMD5(new File(path));
            cursor = resolver.query(uri, new String[]{"progress"}, "hashTag = ? or nativeAbsolutePath = ?", new String[]{md5, path}, null);
            if (cursor != null && cursor.moveToFirst()) {
                progress = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return progress;
    }

    private boolean FilePathValidation() {
        String filePath = binding.etFile.getText().toString();
        if ("".equals(filePath)) {
            Toast.makeText(this, R.string.enter_book_path, Toast.LENGTH_SHORT).show();
            return false;
        }
        File f = new File(filePath);
        if (!f.exists()) {
            Toast.makeText(this, R.string.invalid_path, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
