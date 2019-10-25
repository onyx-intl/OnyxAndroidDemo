package com.onyx.android.demo.note.bean;

public class MultipleExportResult extends ServiceIntentResult {
    public NoteProgress progress;
    public String exportTitle;

    public MultipleExportResult() {
    }

    public MultipleExportResult setProgress(NoteProgress progress) {
        this.progress = progress;
        return this;
    }

    public MultipleExportResult setExportTitle(String exportTitle) {
        this.exportTitle = exportTitle;
        return this;
    }
}
