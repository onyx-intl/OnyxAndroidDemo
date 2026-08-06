package com.android.onyx.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityEpdDemoBinding
import com.onyx.android.sdk.api.device.EpdDeviceManager
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode

class EpdDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEpdDemoBinding
    private var isFastMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_epd_demo)
        // set full update after how many partial update
        EpdDeviceManager.setGcInterval(5)
    }

    fun onClick(v: View) {
        when (v) {
            binding.buttonPartialUpdate -> {
                updateTextView()
                EpdDeviceManager.applyWithGCIntervalWithoutRegal(binding.textview)
            }
            binding.buttonRegalPartial -> {
                updateTextView()
                EpdDeviceManager.applyWithGCIntervalWitRegal(binding.textview, true)
            }
            binding.buttonScreenRefresh -> {
                updateTextView()
                EpdController.repaintEveryThing(UpdateMode.GC)
            }
            binding.buttonEnterFastMode -> {
                isFastMode = true
                EpdDeviceManager.enterAnimationUpdate(true)
            }
            binding.buttonQuitFastMode -> {
                EpdDeviceManager.exitAnimationUpdate(true)
                isFastMode = false
            }
            binding.buttonEnterXMode -> {
                EpdController.clearAppScopeUpdate()
                EpdController.applyAppScopeUpdate(
                    TAG, true, true, UpdateMode.ANIMATION_X, Int.MAX_VALUE
                )
            }
            binding.buttonEnterA2Mode -> {
                EpdController.clearAppScopeUpdate()
                EpdController.applyAppScopeUpdate(
                    TAG, true, true, UpdateMode.ANIMATION_QUALITY, Int.MAX_VALUE
                )
            }
            binding.buttonEnterNormalMode -> {
                EpdController.clearAppScopeUpdate()
                EpdController.applyAppScopeUpdate(
                    TAG, false, true, UpdateMode.None, Int.MAX_VALUE
                )
            }
            binding.buttonEnterDuMode -> {
                EpdController.clearAppScopeUpdate()
                EpdController.applyAppScopeUpdate(
                    TAG, true, true, UpdateMode.DU_QUALITY, Int.MAX_VALUE
                )
            }
        }
    }

    private fun updateTextView() {
        binding.textview.append("hello world!")
    }

    companion object {
        private val TAG: String = EpdDemoActivity::class.java.simpleName
    }
}
