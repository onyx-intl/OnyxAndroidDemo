package com.android.onyx.demo.factory

import android.content.Context
import com.android.onyx.demo.model.BaseLightModel
import com.android.onyx.demo.model.CTMAllLightModel
import com.android.onyx.demo.model.FLLightModel
import com.android.onyx.demo.model.WarmAndColdLightModel
import com.onyx.android.sdk.api.device.brightness.BrightnessController
import com.onyx.android.sdk.api.device.brightness.BrightnessType

object FrontLightFactory {
    fun createLightModel(context: Context): BaseLightModel? {
        return when (BrightnessController.getBrightnessType(context)) {
            BrightnessType.FL -> FLLightModel(context)
            BrightnessType.WARM_AND_COLD -> WarmAndColdLightModel(context)
            BrightnessType.CTM -> CTMAllLightModel(context)
            BrightnessType.NONE -> null
            else -> null
        }
    }
}
