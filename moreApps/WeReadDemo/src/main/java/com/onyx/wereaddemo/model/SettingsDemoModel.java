package com.onyx.wereaddemo.model;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.onyx.weread.api.OnyxSdk;
import com.onyx.wereaddemo.databinding.ActivitySettingsDemoBinding;

public class SettingsDemoModel extends BaseObservable {

    private static final String NEW_LINE = "\n";

    public ObservableField<String> coldConfigValues = new ObservableField<>();
    public ObservableField<String> warmConfigValues = new ObservableField<>();

    public ObservableField<String> coldValue = new ObservableField<>();
    public ObservableField<String> warmValue = new ObservableField<>();
    public ObservableField<String> contrastValue = new ObservableField<>();
    public ObservableField<String> info = new ObservableField<>();

    private ActivitySettingsDemoBinding binding;

    private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (compoundButton == binding.togglebuttonCold) {
                if (isChecked) {
                    OnyxSdk.getInstance().openColdLight();
                } else {
                    OnyxSdk.getInstance().closeColdLight();
                }
            } else if (compoundButton == binding.togglebuttonWarm) {
                if (isChecked) {
                    OnyxSdk.getInstance().openWarmLight();
                } else {
                    OnyxSdk.getInstance().closeWarmLight();
                }
            }
            refreshValues();
        }
    };

    public SettingsDemoModel(ActivitySettingsDemoBinding binding) {
        this.binding = binding;
        initData();
    }

    private void initData() {
        coldConfigValues.set(getColdValues());
        warmConfigValues.set(getWarmValues());
        coldValue.set(String.valueOf(getCurrentColdLightValue()));
        warmValue.set(String.valueOf(getCurrentWarmLightValue()));
        contrastValue.set(String.valueOf(OnyxSdk.getInstance().getCurrentGlobalContrast()));

        refreshValues();
    }

    public void onButtonColdClick() {
        OnyxSdk.getInstance().setColdLightValue(Integer.valueOf(coldValue.get()));
        refreshValues();
    }

    public void onButtonWarmClick() {
        OnyxSdk.getInstance().setWarmLightValue(Integer.valueOf(warmValue.get()));
        refreshValues();
    }

    public void onButtonContrastClick() {
        OnyxSdk.getInstance().setGlobalContrast(Integer.valueOf(contrastValue.get()));
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

    private void refreshValues() {
        binding.togglebuttonCold.setOnCheckedChangeListener(null);
        binding.togglebuttonWarm.setOnCheckedChangeListener(null);

        binding.togglebuttonCold.setChecked(OnyxSdk.getInstance().isColdLightOn());
        binding.togglebuttonWarm.setChecked(OnyxSdk.getInstance().isWarmLightOn());
        info.set(getInfos());

        binding.togglebuttonCold.setOnCheckedChangeListener(checkedChangeListener);
        binding.togglebuttonWarm.setOnCheckedChangeListener(checkedChangeListener);
    }

    private String getInfos() {
        StringBuffer sb = new StringBuffer();
        sb.append("冷光: ").append(OnyxSdk.getInstance().isColdLightOn() ? "开启" : "关闭").append("    当前冷光值: ").append(OnyxSdk.getInstance().getCurrentColdLightValue()).append(NEW_LINE);
        sb.append("暖光: ").append(OnyxSdk.getInstance().isWarmLightOn() ? "开启" : "关闭").append("    当前暖光值: ").append(OnyxSdk.getInstance().getCurrentWarmLightValue()).append(NEW_LINE);
        sb.append("对比度: ").append(OnyxSdk.getInstance().getCurrentGlobalContrast()).append(NEW_LINE);
        return sb.toString();
    }

}
