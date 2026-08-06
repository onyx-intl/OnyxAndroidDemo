package com.android.onyx.demo

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityEacDemoBinding
import com.onyx.android.sdk.api.device.eac.SimpleEACManage
import com.onyx.android.sdk.rx.RxUtils
import com.onyx.android.sdk.utils.DeviceUtils
import com.onyx.android.sdk.utils.RotationUtils
import com.onyx.android.sdk.utils.RxTimerUtil
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * App optimize entrance:long press app to select the optimization option or FloatingButton optimization option.
 */
class EacDemoActivity : AppCompatActivity() {
    private val rotationItemArray =
        arrayOf("rotation 0", "rotation 90", "rotation 180", "rotation 270")
    private lateinit var binding: ActivityEacDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eac_demo)
        initView()
        initData()
    }

    private fun initView() {
        binding.switchEacSupport.setOnCheckedChangeListener { _, isChecked ->
            setSupportEAC(isChecked)
        }
        binding.switchEacEnable.setOnCheckedChangeListener { _, isChecked ->
            setEACEnable(isChecked)
        }
        binding.switchRefreshConfigEnable.setOnCheckedChangeListener { _, isChecked ->
            setEacRefreshConfigEnable(isChecked)
        }
        binding.switchFollowSystemRotationEnable.setOnCheckedChangeListener { _, isChecked ->
            setEacFollowSystemRotation(isChecked)
        }
    }

    private fun initData() {
        updateAllStatus()
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.system_rotation -> systemRotation()
            R.id.app_rotation -> appRotation()
        }
    }

    /**
     * This API is targeted at 3.2 and above.
     */
    private fun setEacRefreshConfigEnable(isChecked: Boolean) {
        RxUtils.runInIO {
            SimpleEACManage.getInstance().setEACRefreshConfigEnable(this, isChecked)
            updateAllStatusDelay()
        }
    }

    /**
     * This API is targeted at 3.3.1 and above.
     */
    private fun setEacFollowSystemRotation(isChecked: Boolean) {
        RxUtils.runInIO {
            SimpleEACManage.getInstance().setFollowSystemRotation(this, isChecked)
            updateAllStatusDelay()
        }
    }

    /**
     * If support EAC is turned off, the optimization setting will not be available.
     * Parameters Context use activity can realize EAC config Immediate effect.(version 3.1 and before not supported，take effect need reopen app)
     */
    fun setSupportEAC(support: Boolean) {
        RxUtils.runInIO {
            SimpleEACManage.getInstance().setSupportEAC(this, support)
            updateAllStatusDelay()
        }
    }

    fun setEACEnable(enable: Boolean) {
        RxUtils.runInIO {
            SimpleEACManage.getInstance().setAppEACEnable(this, enable)
            updateAllStatusDelay()
        }
    }

    /**
     * It`s app optimize switch status, not about with eac enable/disable.
     */
    private fun updateEACSwitchStatus() {
        RxUtils.runWith(
            { SimpleEACManage.getInstance().isAppEACEnabled(packageName) },
            { enable: Boolean? ->
                val value = enable == true
                binding.eacEnableStatus.text = getString(R.string.eac_enable_format, value.toString())
                binding.switchEacEnable.isChecked = value
            },
            Schedulers.io()
        )
    }

    private fun updateHookEpdcStatus() {
        RxUtils.runWith(
            { SimpleEACManage.getInstance().isHookEpdc(packageName) },
            { enable: Boolean? ->
                binding.hookEpdcStatus.text =
                    getString(R.string.hook_epdc_format, (enable == true).toString())
            },
            Schedulers.io()
        )
    }

    private fun updateEACSupportStatus() {
        RxUtils.runWith(
            { SimpleEACManage.getInstance().isSupportEAC(packageName) },
            { support: Boolean? ->
                val value = support == true
                binding.eacSupportStatus.text =
                    getString(R.string.eac_support_format, value.toString())
                binding.switchEacSupport.isChecked = value
            },
            Schedulers.io()
        )
    }

    private fun updateRefreshConfigEnableStatus() {
        RxUtils.runWith(
            { SimpleEACManage.getInstance().isEACRefreshConfigEnable(packageName) },
            { enable: Boolean? ->
                val value = enable == true
                binding.tvEacRefreshConfigEnable.text =
                    getString(R.string.eac_refresh_config_enable_format, value.toString())
                binding.switchRefreshConfigEnable.isChecked = value
            },
            Schedulers.io()
        )
    }

    private fun updateFollowSystemRotationStatus() {
        RxUtils.runWith(
            { SimpleEACManage.getInstance().isFollowSystemRotation(packageName) },
            { enable: Boolean? ->
                val value = enable == true
                binding.tvEacFollowSystemRotationEnable.text =
                    getString(R.string.eac_follow_system_rotation_format, value.toString())
                binding.switchFollowSystemRotationEnable.isChecked = value
            },
            Schedulers.io()
        )
    }

    private fun updateAllStatus() {
        updateEACSupportStatus()
        updateEACSwitchStatus()
        updateHookEpdcStatus()
        updateRefreshConfigEnableStatus()
        updateFollowSystemRotationStatus()
    }

    private fun updateAllStatusDelay() {
        RxTimerUtil.timer(
            UPDATE_EAC_STATUS_DELAY.toLong(),
            TimeUnit.MILLISECONDS,
            object : RxTimerUtil.TimerObserver() {
                override fun onNext(aLong: Long) {
                    updateAllStatus()
                }
            }
        )
    }

    private fun appRotation() {
        AlertDialog.Builder(this).setTitle("App Rotation").setItems(rotationItemArray) { dialog, which ->
            val orientation = computeNewRotation(currentRotation, which * 90)
            dialog.dismiss()
            RotationUtils.setRequestedOrientation(
                this, orientation, false, RotationUtils.ROTATE_BY_APP
            )
        }.show()
    }

    private fun systemRotation() {
        AlertDialog.Builder(this).setTitle("System Rotation").setItems(rotationItemArray) { dialog, which ->
            val orientation = computeNewRotation(currentRotation, which * 90)
            dialog.dismiss()
            RotationUtils.setRequestedOrientation(
                this, orientation, true, RotationUtils.ROTATE_BY_APP
            )
        }.show()
    }

    private val currentRotation: Int
        get() = DeviceUtils.getScreenOrientation(this)

    private fun computeNewRotation(currentOrientation: Int, rotationOperation: Int): Int {
        return when (rotationOperation) {
            0 -> currentOrientation
            90 -> when (currentOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            270 -> when (currentOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            180 -> when (currentOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT ->
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ->
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
            else -> currentOrientation
        }
    }

    companion object {
        private const val UPDATE_EAC_STATUS_DELAY = 300
    }
}
