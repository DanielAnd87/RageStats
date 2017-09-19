package com.example.danielandersson.ragestats.ui.activites;

import android.content.Context;
import android.test.AndroidTestCase;

import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.MainDatabaseHelper;

import java.util.HashMap;

/**
 * Created by danielandersson on 2017-09-18.
 */

public class FirebaseTest extends AndroidTestCase {


    private MainDatabaseHelper mMainDatabaseHelper;
    private String mGroupKey = "test_key";
    private String mStudentKey = "-KuL1-C-VlVrPL5YFtDs";

    @Override
    public void setUp() throws Exception {
        TestClass testClass = new TestClass();
        Context applicationContext = getContext();
//        FirebaseApp.initializeApp(applicationContext);

        mMainDatabaseHelper = new MainDatabaseHelper(
                applicationContext,
                testClass,
                getContext().getSharedPreferences("you_custom_pref_name", Context.MODE_PRIVATE));


    }

//    public void testName() throws Exception {
//        mMainDatabaseHelper.insertStudent(new Student("test_student"), mGroupKey);
//        assertEquals(true, true);
//
//    }


//    public void testInsertData() throws Exception {
//        mMainDatabaseHelper.insertData(Utils.parseStringToSparseArray("2 60 4 54 5 72 "), mStudentKey);
//        assertEquals(true, true);
//
//    }


    public void testUpdateSmileyData() throws Exception {
        Student student = new Student();
        student.setLastDataSave(1505757717);

        HashMap<String, Long> hashMap = new HashMap<>();
        hashMap.put("-KuL8mZlKHhJnRZ71BxG", 1505757718830L);

        student.setDataKeyMap(hashMap);
        // FIXME: 2017-09-18 the smiley data is -50 wrong
        mMainDatabaseHelper.saveSmiley(student, 70);
    }

    public class TestClass implements MainDatabaseHelper.OnAdapterCallBack {
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
