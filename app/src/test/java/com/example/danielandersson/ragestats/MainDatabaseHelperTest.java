package com.example.danielandersson.ragestats;

import android.app.Application;
import android.content.Context;

import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by danielandersson on 2017-09-18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainDatabaseHelperTest {

    private MainDatabaseHelper mMainDatabaseHelper;

    @Before
    public void setUp() throws Exception {
        TestClass testClass = new TestClass();
        Application application = RuntimeEnvironment.application;
        Context applicationContext = application.getApplicationContext();
//        FirebaseApp.initializeApp(applicationContext);

        mMainDatabaseHelper = new MainDatabaseHelper(
                applicationContext,
                testClass,
                application.getSharedPreferences("you_custom_pref_name", Context.MODE_PRIVATE));
    }

    @Test
    public void insertMockGroup() throws Exception {
        mMainDatabaseHelper.insertStudent(new Student("test_student"), "test_key");
    }

    @After
    public void tearDown() throws Exception {

    }

    public class TestClass implements MainDatabaseHelper.OnAdapterCallBack{
        public TestClass() {

        }

        @Override
        public boolean addGroup(Group group) {
            return false;
        }

        @Override
        public void addStudent(Student student, String groupKey) {

        }

        @Override
        public Student getStudent(int studentPosition) {
            return null;
        }
    }
}