package com.example.noteactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    public static final String Note_POSITION = "com.example.noteactivity.Note_Position";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteText;
    private EditText mTextNoteTitle;
    private int mNewPosition;
    private boolean mIsCancel;
    private NoteActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //boilerplate code we write it every time
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.isNewlyCreated && savedInstanceState!=null){
            mViewModel.restoreState(savedInstanceState);
        }
        mViewModel.isNewlyCreated=false;

        mSpinnerCourses = findViewById(R.id.spinner_cources);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);
        readDisplayStateValue();
        saveOrgNoteVals();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancel){
            if(mIsNewNote){
                DataManager.getInstance().removeNote(mNewPosition);
            }else {
                storePrevNoteVals();
            }
        }else {
            saveNote();
        }
    }

    private void storePrevNoteVals() {
       CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOrgNoteCourseId);
       mNote.setCourse(course);
       mNote.setTitle(mViewModel.mOrgNoteTitle);
       mNote.setText(mViewModel.mOrgNoteText);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null){
            mViewModel.saveState(outState);
        }
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void saveOrgNoteVals() {
        if(mIsNewNote){
            return;
        }
        mViewModel.mOrgNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOrgNoteTitle = mNote.getTitle();
        mViewModel.mOrgNoteText = mNote.getText();
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        final List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);

        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValue() {
        Intent intent = getIntent();
        int position =  intent.getIntExtra(Note_POSITION, POSITION_NOT_SET);

        mIsNewNote = position ==POSITION_NOT_SET;
        if(mIsNewNote) {

            createNewNote();
        }else{
            mNote = DataManager.getInstance().getNotes().get(position);
        }

    }

    private void createNewNote() {
        DataManager dataManager =DataManager.getInstance();
        mNewPosition =  dataManager.createNewNote();
        mNote=dataManager.getNotes().get(mNewPosition);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_send_mail){
            sendEmail();
            return true;

        }else if (id ==R.id.action_cancel){
            mIsCancel = true;
             finish();
        }else if (id ==R.id.action_next){
            moveNext();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastIndNote  =DataManager.getInstance().getNotes().size()-1;
        item.setEnabled(mNewPosition<lastIndNote);

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNewPosition;
        mNote=DataManager.getInstance().getNotes().get(mNewPosition);
        saveOrgNoteVals();
        displayNote(mSpinnerCourses,mTextNoteTitle,mTextNoteText);
        invalidateOptionsMenu();
    }

    //    implicit intent
    private void sendEmail() {
    CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
    String subject= mTextNoteText.getText().toString();
    String text =  "checkout that  "+ course.getTitle() +"\"\n"+mTextNoteText.getText();
   Intent intent = new Intent(Intent.ACTION_SEND);
   intent.setType("message/rfc2822");
   intent.putExtra(Intent.EXTRA_SUBJECT,subject);
   intent.putExtra(Intent.EXTRA_TEXT,text);
   startActivity(intent);


    }
}