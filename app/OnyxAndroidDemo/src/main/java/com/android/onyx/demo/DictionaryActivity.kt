package com.android.onyx.demo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.onyx.demo.databinding.ActivityDictqueryBinding;
import com.onyx.android.sdk.data.DictionaryQuery;
import com.onyx.android.sdk.utils.DictionaryUtil;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seeksky on 2018/5/17.
 */

public class DictionaryActivity extends AppCompatActivity {

    private static final int DELAY_DICTIONARY_LOAD_MS = 2000;
    private static final int MAX_LOADING_RETRY = 5;

    private ActivityDictqueryBinding binding;
    private final List<DictionaryQuery.Dictionary> dictionaryResults = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean suppressSpinnerCallback;
    private int dictionaryLoadCount;
    private Runnable pendingLoadingRetry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dictquery);
        binding.setActivityDictQuery(this);
        setupWebView();
        setupSpinner();
    }

    @Override
    protected void onDestroy() {
        cancelPendingLoadingRetry();
        super.onDestroy();
    }

    public void onClick(View v) {
        dictionaryLoadCount = 0;
        cancelPendingLoadingRetry();
        queryDictionary(binding.edittextKeyword.getText().toString().trim());
    }

    private void setupWebView() {
        WebSettings settings = binding.webviewResult.getSettings();
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
    }

    private void setupSpinner() {
        binding.spinnerDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerCallback || position < 0 || position >= dictionaryResults.size()) {
                    return;
                }
                loadHtml(dictionaryResults.get(position).getExplanation());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void queryDictionary(String keyword) {
        if (StringUtils.isNullOrEmpty(keyword)) {
            Toast.makeText(this, R.string.dict_query_param_error, Toast.LENGTH_SHORT).show();
            return;
        }
        clearResult();
        hideSoftKeyboard();
        new AsyncTask<Void, Void, DictionaryQuery>() {
            @Override
            protected DictionaryQuery doInBackground(Void... params) {
                return DictionaryUtil.queryKeyWord(DictionaryActivity.this, keyword);
            }

            @Override
            protected void onPostExecute(DictionaryQuery dictionaryQuery) {
                if (isFinishing()) {
                    return;
                }
                handleQueryResult(keyword, dictionaryQuery);
            }
        }.execute();
    }

    private void handleQueryResult(final String keyword, @Nullable DictionaryQuery dictionaryQuery) {
        if (dictionaryQuery == null) {
            showMessage(R.string.dict_query_error, Toast.LENGTH_SHORT);
            return;
        }

        switch (dictionaryQuery.getState()) {
            case DictionaryQuery.DICT_STATE_QUERY_SUCCESSFUL:
                List<DictionaryQuery.Dictionary> list = dictionaryQuery.getList();
                if (list == null || list.isEmpty()) {
                    showMessage(R.string.dict_query_no_data, Toast.LENGTH_SHORT);
                    return;
                }
                bindDictionaryResults(list);
                break;
            case DictionaryQuery.DICT_STATE_LOADING:
                handleLoadingState(keyword);
                break;
            case DictionaryQuery.DICT_STATE_QUERY_FAILED:
                showMessage(R.string.dict_query_failed, Toast.LENGTH_SHORT);
                break;
            case DictionaryQuery.DICT_STATE_NO_DATA:
                showMessage(R.string.dict_query_no_data, Toast.LENGTH_SHORT);
                break;
            case DictionaryQuery.DICT_STATE_PARAM_ERROR:
                showMessage(R.string.dict_query_param_error, Toast.LENGTH_SHORT);
                break;
            case DictionaryQuery.DICT_STATE_ERROR:
            default:
                showMessage(R.string.dict_query_error, Toast.LENGTH_SHORT);
                break;
        }
    }

    private void handleLoadingState(final String keyword) {
        if (dictionaryLoadCount >= MAX_LOADING_RETRY) {
            showMessage(R.string.dict_query_load_fail, Toast.LENGTH_SHORT);
            return;
        }
        showMessage(R.string.dict_query_loading, Toast.LENGTH_SHORT);
        dictionaryLoadCount++;
        cancelPendingLoadingRetry();
        pendingLoadingRetry = new Runnable() {
            @Override
            public void run() {
                pendingLoadingRetry = null;
                if (!isFinishing()) {
                    queryDictionary(keyword);
                }
            }
        };
        handler.postDelayed(pendingLoadingRetry, DELAY_DICTIONARY_LOAD_MS);
    }

    private void bindDictionaryResults(List<DictionaryQuery.Dictionary> list) {
        dictionaryResults.clear();
        dictionaryResults.addAll(list);

        List<String> dictNames = new ArrayList<>(list.size());
        for (DictionaryQuery.Dictionary dictionary : list) {
            String name = dictionary.getDictName();
            dictNames.add(StringUtils.isNullOrEmpty(name)
                    ? getString(R.string.dict_query_unknown_dictionary) : name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.layout_dict_spinner_item, dictNames);
        adapter.setDropDownViewResource(R.layout.layout_dict_spinner_dropdown_item);

        suppressSpinnerCallback = true;
        binding.spinnerDict.setAdapter(adapter);
        binding.spinnerDict.setSelection(0, false);
        suppressSpinnerCallback = false;

        binding.spinnerDict.setVisibility(list.size() > 1 ? View.VISIBLE : View.GONE);
        loadHtml(list.get(0).getExplanation());
    }

    private void loadHtml(String html) {
        binding.webviewResult.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void clearResult() {
        binding.spinnerDict.setVisibility(View.GONE);
        dictionaryResults.clear();
        loadHtml("");
    }

    private void showMessage(@StringRes int messageResId, int duration) {
        String message = getString(messageResId);
        binding.spinnerDict.setVisibility(View.GONE);
        dictionaryResults.clear();
        loadHtml("");
        Toast.makeText(this, message, duration).show();
    }

    private void cancelPendingLoadingRetry() {
        if (pendingLoadingRetry != null) {
            handler.removeCallbacks(pendingLoadingRetry);
            pendingLoadingRetry = null;
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.buttonQuery.getWindowToken(), 0);
        }
    }
}
