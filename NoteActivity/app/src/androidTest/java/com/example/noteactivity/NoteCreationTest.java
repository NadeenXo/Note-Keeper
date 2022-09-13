package com.example.noteactivity;

import static org.junit.Assert.*;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import  static org.hamcrest.Matchers.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.*;




@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    static DataManager sDataManager;
    @BeforeClass
    public static void classSetUp(){ //method must be static
        sDataManager = DataManager.getInstance();
    }
    @Rule //create instance before test and del it after it
    //ActivityTestRule
    public ActivityScenarioRule<NoteListActivity> mNoteListActivityRule = new ActivityScenarioRule<>(NoteListActivity.class);


    @Test
    public void createNewNote(){
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "test note title";
        final String noteText = "body of test note";

//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_cources)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class),equalTo(course))).perform(click());
        onView(withId(R.id.spinner_cources)).check(matches(withSpinnerText(containsString(course.getTitle()))));

        onView(withId(R.id.text_note_title)).perform(typeText(noteTitle))
                        .check(matches(withText(containsString(noteTitle))));

        onView(withId(R.id.text_note_text)).perform(typeText(noteText), closeSoftKeyboard());
        onView(withId(R.id.text_note_text)).check(matches(withText(containsString(noteText))));

        pressBack();

        //ensure that note created correctly
        int noteIndex = sDataManager.getNotes().size()-1;
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        assertEquals(course,newNote.getCourse());
        assertEquals(noteTitle,newNote.getTitle());
        assertEquals(noteText,newNote.getText());


    }
}















