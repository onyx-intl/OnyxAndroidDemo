package com.android.onyx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityBooxSettingBinding;
import com.onyx.android.sdk.api.device.GlobalContrastController;

public class BooxSettingsDemoActivity extends AppCompatActivity {

    private ActivityBooxSettingBinding binding;
    public ObservableBoolean isHighContrastEnabled = new ObservableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_boox_setting);
        binding.setActivity(this);
        updateData();
    }

    private void updateData() {
        isHighContrastEnabled.set(GlobalContrastController.isHighContrastEnabled());
    }

    /**
     * {@link GlobalContrastController#isHighContrastEnabled()}
     * {@link GlobalContrastController#setHighContrastEnabled(boolean)}
     * Please be careful not to call it directly during the initial lifecycle of the application when using it, as this may cause incorrect results.You can use {@link android.view.View#post(Runnable)} call it.
     */
    public void onHighContrastCheckedChanged(CompoundButton view, boolean isChecked) {
        GlobalContrastController.setHighContrastEnabled(isChecked);
        isHighContrastEnabled.set(isChecked);
    }
}