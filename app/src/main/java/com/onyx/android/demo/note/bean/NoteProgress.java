package com.onyx.android.demo.note.bean;

public class NoteProgress {

    public String noteTitle;
    public int noteCount;
    public int noteIndex;
    public int pageCount;
    public int pageIndex;

    public NoteProgress setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
        return this;
    }

    public NoteProgress setNoteCount(int noteCount) {
        this.noteCount = noteCount;
        return this;
    }

    public NoteProgress setNoteIndex(int noteIndex) {
        this.noteIndex = noteIndex;
        return this;
    }

    public NoteProgress setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    public NoteProgress setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }
}
