package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.databinding.ActivityFactoryResetDemoBinding;

/**
 * Created by seeksky on 2018/5/17.
 */

public class FactoryResetDemoActivity extends AppCompatActivity {

    private static final String TAG = FactoryResetDemoActivity.class.getSimpleName();

    private ActivityFactoryResetDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_factory_reset_demo);
        initView();
    }

    private void initView() {
        binding.buttonFactoryReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag = OnyxSdk.getInstance().doFactoryReset();
                Log.w(TAG, " factory reset flag : " + flag);
            }
        });
    }
}
