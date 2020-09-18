package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioGroup;

import com.onyx.android.sdk.api.device.epd.UpdateOption;
import com.onyx.weread.api.OnyxSdk;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RefreshModeDemoActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = RefreshModeDemoActivity.class.getSimpleName();

    @Bind(R.id.rg_refresh_mode)
    public RadioGroup rgRefreshmode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_mode_demo);
        ButterKnife.bind(this);

        initData();
        rgRefreshmode.setOnCheckedChangeListener(this);
    }

    private void initData() {
        UpdateOption updateOption = OnyxSdk.getInstance().getSystemRefreshMode();
        rgRefreshmode.check(getRadioButtonIdByUpdateOption(updateOption));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.rb_normal:
                OnyxSdk.getInstance().setSystemRefreshMode(UpdateOption.NORMAL);
                break;
            case R.id.rb_fast_quality:
                OnyxSdk.getInstance().setSystemRefreshMode(UpdateOption.FAST_QUALITY);
                break;
            case R.id.rb_fast:
                OnyxSdk.getInstance().setSystemRefreshMode(UpdateOption.FAST);
                break;
            case R.id.rb_fast_x:
                OnyxSdk.getInstance().setSystemRefreshMode(UpdateOption.FAST_X);
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
