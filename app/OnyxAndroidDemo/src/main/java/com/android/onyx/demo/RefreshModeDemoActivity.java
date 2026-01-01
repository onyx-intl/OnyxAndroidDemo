package com.android.onyx.demo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
        UpdateOption updateOption = Device.currentDevice().getAppScopeRefreshMode();
        binding.rgRefreshMode.check(getRadioButtonIdByUpdateOption(updateOption));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.rb_normal) {
            Device.currentDevice().setAppScopeRefreshMode(UpdateOption.NORMAL);
        } else if (checkedId == R.id.rb_fast_quality) {
            Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST_QUALITY);
        } else if (checkedId == R.id.rb_regal) {
            Device.currentDevice().setAppScopeRefreshMode(UpdateOption.REGAL);
        } else if (checkedId == R.id.rb_fast) {
            Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST);
        } else if (checkedId == R.id.rb_fast_x) {
            Device.currentDevice().setAppScopeRefreshMode(UpdateOption.FAST_X);
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
            case REGAL:
                return R.id.rb_regal;
        }
        return -1;
    }

}
