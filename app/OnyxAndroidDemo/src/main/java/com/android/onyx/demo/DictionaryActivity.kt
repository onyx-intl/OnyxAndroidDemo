package com.android.onyx.demo

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.onyx.demo.databinding.ActivityDictqueryBinding
import com.onyx.android.sdk.data.DictionaryQuery
import com.onyx.android.sdk.utils.DictionaryUtil
import com.onyx.android.sdk.utils.StringUtils

/**
 * Created by seeksky on 2018/5/17.
 */
class DictionaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDictqueryBinding
    private val dictionaryResults: MutableList<DictionaryQuery.Dictionary> = ArrayList()
    private val handler = Handler(Looper.getMainLooper())
    private var suppressSpinnerCallback = false
    private var dictionaryLoadCount = 0
    private var pendingLoadingRetry: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dictquery)
        binding.activityDictQuery = this
        setupWebView()
        setupSpinner()
    }

    override fun onDestroy() {
        cancelPendingLoadingRetry()
        super.onDestroy()
    }

    fun onClick(v: View?) {
        dictionaryLoadCount = 0
        cancelPendingLoadingRetry()
        queryDictionary(binding.edittextKeyword.text.toString().trim { it <= ' ' })
    }

    private fun setupWebView() {
        val settings = binding.webviewResult.settings
        settings.defaultTextEncodingName = "UTF-8"
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
    }

    private fun setupSpinner() {
        binding.spinnerDict.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (suppressSpinnerCallback || position !in dictionaryResults.indices) {
                    return
                }
                loadHtml(dictionaryResults[position].explanation)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun queryDictionary(keyword: String) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            Toast.makeText(this, R.string.dict_query_param_error, Toast.LENGTH_SHORT).show()
            return
        }
        clearResult()
        hideSoftKeyboard()
        object : AsyncTask<Void, Void, DictionaryQuery>() {
            @Deprecated("Deprecated in Java")
            override fun doInBackground(vararg params: Void?): DictionaryQuery {
                return DictionaryUtil.queryKeyWord(this@DictionaryActivity, keyword)
            }

            @Deprecated("Deprecated in Java")
            override fun onPostExecute(dictionaryQuery: DictionaryQuery?) {
                if (isFinishing) {
                    return
                }
                handleQueryResult(keyword, dictionaryQuery)
            }
        }.execute()
    }

    private fun handleQueryResult(keyword: String, dictionaryQuery: DictionaryQuery?) {
        if (dictionaryQuery == null) {
            showMessage(R.string.dict_query_error, Toast.LENGTH_SHORT)
            return
        }

        when (dictionaryQuery.state) {
            DictionaryQuery.DICT_STATE_QUERY_SUCCESSFUL -> {
                val list = dictionaryQuery.list
                if (list.isNullOrEmpty()) {
                    showMessage(R.string.dict_query_no_data, Toast.LENGTH_SHORT)
                    return
                }
                bindDictionaryResults(list)
            }
            DictionaryQuery.DICT_STATE_LOADING ->
                handleLoadingState(keyword)
            DictionaryQuery.DICT_STATE_QUERY_FAILED ->
                showMessage(R.string.dict_query_failed, Toast.LENGTH_SHORT)
            DictionaryQuery.DICT_STATE_NO_DATA ->
                showMessage(R.string.dict_query_no_data, Toast.LENGTH_SHORT)
            DictionaryQuery.DICT_STATE_PARAM_ERROR ->
                showMessage(R.string.dict_query_param_error, Toast.LENGTH_SHORT)
            else -> showMessage(R.string.dict_query_error, Toast.LENGTH_SHORT)
        }
    }

    private fun handleLoadingState(keyword: String) {
        if (dictionaryLoadCount >= MAX_LOADING_RETRY) {
            showMessage(R.string.dict_query_load_fail, Toast.LENGTH_SHORT)
            return
        }
        showMessage(R.string.dict_query_loading, Toast.LENGTH_SHORT)
        dictionaryLoadCount++
        cancelPendingLoadingRetry()
        val retry = Runnable {
            pendingLoadingRetry = null
            if (!isFinishing) {
                queryDictionary(keyword)
            }
        }
        pendingLoadingRetry = retry
        handler.postDelayed(retry, DELAY_DICTIONARY_LOAD_MS.toLong())
    }

    private fun bindDictionaryResults(list: List<DictionaryQuery.Dictionary>) {
        dictionaryResults.clear()
        dictionaryResults.addAll(list)

        val dictNames = ArrayList<String>(list.size)
        for (dictionary in list) {
            val name = dictionary.dictName
            dictNames.add(
                if (StringUtils.isNullOrEmpty(name)) {
                    getString(R.string.dict_query_unknown_dictionary)
                } else {
                    name
                }
            )
        }

        val adapter = ArrayAdapter(this, R.layout.layout_dict_spinner_item, dictNames)
        adapter.setDropDownViewResource(R.layout.layout_dict_spinner_dropdown_item)

        suppressSpinnerCallback = true
        binding.spinnerDict.adapter = adapter
        binding.spinnerDict.setSelection(0, false)
        suppressSpinnerCallback = false

        binding.spinnerDict.visibility = if (list.size > 1) View.VISIBLE else View.GONE
        loadHtml(list[0].explanation)
    }

    private fun loadHtml(html: String?) {
        binding.webviewResult.loadDataWithBaseURL(null, html ?: "", "text/html", "UTF-8", null)
    }

    private fun clearResult() {
        binding.spinnerDict.visibility = View.GONE
        dictionaryResults.clear()
        loadHtml("")
    }

    private fun showMessage(@StringRes messageResId: Int, duration: Int) {
        val message = getString(messageResId)
        binding.spinnerDict.visibility = View.GONE
        dictionaryResults.clear()
        loadHtml("")
        Toast.makeText(this, message, duration).show()
    }

    private fun cancelPendingLoadingRetry() {
        pendingLoadingRetry?.let {
            handler.removeCallbacks(it)
            pendingLoadingRetry = null
        }
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        imm.hideSoftInputFromWindow(binding.buttonQuery.windowToken, 0)
    }

    companion object {
        private const val DELAY_DICTIONARY_LOAD_MS = 2000
        private const val MAX_LOADING_RETRY = 5
    }
}