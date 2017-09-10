package com.example.danielandersson.ragestats;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Member;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.ui.adapters.MyMainItemRecyclerViewAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danielandersson on 2017-08-14.
 */

public class MainDatabaseHelper {

    private final FirebaseDatabase mDatabase;
    private String mMyMemeberKey;
    private final Context mContext;
    private Member mMember;
    private MyMainItemRecyclerViewAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    public static final String TAG = MainDatabaseHelper.class.getSimpleName();


    public MainDatabaseHelper(FirebaseDatabase firebaseDatabase, Activity activity) {
        mDatabase = FirebaseDatabase.getInstance();

        mSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);

        mContext = activity;
        // the reference for the users own member profile.
    }

    public void setAdapter(MyMainItemRecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }

//    public void onSignedInInitialize(String userDisplayName, String userUid) {
//
//        mMyMemeberKey = mSharedPreferences.getString(Constants.KEY_MEMBER, "");
//        if (!mMyMemeberKey.equals(userUid)) {
//            final DatabaseReference membersReferens = mDatabase.getReference();
//            // TODO: 2017-08-02 Because the displayName is null it wont save
//            if (userDisplayName == null) {
//                userDisplayName = mContext.getString(R.string.label_user);
//            }
//            membersReferens.child("/members/" + userUid + "/memberName/").setValue(userDisplayName);
//
//            SharedPreferences.Editor editor = mSharedPreferences.edit();
//            editor.putString(Constants.KEY_MEMBER, userUid);
//            editor.putString(Constants.KEY_MEMBER_NAME, userDisplayName);
//            editor.apply();
//        }
//
//        // testing comment for git
//
//        // the reference for the users own member profile.
//        final DatabaseReference reference = mDatabase.getReference("/members/" + mMyMemeberKey);
//
//
////        reference.addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                mMember = dataSnapshot.getValue(Member.class);
////                try {
////                    // add the member object right here
////                    final HashMap<String, Boolean> hashMap = mMember.getGroupKeys();
////                    for (String key : hashMap.keySet()) {
////                        final DatabaseReference reference1 = mDatabase.getReference(Constants.PATH_GROUP + "/" + key + "/");
////                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
////                            @Override
////                            public void onDataChange(DataSnapshot dataSnapshot) {
////                                final Group group = dataSnapshot.getValue(Group.class);
////                                final String key = dataSnapshot.getKey();
////                                group.setGroupKey(key);
////
////                                mAdapter.addGroup(group);
////
////
////                                final DatabaseReference studentReference = mDatabase.getReference(Constants.PATH_STUDENTS + group.getStudentListKey() + "/");
////
////                                studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
////                                    @Override
////                                    public void onDataChange(DataSnapshot dataSnapshot) {
////                                        final List<Student> studentList = dataSnapshot.getValue(new GenericTypeIndicator<List<Student>>() {
////                                        });
////                                        if (studentList != null) {
////                                            mAdapter.addStudents(studentList, group.getGroupKey());
////                                        }
////                                    }
////
////                                    @Override
////                                    public void onCancelled(DatabaseError databaseError) {
////
////                                    }
////                                });
////
////                            }
////
////                            @Override
////                            public void onCancelled(DatabaseError databaseError) {
////
////                            }
////                        });
////
////                        // ...
////                    }
////                } catch (NullPointerException e) {
////                    e.printStackTrace();
////                }
////
////            }
////
////            @Override
////            public void onCancelled(DatabaseError databaseError) {
////
////            }
////        });
//
//
//    }

    public void onSignedInInitialize(String userDisplayName, String userUid) {

        mMyMemeberKey = mSharedPreferences.getString(Constants.KEY_MEMBER, "");
        if (!mMyMemeberKey.equals(userUid)) {
            final DatabaseReference membersReferens = mDatabase.getReference();
            // TODO: 2017-08-02 Because the displayName is null it wont save
            if (userDisplayName == null) {
                userDisplayName = mContext.getString(R.string.label_user);
            }
            membersReferens.child("/members/" + userUid + "/memberName/").setValue(userDisplayName);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.KEY_MEMBER, userUid);
            editor.putString(Constants.KEY_MEMBER_NAME, userDisplayName);
            editor.apply();
        }

        // testing comment for git

        // the reference for the users own member profile.
        final DatabaseReference reference = mDatabase.getReference("/members/" + mMyMemeberKey);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMember = dataSnapshot.getValue(Member.class);
                try {
                    // add the member object right here
                    final DatabaseReference reference1 = mDatabase.getReference();
                    final Query query = reference1.child("group").orderByKey();
                    final HashMap<String, Boolean> hashMap = mMember.getGroupKeys();
                    for (String key : hashMap.keySet()) {
                        query.equalTo(key);
                    }

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            final Group group = dataSnapshot.getValue(Group.class);
                            final String key = dataSnapshot.getKey();
                            group.setGroupKey(key);

                            final boolean groupWasAdded = mAdapter.addGroup(group);
                            if (groupWasAdded) {
                                Log.i(TAG, "onChildAdded: query success for: " + group.getGroupName());

                                final DatabaseReference studentReference = mDatabase.getReference(Constants.PATH_STUDENTS + group.getStudentListKey() + "/");

                                studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final List<Student> studentList = dataSnapshot.getValue(new GenericTypeIndicator<List<Student>>() {
                                        });
                                        if (studentList != null) {
                                            mAdapter.addStudents(studentList, group.getGroupKey());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void insertComment(String text, int studentPosition) {
        final DatabaseReference commentReference = mDatabase.getReference();
        final String key = commentReference.push().getKey();
        final Student student = mAdapter.getStudent(studentPosition);
        student.addCommentKey(key);

        mDatabase.getReference().child("student").child(mAdapter.getStudentKey(studentPosition)).child(mAdapter.getStudentPos(studentPosition) + "").child("commentsKeyMap").setValue(student.getCommentsKeyMap());
        String userName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
        final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
        comment.setTimeAndDate();
        commentReference.child("comments").child(key).setValue(comment);
    }

    public void saveStudent(String name, int groupPos) {
        // getting reference for db
        final DatabaseReference reference = mDatabase.getReference();
        final String studentKey = reference.push().getKey();
        // setting keys for comments and statData
        final Student student = new Student(name);
        // FIXME: 2017-09-06 the statdata needs its a keyset instead of a list
        // adding to list adapter
        final Group group = mAdapter.addStudent(name, groupPos);
        // adding the student object to db list
        reference.child(Constants.PATH_STUDENTS).child(group.getStudentListKey()).child((group.getStudents().size() - 1) + "").setValue(student);
    }

    public void updateGroup(String key, String name, List<String> members) {
        Map<String, Boolean> membersMap = new HashMap<>();
        for (String member : members) {
            membersMap.put(member, true);
        }
        final DatabaseReference reference = mDatabase.getReference();
        reference.child("group").child(key + "/groupName/").setValue(name);
        reference.child("group").child(key + "/membersMap/").setValue(membersMap);
        updateMembersGroupMap(key, membersMap, reference);
    }

    private void updateMembersGroupMap(String key, Map<String, Boolean> membersMap, DatabaseReference reference) {
        // updating its members key maps in database
        for (String keyString : membersMap.keySet()) {
            reference.child("members").child(keyString).child("groupKeys").child(key).setValue(true);
        }
    }

    public void saveSmiley(int smileyIndex, Group group) {

        // TODO: 2017-08-23 get the data from the map
        // TODO: 2017-08-23 if no data then create a clean data.
        // TODO: 2017-08-23 check if contains current hour.
        // TODO: 2017-08-23 update the current hour if exist.
        // TODO: 2017-08-23 save the last hour that was saved, if nothing was saved then no need to fetch it.
        // TODO: 2017-08-23 use the method in Util to check if it has been saved today.
//        reference.child(Constants.PATH_STUDENTS).child(group.getStudentListKey()).child((group.getStudents().size() - 1) + "").setValue(student);

    }


    public void insertGroup(Group group, Student student) {
        group.addStudent(student);

        // Adding a student list to the database and saving the key
        final DatabaseReference reference = mDatabase.getReference();
        final String studentKey = reference.push().getKey();

        final ArrayList<Student> students = new ArrayList<Student>();
        // setting new key references for comments and data
        students.add(student);
        reference.child("student").child(studentKey).setValue(students);

        // FIXME: 2017-07-30 the student isnt saved

        group.setStudentListKey(studentKey);
        // TODO: 2017-07-30 send intire group when starting AddGroupFragment

        // saving to Firebase Realtime Database.
        final DatabaseReference groupReference = mDatabase.getReference();
        group.setGroupKey(groupReference.push().getKey());
        groupReference.child("group").child(group.getGroupKey()).setValue(group);
        // updating its members key maps in database
        updateMembersGroupMap(group.getGroupKey(), group.getMembersMap(), groupReference);
        mAdapter.addGroup(group);
        // TODO: 2017-07-30 add student
    }

}



