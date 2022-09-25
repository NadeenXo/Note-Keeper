package com.example.noteactivity;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.CursorLoader;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.ViewModelProvider;
//import androidx.loader.content.CursorLoader;

import com.example.noteactivity.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.noteactivity.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.example.noteactivity.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.noteactivity.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.noteactivity.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.noteactivity.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote= new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteText;
    private EditText mTextNoteTitle;
    private int mNoteId;
    private boolean mIsCancel;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);
        //boilerplate code we write it every time
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.isNewlyCreated && savedInstanceState!=null){
            mViewModel.restoreState(savedInstanceState);
        }
        mViewModel.isNewlyCreated=false;

        mSpinnerCourses = findViewById(R.id.spinner_cources);
        //from memory
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,courses);
//        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpinnerCourses.setAdapter(adapterCourses);

        //from db
        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        getLoaderManager().initLoader(LOADER_COURSES,null,this);

        readDisplayStateValue();
        if(savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
//            loadNoteData();
//            LOADER_NOTES = 0;
            getLoaderManager().initLoader(LOADER_NOTES, null,this);
        }

        Log.d(TAG, "onCreate");

    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        //if i want a note
        String courseId = "android_intents";
        String titleStart = "dynamic";

       // String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND " + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";
//        String[] selectionArgs = {courseId, titleStart + "%"};
        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext(); // we need it because it will be before the first row of the result
        displayNote();
    }
    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }
    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancel){
            if(mIsNewNote){
                deleteNoteFromDB();
            }else {
                storePrevNoteVals();
            }
        }else {
            saveNote();
        }
    }

    private void deleteNoteFromDB() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePrevNoteVals() {
//       CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOrgNoteCourseId);
//       mNote.setCourse(course);
//       mNote.setTitle(mViewModel.mOrgNoteTitle);
//       mNote.setText(mViewModel.mOrgNoteText);
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        if(outState!=null){
//            mViewModel.saveState(outState);
//        }
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }



    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);

        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while(more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if(courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValue() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        mIsNewNote = mNoteId == ID_NOT_SET;
        if(mIsNewNote) {

            createNewNote();
        }else{
            //mNote = DataManager.getInstance().getNotes().get(mNoteId);
            Log.i(TAG,"Note id: "+ mNoteId);
        }

    }

    private void createNewNote() {
//        DataManager dataManager =DataManager.getInstance();
//        mNoteId =  dataManager.createNewNote();

       // mNote=dataManager.getNotes().get(mNewPosition);

            ContentValues values = new ContentValues();
            values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
            values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
            values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

            SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
            mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);

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
        item.setEnabled(mNoteId <lastIndNote);

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote=DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalNoteValues();
        displayNote();
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
       // mCoursesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE, //col=0 in query
                        CourseInfoEntry.COLUMN_COURSE_ID,//col=1
                        CourseInfoEntry._ID //col=2
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

            }
        };
    }

    private CursorLoader createLoaderNotes() {
      //  mNotesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                        selection, selectionArgs, null, null, null);

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if(loader.getId() == LOADER_COURSES) {
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToFirst();

        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();

    }
    private void displayNoteWhenQueriesFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES) {
            if(mNoteCursor != null)
                mNoteCursor.close();
        } else if(loader.getId() == LOADER_COURSES) {
            mAdapterCourses.changeCursor(null);
        }
    }
}