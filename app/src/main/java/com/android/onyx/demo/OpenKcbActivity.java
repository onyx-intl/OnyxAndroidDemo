package com.android.onyx.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.utils.JSONUtils;
import com.onyx.android.sdk.utils.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @Bind(R.id.et_storage_jump_path)
    EditText etStorageJumpPath;

    @Bind(R.id.rg_note)
    RadioGroup rgNoteShop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_kcb);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_open_library)
    public void openLibrary() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_HOME));
    }

    @OnClick(R.id.btn_open_shop)
    public void openShop() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_SHOP));
    }

    @OnClick(R.id.btn_open_note)
    public void openNote() {
        openModule(setNoteJumpPath(new TabIntentData().setAction(TabAction.OPEN_NOTE)));
    }

    @OnClick(R.id.btn_open_storage)
    public void openStorage() {
        TabIntentData data = new TabIntentData().setAction(TabAction.OPEN_STORAGE);
        String jumpPath = etStorageJumpPath.getText().toString();
        if (!StringUtils.isNullOrEmpty(jumpPath)) {
            data.setJumpPath(jumpPath);
        }
        openModule(data);
    }

    @OnClick(R.id.btn_open_apps)
    public void openApps() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_APPS));
    }

    @OnClick(R.id.btn_open_setting)
    public void openSetting() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_SETTING));
    }

    @OnClick(R.id.btn_open_application_management)
    public void openApplicationManagement() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_APPLICATION_MANAGEMENT));
    }

    @OnClick(R.id.btn_open_account_management)
    public void openAccountManagement() {
        openModule(new TabIntentData().setAction(TabAction.OPEN_ACCOUNT_MANAGER));
    }

    private TabIntentData setNoteJumpPath(TabIntentData data) {
        String jumpPath = "";
        switch (rgNoteShop.getCheckedRadioButtonId()) {
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
        intent.setComponent(new ComponentName("com.onyx", "com.onyx.main.ui.MainActivity"));
        intent.putExtra("json", JSONUtils.toJson(data));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "open kcb failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
