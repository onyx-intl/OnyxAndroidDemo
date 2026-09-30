package com.android.onyx.demo

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityReaderDemoBinding
import com.onyx.android.sdk.utils.FileUtils
import com.onyx.android.sdk.utils.StringUtils
import java.io.File

/**
 * Created by Administrator on 2018/4/25 17:23.
 */
class ReaderDemoActivity : Activity() {
    private lateinit var binding: ActivityReaderDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reader_demo)
        binding.activityReader = this
    }

    fun btn_open(view: View?) {
        if (!filePathValidation()) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.component =
            ComponentName("com.onyx.kreader", "com.onyx.kreader.ui.ReaderHomeActivity")
        intent.data = FileProvider.getUriForFile(
            this,
            "$packageName.onyx.fileprovider",
            File(binding.etFile.text.toString())
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivity(intent)
    }

    fun btn_query_progress(view: View?) {
        if (!filePathValidation()) {
            return
        }
        val progress = queryByPath(binding.etFile.text.toString())
        if (!StringUtils.isNullOrEmpty(progress)) {
            binding.textViewProgress.text = getString(R.string.reading_progress, progress)
        } else {
            Toast.makeText(applicationContext, R.string.query_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun btn_delete_reader_data(view: View?) {
        if (!filePathValidation()) {
            return
        }
        try {
            // Handwritten notes have a cache, you need to restart Reader
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses("com.onyx.kreader")
            Toast.makeText(this, R.string.delete_reader_data_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.delete_reader_data_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun queryByPath(path: String): String? {
        var cursor: Cursor? = null
        var progress: String? = ""
        try {
            val resolver = contentResolver
            val uri = Uri.parse(READER_PROVIDER)
            val md5 = FileUtils.computeMD5(File(path))
            cursor = resolver.query(
                uri,
                arrayOf("progress"),
                "hashTag = ? or nativeAbsolutePath = ?",
                arrayOf(md5, path),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                progress = cursor.getString(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return progress
    }

    private fun filePathValidation(): Boolean {
        val filePath = binding.etFile.text.toString()
        if (filePath.isEmpty()) {
            Toast.makeText(this, R.string.enter_book_path, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!File(filePath).exists()) {
            Toast.makeText(this, R.string.invalid_path, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        private const val READER_PROVIDER =
            "content://com.onyx.content.database.ContentProvider/Metadata"
    }
}
