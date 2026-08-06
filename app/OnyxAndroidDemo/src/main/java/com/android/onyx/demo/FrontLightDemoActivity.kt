package com.android.onyx.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityFrontLightDemoBinding
import com.android.onyx.demo.factory.FrontLightFactory
import com.android.onyx.demo.model.BaseLightModel

class FrontLightDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrontLightDemoBinding
    private var lightModel: BaseLightModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_front_light_demo)
        initLightModel()
    }

    private fun initLightModel() {
        lightModel = FrontLightFactory.createLightModel(this)
        lightModel?.let { model ->
            model.initView(binding)
            binding.buttonShowBrightnessSetting.setOnClickListener { v ->
                model.showBrightnessSetting(v)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightModel?.updateLightValue()
    }
}
