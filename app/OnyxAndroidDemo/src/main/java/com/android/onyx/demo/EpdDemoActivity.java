package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;


import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityEpdDemoBinding;
import com.onyx.android.sdk.api.device.EpdDeviceManager;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;


public class EpdDemoActivity extends AppCompatActivity {
    private ActivityEpdDemoBinding binding;
    private static final String TAG = EpdDemoActivity.class.getSimpleName();

    private boolean isFastMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_epd_demo);
        // set full update after how many partial update
        EpdDeviceManager.setGcInterval(5);
    }

    public void onClick(View v) {
        if (v.equals(binding.buttonPartialUpdate)) {
            updateTextView();
            EpdDeviceManager.applyWithGCIntervalWithoutRegal(binding.textview);
        } else if (v.equals(binding.buttonRegalPartial)) {
            updateTextView();
            EpdDeviceManager.applyWithGCIntervalWitRegal(binding.textview, true);
        } else if (v.equals(binding.buttonScreenRefresh)) {
            updateTextView();
            EpdController.repaintEveryThing(UpdateMode.GC);
        } else if (v.equals(binding.buttonEnterFastMode)) {
            isFastMode = true;
            EpdDeviceManager.enterAnimationUpdate(true);
        } else if (v.equals(binding.buttonQuitFastMode)) {
            EpdDeviceManager.exitAnimationUpdate(true);
            isFastMode = false;
        } else if (v.equals(binding.buttonEnterXMode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.ANIMATION_X, Integer.MAX_VALUE);
        } else if (v.equals(binding.buttonEnterA2Mode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.ANIMATION_QUALITY, Integer.MAX_VALUE);
        } else if (v.equals(binding.buttonEnterNormalMode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, false, true, UpdateMode.None, Integer.MAX_VALUE);
        } else if (v.equals(binding.buttonEnterDuMode)) {
            EpdController.clearAppScopeUpdate();
            EpdController.applyAppScopeUpdate(TAG, true, true, UpdateMode.DU_QUALITY, Integer.MAX_VALUE);
        }
    }

    private void updateTextView() {
        StringBuilder sb = new StringBuilder();
        sb.append(binding.textview.getText());
        sb.append("hello world!");
        binding.textview.setText(sb.toString());
    }
}
