package com.android.onyx.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityOpenKcbBinding;
import com.onyx.android.sdk.utils.JSONUtils;
import com.onyx.android.sdk.utils.StringUtils;


/**
 * Created by zhilun on 2019/4/4.
 */
public class OpenKcbActivity extends AppCompatActivity {

    public enum TabAction {
        NOTHING, OPEN_HOME, OPEN_STORAGE, OPEN_APPLICATION_MANAGEMENT, OPEN_SETTING, OPEN_APPS, OPEN_NOTE, OPEN_ACCOUNT_MANAGER,
        OPEN_SHOP
    }

    public enum NoteRouter {
        SEARCH, BACKUP, COMMON_SETTING, AI_SETTING;
    }

    public static class TabIntentData {
        public String jumpPath;
        public TabAction action = TabAction.NOTHING;

        public TabIntentData setJumpPath(String jumpPath) {
            this.jumpPath = jumpPath;
            return this;
        }

        public TabIntentData setAction(TabAction action) {
            this.action = action;
            return this;
        }
    }

    private ActivityOpenKcbBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_kcb);
        binding.setActivityOpenKcb(this);
    }

    public void openLibrary(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_HOME));
    }

    public void openShop(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_SHOP));
    }

    public void openNote(View view) {
        openModule(setNoteJumpPath(new TabIntentData().setAction(TabAction.OPEN_NOTE)));
    }

    public void openStorage(View view) {
        TabIntentData data = new TabIntentData().setAction(TabAction.OPEN_STORAGE);
        String jumpPath = binding.etStorageJumpPath.getText().toString();
        if (!StringUtils.isNullOrEmpty(jumpPath)) {
            data.setJumpPath(jumpPath);
        }
        openModule(data);
    }

    public void openApps(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_APPS));
    }

    public void openSetting(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_SETTING));
    }

    public void openApplicationManagement(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_APPLICATION_MANAGEMENT));
    }

    public void openAccountManagement(View view) {
        openModule(new TabIntentData().setAction(TabAction.OPEN_ACCOUNT_MANAGER));
    }

    private TabIntentData setNoteJumpPath(TabIntentData data) {
        String jumpPath = "";
        switch (binding.rgNote.getCheckedRadioButtonId()) {
            case R.id.rb_search:
                jumpPath = NoteRouter.SEARCH.toString();
                break;
            case R.id.rb_backup:
                jumpPath = NoteRouter.BACKUP.toString();
                break;
            case R.id.rb_common_setting:
                jumpPath = NoteRouter.COMMON_SETTING.toString();
                break;
            case R.id.rb_ai_setting:
                jumpPath = NoteRouter.AI_SETTING.toString();
                break;
        }
        return data.setJumpPath(jumpPath);
    }

    private void openModule(TabIntentData data) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.onyx", "com.onyx.reader.main.ui.MainActivity"));
        intent.putExtra("json", JSONUtils.toJson(data));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "open kcb failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
