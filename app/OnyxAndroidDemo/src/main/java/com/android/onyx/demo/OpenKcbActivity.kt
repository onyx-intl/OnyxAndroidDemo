package com.android.onyx.demo

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityOpenKcbBinding
import com.onyx.android.sdk.utils.JSONUtils
import com.onyx.android.sdk.utils.StringUtils

/**
 * Created by zhilun on 2019/4/4.
 */
class OpenKcbActivity : AppCompatActivity() {
    enum class TabAction {
        NOTHING, OPEN_HOME, OPEN_STORAGE, OPEN_APPLICATION_MANAGEMENT, OPEN_SETTING, OPEN_APPS,
        OPEN_NOTE, OPEN_ACCOUNT_MANAGER, OPEN_SHOP
    }

    enum class NoteRouter {
        SEARCH, BACKUP, COMMON_SETTING, AI_SETTING
    }

    class TabIntentData {
        var jumpPath: String? = null
        var action: TabAction = TabAction.NOTHING

        fun setJumpPath(jumpPath: String?): TabIntentData {
            this.jumpPath = jumpPath
            return this
        }

        fun setAction(action: TabAction): TabIntentData {
            this.action = action
            return this
        }
    }

    private lateinit var binding: ActivityOpenKcbBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_kcb)
        binding.activityOpenKcb = this
    }

    fun openLibrary(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_HOME))
    }

    fun openShop(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_SHOP))
    }

    fun openNote(view: View?) {
        openModule(setNoteJumpPath(TabIntentData().setAction(TabAction.OPEN_NOTE)))
    }

    fun openStorage(view: View?) {
        val data = TabIntentData().setAction(TabAction.OPEN_STORAGE)
        val jumpPath = binding.etStorageJumpPath.text.toString()
        if (!StringUtils.isNullOrEmpty(jumpPath)) {
            data.setJumpPath(jumpPath)
        }
        openModule(data)
    }

    fun openApps(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_APPS))
    }

    fun openSetting(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_SETTING))
    }

    fun openApplicationManagement(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_APPLICATION_MANAGEMENT))
    }

    fun openAccountManagement(view: View?) {
        openModule(TabIntentData().setAction(TabAction.OPEN_ACCOUNT_MANAGER))
    }

    private fun setNoteJumpPath(data: TabIntentData): TabIntentData {
        val jumpPath = when (binding.rgNote.checkedRadioButtonId) {
            R.id.rb_search -> NoteRouter.SEARCH.toString()
            R.id.rb_backup -> NoteRouter.BACKUP.toString()
            R.id.rb_common_setting -> NoteRouter.COMMON_SETTING.toString()
            R.id.rb_ai_setting -> NoteRouter.AI_SETTING.toString()
            else -> ""
        }
        return data.setJumpPath(jumpPath)
    }

    private fun openModule(data: TabIntentData) {
        val intent = Intent()
        intent.component = ComponentName("com.onyx", "com.onyx.main.ui.MainActivity")
        intent.putExtra("json", JSONUtils.toJson(data))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "open kcb failed!", Toast.LENGTH_SHORT).show()
        }
    }
}
