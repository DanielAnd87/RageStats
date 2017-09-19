package com.example.danielandersson.ragestats;

import android.appwidget.AppWidgetManager;
import android.util.Log;

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Member;
import com.example.danielandersson.ragestats.Data.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by danielandersson on 2017-08-14.
 */

public class DatabaseHelper {

    private final FirebaseDatabase mFirebaseDatabase;
    private final String mMyMemeberKey;
    private final AppWidgetManager mAppWidgetManager;
    private Member mMember;


    public DatabaseHelper(AppWidgetManager context, String keyString) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mAppWidgetManager = context;


        // the reference for the users own member profile.
        mMyMemeberKey = keyString;
    }

    public void fetchData(final Group group, String groupKey) {
        final DatabaseReference reference = mFirebaseDatabase.getReference("/members/" + mMyMemeberKey);


        final DatabaseReference reference1 = mFirebaseDatabase.getReference(Constants.PATH_GROUP + "/" + groupKey + "/");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group fetchedGroup = dataSnapshot.getValue(Group.class);
                Log.i(TAG, "onDataChange: "
                        + group.getGroupName());
                final String key = dataSnapshot.getKey();
                group.setGroup(fetchedGroup);
                group.setGroupKey(key);

                // FIXME: 2017-09-10 If you are going to use this class then fetch whit the hashset instead
                final DatabaseReference studentReference = mFirebaseDatabase.getReference(Constants.PATH_STUDENTS + "/" + "/");

                studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<Student> studentList = dataSnapshot.getValue(new GenericTypeIndicator<List<Student>>() {
                        });
                        group.setStudents((ArrayList<Student>) studentList);
                        mAppWidgetManager.notify();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
