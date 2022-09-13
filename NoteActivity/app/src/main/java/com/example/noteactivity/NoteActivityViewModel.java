package com.example.noteactivity;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.noteactivity.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.noteactivity.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.noteactivity.ORIGINAL_NOTE_TEXT";
    public String mOrgNoteCourseId;
    public String mOrgNoteTitle;
    public String mOrgNoteText;
    public boolean isNewlyCreated = true;


    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID,mOrgNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE,mOrgNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT,mOrgNoteText);

    }
    public void restoreState(Bundle inState){
        mOrgNoteCourseId=inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOrgNoteText=inState.getString(ORIGINAL_NOTE_TEXT);
        mOrgNoteTitle=inState.getString(ORIGINAL_NOTE_TITLE);

    }
}
