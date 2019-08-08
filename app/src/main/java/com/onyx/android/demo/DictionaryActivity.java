package com.onyx.android.demo;

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

import com.onyx.android.sdk.data.DictionaryQuery;
import com.onyx.android.sdk.utils.DictionaryUtil;

import java.util.List;

/**
 * Created by seeksky on 2018/5/17.
 */

public class DictionaryActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText editText;
    private Button buttonQuery;
    private TextView textViewDisplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictquery);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.edittext_keyword);
        buttonQuery = (Button) findViewById(R.id.button_query);
        textViewDisplay = (TextView) findViewById(R.id.textview_query_result);
        textViewDisplay.setMovementMethod(ScrollingMovementMethod.getInstance());
        buttonQuery.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        doQueryJob(editText.getText().toString());
    }

    private void doQueryJob(final String keyword) {
        textViewDisplay.setText("");
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
                        textViewDisplay.append(dictionary.getDictName() + "\n");
                        textViewDisplay.append(dictionary.getExplanation());
                        textViewDisplay.append("\n=============================\n");
                    }
                }
            }
        }.execute();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(buttonQuery.getWindowToken(),0);
        }
    }
}
