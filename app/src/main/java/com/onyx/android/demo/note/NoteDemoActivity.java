package com.onyx.android.demo.note;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.onyx.android.demo.R;
import com.onyx.android.demo.note.bean.MultipleExportResult;
import com.onyx.android.demo.note.bean.NoteProgress;
import com.onyx.android.sdk.scribble.data.NoteModel;
import com.onyx.android.sdk.scribble.data.bean.OpenNoteBean;
import com.onyx.android.sdk.scribble.provider.RemoteNoteProvider;
import com.onyx.android.sdk.scribble.utils.ThumbnailUtils;
import com.onyx.android.sdk.utils.JSONUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.ShapeGeneratedDatabaseHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhilun on 2019/10/16.
 */
public class NoteDemoActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OPEN_NOTE = 100;
    private static final String TAG = NoteDemoActivity.class.getSimpleName();
    private OpenNoteBean openNoteBean;
    private BroadcastReceiver receiver;

    @Bind(R.id.textView_note_return_result)
    public TextView textViewReturnResult;
    @Bind(R.id.textView_load_note_model_result)
    public TextView textViewLoadNoteModelResult;
    @Bind(R.id.textView_note_thumbnail_path)
    public TextView textViewNoteThumbnailPath;
    @Bind(R.id.textView_export_result)
    public TextView textViewExportResult;
    @Bind(R.id.editText_note_id)
    public EditText editTextNoteId;
    @Bind(R.id.editText_export_path)
    public EditText editTextExportPath;
    private RemoteNoteProvider remoteNoteProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_demo);
        ButterKnife.bind(this);
        FlowManager.init(FlowConfig.builder(this).addDatabaseHolder(ShapeGeneratedDatabaseHolder.class).build());
        remoteNoteProvider = new RemoteNoteProvider();
        editTextExportPath.setText(Environment.getExternalStorageDirectory().getPath() + File.separator + "Note.pdf");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String serviceAction = intent.getStringExtra(NoteConstants.SERVICE_ACTION);
                if (StringUtils.isNullOrEmpty(action) ||
                        !StringUtils.safelyEquals(NoteConstants.ACTION_SERVICE_INTENT_RESULT, action) ||
                        !StringUtils.safelyEquals(NoteConstants.ACTION_EXPORT_NOTE, serviceAction)) {
                    return;
                }
                String json = intent.getStringExtra(NoteConstants.KEY_SERVICE_INTENT_RESULT_JSON);
                MultipleExportResult result = JSONUtils.toBean(json, MultipleExportResult.class);
                Log.i(TAG, "onReceive: " + result.toString());
                if (result.inProgress()) {
                    NoteProgress progress = result.progress;
                    String message = String.format(Locale.getDefault(), "Exporting pdf page (%d/%d) in \"%s\"(%d/%d)....",
                            progress.pageIndex + 1,
                            progress.pageCount,
                            progress.noteTitle,
                            progress.noteIndex + 1,
                            progress.noteCount);
                    appendText(textViewExportResult, message);
                } else if (result.isSuccess()) {
                    String message = String.format("Export success: %s", editTextExportPath.getText().toString());
                    appendText(textViewExportResult, message);
                    Toast.makeText(NoteDemoActivity.this, message, Toast.LENGTH_SHORT).show();
                } else if (result.isFail()) {
                    String message = "Export failed";
                    appendText(textViewExportResult, message);
                    Toast.makeText(NoteDemoActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(NoteConstants.ACTION_SERVICE_INTENT_RESULT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @OnClick(R.id.button_create_note)
    public void createNewNote() {
        createNote(null);
    }

    @OnClick(R.id.button_load_note_model)
    public void loadNoteModel() {
        String noteId = editTextNoteId.getText().toString();
        if (StringUtils.isNullOrEmpty(noteId)) {
            Toast.makeText(this, "Please input note id", Toast.LENGTH_SHORT).show();
            return;
        }
        NoteModel noteModel = remoteNoteProvider.loadNote(noteId);
        textViewLoadNoteModelResult.setText(noteModel == null ? "note not exist" : noteModel.toString());
    }

    @OnClick(R.id.button_get_note_thumbnail_path)
    public void getNoteThumbnailPath() {
        if (openNoteBean == null || StringUtils.isNullOrEmpty(openNoteBean.documentId)) {
            Toast.makeText(this, "Please create note first", Toast.LENGTH_SHORT).show();
            return;
        }
        String thumbnailPath = ThumbnailUtils.thumbnailPath(this, openNoteBean.documentId);
        textViewNoteThumbnailPath.setText(thumbnailPath);
    }

    @OnClick(R.id.button_export_pdf)
    public void exportPdf() {
        if (openNoteBean == null || StringUtils.isNullOrEmpty(openNoteBean.documentId)) {
            Toast.makeText(this, "Please create note first", Toast.LENGTH_SHORT).show();
            return;
        }
        String exportPath = editTextExportPath.getText().toString();
        if (StringUtils.isNullOrEmpty(exportPath)) {
            Toast.makeText(this, "Please input export path", Toast.LENGTH_SHORT).show();
            return;
        }
        textViewExportResult.setText(null);
        Intent intent = new Intent();
        intent.setAction(NoteConstants.NOTE_SERVICE_ACTION);
        intent.setPackage(NoteConstants.NOTE_PACKAGE_NAME);
        intent.putExtra(NoteConstants.SERVICE_ACTION, NoteConstants.ACTION_EXPORT_NOTE);
        intent.putExtra(NoteConstants.OPEN_NOTE_BEAN_LIST_EXTRA, JSONUtils.toJson(getOpenNoteBeanList()));
        intent.putExtra(NoteConstants.EXPORT_PATH, exportPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @OnClick(R.id.button_create_note_specified_folder)
    public void createNewNoteSpecifiedFolder() {
        new DialogCreateNoteFolder(this)
                .setOnDialogCreateNoteListener(new DialogCreateNoteFolder.OnDialogCreateNoteListener() {
                    @Override
                    public void onCreateNote(NoteModel library) {
                        createNote(library.getUniqueId());
                    }
                }).show();
    }

    private void createNote(String parentLibraryId) {
        Intent intent = new Intent();
        openNoteBean = new OpenNoteBean().setTitle("Note").setJumpBackToNote(false);
        openNoteBean.setParentUniqueId(parentLibraryId);
        intent.putExtra(NoteConstants.OPEN_NOTE_BEAN, JSON.toJSONString(openNoteBean));
        ComponentName comp = new ComponentName(NoteConstants.NOTE_PACKAGE_NAME, NoteConstants.SCRIBBLE_ACTIVITY_CLASS_PATH);
        intent.setComponent(comp);
        startActivityForResult(intent, REQUEST_CODE_OPEN_NOTE);
    }

    private void appendText(TextView textView, CharSequence cs) {
        textView.setText(String.valueOf(textView.getText()) + "\n" + cs);
    }

    private List<OpenNoteBean> getOpenNoteBeanList() {
        ArrayList<OpenNoteBean> openNoteBeans = new ArrayList<>();
        openNoteBeans.add(openNoteBean);
        return openNoteBeans;
    }

    private String openNoteBeanToString(OpenNoteBean bean) {
        return "OpenNoteBean{" +
                "documentId='" + bean.documentId + '\'' +
                ", parentUniqueId='" + bean.parentUniqueId + '\'' +
                ", title='" + bean.title + '\'' +
                ", associationType=" + bean.associationType +
                ", associationId='" + bean.associationId + '\'' +
                ", jumpBackToNote=" + bean.jumpBackToNote +
                '}';
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE_OPEN_NOTE == requestCode) {
            String json = data.getStringExtra(NoteConstants.KEY_OPEN_NOTE_BEAN_JSON);
            try {
                openNoteBean = JSON.parseObject(json, OpenNoteBean.class);
                textViewReturnResult.setText(openNoteBeanToString(openNoteBean));
                editTextNoteId.setText(openNoteBean.documentId);
            } catch (Exception e) {
                textViewReturnResult.setText("error occurs, please check log for details");
                e.printStackTrace();
            }
        }
    }
}
