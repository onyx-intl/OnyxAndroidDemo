package com.android.onyx.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityRefreshModeDemoBinding;
import com.onyx.android.sdk.api.device.epd.UpdateOption;
import com.onyx.android.sdk.device.Device;


public class RefreshModeDemoActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = RefreshModeDemoActivity.class.getSimpleName();

    private ActivityRefreshModeDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refresh_mode_demo);

        initData();
        binding.rgRefreshMode.setOnCheckedChangeListener(this);
    }

    private void initData() {
        UpdateOption updateOption = Device.currentDevice().getSystemRefreshMode();
        binding.rgRefreshMode.check(getRadioButtonIdByUpdateOption(updateOption));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.rb_normal:
                Device.currentDevice().setSystemRefreshMode(UpdateOption.NORMAL);
                break;
            case R.id.rb_fast_quality:
                Device.currentDevice().setSystemRefreshMode(UpdateOption.FAST_QUALITY);
                break;
            case R.id.rb_fast:
                Device.currentDevice().setSystemRefreshMode(UpdateOption.FAST);
                break;
            case R.id.rb_fast_x:
                Device.currentDevice().setSystemRefreshMode(UpdateOption.FAST_X);
                break;
        }
    }

    public int getRadioButtonIdByUpdateOption(UpdateOption updateOption) {
        switch (updateOption) {
            case NORMAL:
                return R.id.rb_normal;
            case FAST_QUALITY:
                return R.id.rb_fast_quality;
            case FAST:
                return R.id.rb_fast;
            case FAST_X:
                return R.id.rb_fast_x;
        }
        return -1;
    }

}
