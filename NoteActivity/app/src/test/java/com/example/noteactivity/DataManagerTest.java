package com.example.noteactivity;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataManagerTest {

    static DataManager sDataManager;
    @BeforeClass
    public static void classSetUp(){ //method must be static
        sDataManager = DataManager.getInstance();
    }
    @Before
    public void setUp(){
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }
    @Test
    public void createNewNote() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String nTitle = "test note title";
        final String nText = "body of test note";

        int noteIndex = sDataManager.createNewNote();
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(nTitle);
        newNote.setText(nText);

        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
        assertEquals(course,compareNote.getCourse()); //expected , actual
        assertEquals(nTitle,compareNote.getTitle());
        assertEquals(nText,compareNote.getText());
    }
    @Test
    public void findSimilarNotes() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String nTitle = "test note title";
        final String nText1 = "body of test note";
        final String nText2 = "body of second test note";

        int noteIndex1 = sDataManager.createNewNote();
        NoteInfo newNote1 = sDataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(nTitle);
        newNote1.setText(nText1);


        int foundIndex1 = sDataManager.findNote(newNote1);
        assertEquals(noteIndex1,foundIndex1);

        int noteIndex2 = sDataManager.createNewNote();
        NoteInfo newNote2 = sDataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(nTitle);
        newNote2.setText(nText2);


        int foundIndex2 = sDataManager.findNote(newNote2);
        assertEquals(noteIndex2,foundIndex2);


    }
    @Test
    public void createNewNoteOneStepCreation(){
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String nTitle = "test note title";
        final String nText = "body of test note";

        int noteIndex = sDataManager.createNewNote(course,nTitle,nText);

        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        assertEquals(course,newNote.getCourse()); //expected , actual
        assertEquals(nTitle,newNote.getTitle());
        assertEquals(nText,newNote.getText());



    }

}