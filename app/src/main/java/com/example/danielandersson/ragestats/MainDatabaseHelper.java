package com.example.danielandersson.ragestats;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseIntArray;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Member;
import com.example.danielandersson.ragestats.Data.StatData;
import com.example.danielandersson.ragestats.Data.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by danielandersson on 2017-08-14.
 */

public class MainDatabaseHelper {

    private final FirebaseDatabase mDatabase;
    private final OnAdapterCallBack mListener;
    private String mMyMemeberKey;
    private final Context mContext;
    private Member mMember;
    private SharedPreferences mSharedPreferences;
    public static final String TAG = MainDatabaseHelper.class.getSimpleName();


    public MainDatabaseHelper(Context context, OnAdapterCallBack listener, SharedPreferences preferences) {
        mDatabase = FirebaseDatabase.getInstance();
        mListener = listener;

        mSharedPreferences = preferences;

        mContext = context;
        // the reference for the users own member profile.
    }


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

        fetchMembers();
    }

    public void fetchMembers() {
        final DatabaseReference reference = mDatabase.getReference("/members/" + mMyMemeberKey);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMember = dataSnapshot.getValue(Member.class);
                try {
                    fetchGroups(mMember.getGroupKeys());

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void fetchGroups(HashMap<String, Boolean> hashMap) {
        for (String key : hashMap.keySet()) {
            fetchGroup(key);
        }
    }

    public void fetchGroup(String groupKey) {
        mDatabase.getReference().child("group").child(groupKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Group group = dataSnapshot.getValue(Group.class);
                final String key = dataSnapshot.getKey();
                group.setGroupKey(key);

                boolean groupWasAdded = mListener.addGroup(group);
                if (groupWasAdded) {
                    Log.i(TAG, "onChildAdded: query success for: " + group.getGroupName());

                    fetchStudents(group, group.getStudentMap());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void fetchStudents(final Group group, Map<String, Boolean> studentMap) {
        if (studentMap != null || studentMap.size() > 0) {
            for (String keyString : studentMap.keySet()) {
                mDatabase.
                        getReference().
                        child("student").
                        child(keyString).
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Student student = dataSnapshot.getValue(Student.class);
                                student.setStudentKey(dataSnapshot.getKey());
                                mListener.addStudent(student, group.getGroupKey());

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

        }
    }

    public void insertComment(String text, int studentPosition) {
        final DatabaseReference commentReference = mDatabase.getReference();
        final String key = commentReference.push().getKey();
        final Student student = mListener.getStudent(studentPosition);
        student.addCommentKey(key);

        mDatabase.getReference()
                .child("student")
                .child(student.getStudentKey())
                .child("commentsKeyMap").setValue(student.getCommentsKeyMap());
        String userName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
        final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
        comment.setTimeAndDate();
        commentReference.child("comments").child(key).setValue(comment);
    }

    public String insertStudent(Student student, String groupKey) {
        // getting reference for db
        final DatabaseReference reference = mDatabase.getReference();
        final String studentKey = reference.push().getKey();
        reference
                .child("group")
                .child(groupKey)
                .child("studentMap")
                .child(studentKey)
                .setValue(true);

        mDatabase.getReference()
                .child("student")
                .child(studentKey)
                .setValue(student);
        return studentKey;
    }

    public void updateStudent(SparseIntArray dataMap, Student student) {
        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference.child("student").child(student.getStudentKey()).setValue(student);
    }

    public void removeData(String studentKey, String groupKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference.child("group").child(groupKey).child("studentMap").child(studentKey).removeValue();

        dataReference.child("student").child(studentKey).removeValue();
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

    public void saveSmiley(Student student, int smileyValue) {
        int hour = Utils.getThisHour(Utils.getCurrentTimestamp());
        HashMap<String, Long> dataKeyMap = student.getDataKeyMap();
        boolean isFound = false;
        if (dataKeyMap.size()>0) {
            Iterator<String> iterator = dataKeyMap.keySet().iterator();
            isFound = false;
            while (iterator.hasNext() && !isFound) {
                final String firstKey = iterator.next();
                if (Utils.isTimestampToday(dataKeyMap.get(firstKey) / 1000)) {
                    updateSmileyData(firstKey, hour, smileyValue);
                    isFound = true;
                }
            }
        }

        if (!isFound) {
            final SparseIntArray sparseIntArray = new SparseIntArray();
            sparseIntArray.put(hour, smileyValue);
            insertData(sparseIntArray, student.getStudentKey());
        }
    }


    public String insertData(SparseIntArray dataMap, String studentKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final String dataKey = dataReference.push().getKey();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());


        dataReference
                .child("student")
                .child(studentKey)
                .child("dataKeyMap")
                .child(dataKey)
                .setValue(ServerValue.TIMESTAMP);
        mDatabase.getReference()
                .child("statData")
                .child(dataKey)
                .setValue(statData);

        updateLastInsert(studentKey, dataKey);
        return dataKey;
    }

    private void updateLastInsert(String studentKey, String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        long currentTimestamp = Utils.getCurrentTimestamp();
        dataReference.child("student").child(studentKey).child("lastDataSave").setValue(currentTimestamp);
        // updating the student in the list as well.
        mListener.updateStudent(studentKey, dataKey);
    }


    public void insertGroup(Group group, Student student) {
        group.addStudent(student);


        // saving to Firebase Realtime Database.
        final DatabaseReference groupReference = mDatabase.getReference();
        group.setGroupKey(groupReference.push().getKey());
        groupReference.child("group").child(group.getGroupKey()).setValue(group);
        // updating its members key maps in database
        insertStudent(student, group.getGroupKey());
        updateMembersGroupMap(group.getGroupKey(), group.getMembersMap(), groupReference);
        mListener.addGroup(group);
    }


    public void updateData(SparseIntArray dataMap, String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());
        dataReference.child("statData").child(dataKey).setValue(statData);
    }

    private void updateSmileyData(final String dataKey, final int hour, final int value) {

        // TODO: 2017-09-10 fetch the student and its keymap
        final DatabaseReference reference = mDatabase.getReference().child("statData").child(dataKey).child("dataString");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String statDatas = dataSnapshot.getValue(String.class);

                final SparseIntArray sparseIntArray = Utils.parseStringToSparseArray(statDatas);
                // TODO: 2017-09-11 add value
                sparseIntArray.put(hour, value);

                updateData(sparseIntArray, dataKey);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public interface OnAdapterCallBack {
        boolean addGroup(Group group);

        void addStudent(Student student, String groupKey);

        Student getStudent(int studentPosition);

        void updateStudent(String studentKey, String dataKey);
    }
}



