package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.onyx.weread.api.OnyxSdk;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by seeksky on 2018/5/17.
 */

public class FactoryResetDemoActivity extends AppCompatActivity {

    private static final String TAG = FactoryResetDemoActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_reset_demo);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_factory_reset)
    public void onFactoryReset() {
        boolean flag = OnyxSdk.getInstance().doFactoryReset();
        Log.w(TAG, " factory reset flag : " + flag);
    }
}
