package com.onyx.wereaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.onyx.weread.api.OnyxSdk;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsDemoActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String NEW_LINE = "\n";

    @Bind(R.id.tv_cold_values)
    public TextView tvColdValues;

    @Bind(R.id.tv_warm_values)
    public TextView tvWarmValues;

    @Bind(R.id.togglebutton_cold)
    public ToggleButton togglebuttonCold;

    @Bind(R.id.et_cold)
    public EditText et_cold;

    @Bind(R.id.btn_cold)
    public Button btn_cold;

    @Bind(R.id.togglebutton_warm)
    public ToggleButton togglebuttonWarm;

    @Bind(R.id.et_warm)
    public EditText et_warm;

    @Bind(R.id.btn_warm)
    public Button btn_warm;

    @Bind(R.id.et_contrast)
    public EditText et_contrast;

    @Bind(R.id.btn_contrast)
    public Button btn_contrast;

    @Bind(R.id.tv_infos)
    public TextView tvInfos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_demo);
        ButterKnife.bind(this);

        initData();
    }

    private void initData() {
        tvColdValues.setText(getColdValues());
        tvWarmValues.setText(getWarmValues());
        et_cold.setText(String.valueOf(getCurrentColdLightValue()));
        et_warm.setText(String.valueOf(getCurrentWarmLightValue()));
        et_contrast.setText(String.valueOf(OnyxSdk.getCurrentGlobalContrast()));

        refreshValues();
    }

    private void refreshValues() {
        togglebuttonCold.setOnCheckedChangeListener(null);
        togglebuttonWarm.setOnCheckedChangeListener(null);

        togglebuttonCold.setChecked(OnyxSdk.isColdLightOn(this));
        togglebuttonWarm.setChecked(OnyxSdk.isWarmLightOn(this));
        tvInfos.setText(getInfos());

        togglebuttonCold.setOnCheckedChangeListener(this);
        togglebuttonWarm.setOnCheckedChangeListener(this);
    }

    private String getInfos() {
        StringBuffer sb = new StringBuffer();
        sb.append("冷光: ").append(OnyxSdk.isColdLightOn(this) ? "开启" : "关闭").append("    当前冷光值: ").append(OnyxSdk.getCurrentColdLightValue(this)).append(NEW_LINE);
        sb.append("暖光: ").append(OnyxSdk.isWarmLightOn(this) ? "开启" : "关闭").append("    当前暖光值: ").append(OnyxSdk.getCurrentWarmLightValue(this)).append(NEW_LINE);
        sb.append("对比度: ").append(OnyxSdk.getCurrentGlobalContrast()).append(NEW_LINE);
        return sb.toString();
    }

    @OnClick(R.id.btn_cold)
    public void onButtonColdClick() {
        OnyxSdk.setColdLightValue(this, Integer.valueOf(et_cold.getText().toString()));
        refreshValues();
    }

    @OnClick(R.id.btn_warm)
    public void onButtonWarmClick() {
        OnyxSdk.setWarmLightValue(this, Integer.valueOf(et_warm.getText().toString()));
        refreshValues();
    }

    @OnClick(R.id.btn_contrast)
    public void onButtonContrastClick() {
        OnyxSdk.setGlobalContrast(Integer.valueOf(et_contrast.getText().toString()));
        refreshValues();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.togglebutton_cold:
                if (isChecked) {
                    OnyxSdk.openColdLight();
                } else {
                    OnyxSdk.closeColdLight();
                }
                break;
            case R.id.togglebutton_warm:
                if (isChecked) {
                    OnyxSdk.openWarmLight();
                } else {
                    OnyxSdk.closeWarmLight();
                }
                break;
        }
        refreshValues();
    }



    private int getCurrentColdLightValue() {
        return OnyxSdk.getCurrentColdLightValue(this);
    }

    private int getCurrentWarmLightValue() {
        return OnyxSdk.getCurrentWarmLightValue(this);
    }


    private String getColdValues() {
        return conversionToString(OnyxSdk.getColdLightValues(this));
    }

    private String getWarmValues() {
        return conversionToString(OnyxSdk.getWarmLightValues(this));
    }

    private String conversionToString(Integer[] list) {
        if (list == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (Integer v : list) {
            sb.append(v).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }


}
