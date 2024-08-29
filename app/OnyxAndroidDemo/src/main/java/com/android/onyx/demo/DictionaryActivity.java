package com.android.onyx.demo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityDictqueryBinding;
import com.onyx.android.sdk.data.DictionaryQuery;
import com.onyx.android.sdk.utils.DictionaryUtil;

import java.util.List;

/**
 * Created by seeksky on 2018/5/17.
 */

public class DictionaryActivity extends AppCompatActivity {

    private ActivityDictqueryBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dictquery);
        binding.setActivityDictQuery(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        binding.textviewQueryResult.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    public void onClick(View v) {
        doQueryJob(binding.edittextKeyword.getText().toString());
    }

    private void doQueryJob(final String keyword) {
        binding.textviewQueryResult.setText("");
        hideSoftKeyboard();
        new AsyncTask<Void, Void, DictionaryQuery>() {
            @Override
            protected DictionaryQuery doInBackground(Void... params) {
                return DictionaryUtil.queryKeyWord(DictionaryActivity.this, keyword);
            }

            @Override
            protected void onPostExecute(DictionaryQuery dictionaryQuery) {
                super.onPostExecute(dictionaryQuery);
                if (dictionaryQuery.getState() == DictionaryQuery.DICT_STATE_QUERY_SUCCESSFUL) {
                    List<DictionaryQuery.Dictionary> list = dictionaryQuery.getList();
                    if (list == null || list.size() <= 0) {
                        return;
                    }
                    for (DictionaryQuery.Dictionary dictionary : list) {
                        System.out.println("dictionary = " + dictionary.getExplanation());
                        binding.textviewQueryResult.append(dictionary.getDictName() + "\n");
                        binding.textviewQueryResult.append(dictionary.getExplanation());
                        binding.textviewQueryResult.append("\n=============================\n");
                    }
                }
            }
        }.execute();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.buttonQuery.getWindowToken(), 0);
        }
    }
}
