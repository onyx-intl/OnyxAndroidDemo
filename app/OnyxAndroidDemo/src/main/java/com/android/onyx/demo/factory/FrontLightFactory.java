package com.android.onyx.demo.factory;

import android.content.Context;

import com.android.onyx.demo.model.BaseLightModel;
import com.android.onyx.demo.model.CTMAllLightModel;
import com.android.onyx.demo.model.FLLightModel;
import com.android.onyx.demo.model.WarmAndColdLightModel;
import com.onyx.android.sdk.api.device.brightness.BrightnessController;

public class FrontLightFactory {

    public static BaseLightModel createLightModel(Context context) {
        BaseLightModel lightModel = null;
        switch (BrightnessController.getBrightnessType(context)) {
            case FL:
                lightModel = new FLLightModel(context);
                break;
            case WARM_AND_COLD:
                lightModel = new WarmAndColdLightModel(context);
                break;
            case CTM:
                lightModel = new CTMAllLightModel(context);
                break;
            case NONE:
            default:
                break;
        }
        return lightModel;
    }
}
