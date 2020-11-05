package com.onyx.wereaddemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.databinding.ActivitySettingsDemoBinding;

public class SettingsDemoActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String NEW_LINE = "\n";

    private ActivitySettingsDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings_demo);

        initView();
        initData();
    }

    private void initView() {
        binding.btnCold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonColdClick();
            }
        });
        binding.btnWarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonWarmClick();
            }
        });
        binding.btnContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonContrastClick();
            }
        });
    }

    private void initData() {
        binding.tvColdValues.setText(getColdValues());
        binding.tvWarmValues.setText(getWarmValues());
        binding.etCold.setText(String.valueOf(getCurrentColdLightValue()));
        binding.etWarm.setText(String.valueOf(getCurrentWarmLightValue()));
        binding.etContrast.setText(String.valueOf(OnyxSdk.getInstance().getCurrentGlobalContrast()));

        refreshValues();
    }

    private void refreshValues() {
        binding.togglebuttonCold.setOnCheckedChangeListener(null);
        binding.togglebuttonWarm.setOnCheckedChangeListener(null);

        binding.togglebuttonCold.setChecked(OnyxSdk.getInstance().isColdLightOn());
        binding.togglebuttonWarm.setChecked(OnyxSdk.getInstance().isWarmLightOn());
        binding.tvInfos.setText(getInfos());

        binding.togglebuttonCold.setOnCheckedChangeListener(this);
        binding.togglebuttonWarm.setOnCheckedChangeListener(this);
    }

    private String getInfos() {
        StringBuffer sb = new StringBuffer();
        sb.append("冷光: ").append(OnyxSdk.getInstance().isColdLightOn() ? "开启" : "关闭").append("    当前冷光值: ").append(OnyxSdk.getInstance().getCurrentColdLightValue()).append(NEW_LINE);
        sb.append("暖光: ").append(OnyxSdk.getInstance().isWarmLightOn() ? "开启" : "关闭").append("    当前暖光值: ").append(OnyxSdk.getInstance().getCurrentWarmLightValue()).append(NEW_LINE);
        sb.append("对比度: ").append(OnyxSdk.getInstance().getCurrentGlobalContrast()).append(NEW_LINE);
        return sb.toString();
    }

    public void onButtonColdClick() {
        OnyxSdk.getInstance().setColdLightValue(Integer.valueOf(binding.etCold.getText().toString()));
        refreshValues();
    }

    public void onButtonWarmClick() {
        OnyxSdk.getInstance().setWarmLightValue(Integer.valueOf(binding.etWarm.getText().toString()));
        refreshValues();
    }

    public void onButtonContrastClick() {
        OnyxSdk.getInstance().setGlobalContrast(Integer.valueOf(binding.etContrast.getText().toString()));
        refreshValues();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.togglebutton_cold:
                if (isChecked) {
                    OnyxSdk.getInstance().openColdLight();
                } else {
                    OnyxSdk.getInstance().closeColdLight();
                }
                break;
            case R.id.togglebutton_warm:
                if (isChecked) {
                    OnyxSdk.getInstance().openWarmLight();
                } else {
                    OnyxSdk.getInstance().closeWarmLight();
                }
                break;
        }
        refreshValues();
    }



    private int getCurrentColdLightValue() {
        return OnyxSdk.getInstance().getCurrentColdLightValue();
    }

    private int getCurrentWarmLightValue() {
        return OnyxSdk.getInstance().getCurrentWarmLightValue();
    }


    private String getColdValues() {
        return conversionToString(OnyxSdk.getInstance().getColdLightValues());
    }

    private String getWarmValues() {
        return conversionToString(OnyxSdk.getInstance().getWarmLightValues());
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
