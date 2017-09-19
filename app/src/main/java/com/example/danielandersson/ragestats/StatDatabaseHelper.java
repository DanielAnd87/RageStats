package com.example.danielandersson.ragestats;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.StatData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by danielandersson on 2017-09-06.
 */

public class StatDatabaseHelper {

    private static final String TAG = StatDatabaseHelper.class.getSimpleName();
    private OnDatabaseResultListener mListerner;
    private FirebaseDatabase mDatabase;
    private String mStudentKey;


    public StatDatabaseHelper(FirebaseDatabase firebaseDatabase, OnDatabaseResultListener listerner, String studentKey) {
        mDatabase = firebaseDatabase;
        mListerner = listerner;
        mStudentKey = studentKey;
    }
// TODO: 2017-09-06 use interface to send results to activity.

    public String insertData(SparseIntArray dataMap) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final String dataKey = dataReference.push().getKey();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());


        dataReference
                .child("student")
                .child(mStudentKey)
                .child("dataKeyMap")
                .child(dataKey)
                .setValue(ServerValue.TIMESTAMP);
        mDatabase.getReference()
                .child("statData")
                .child(dataKey)
                .setValue(statData);

        updateLastInsert();
        return dataKey;
    }

    private void updateLastInsert() {
        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference.child("student").child(mStudentKey).child("lastDataSave").setValue(Utils.getCurrentTimestamp());
    }


    public void updateData(SparseIntArray dataMap, String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());
        dataReference.child("statData").child(dataKey).setValue(statData);
    }

    public void removeData(String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference
                .child("student")
                .child(mStudentKey)
                .child("dataKeyMap")
                .child(dataKey)
                .removeValue();
        dataReference.child("statData").child(dataKey).removeValue();
    }

    public void fetchData(HashMap<String, Long> hashMap, Calendar instance, final boolean dayInterval) {

        if (hashMap != null) {
            for (String dataKey : hashMap.keySet()) {

                Long value = hashMap.get(dataKey) / 1000;

                boolean isInRange = dayInterval ?
                                Utils.getDayStartTimestamp(instance) < value ||
                                Utils.getDayEndTimestamp(instance) > value
                                :
                                Utils.getMonthStartTimestamp(instance) < value ||
                                Utils.getNextMonthStartTimestamp(instance) > value;
                if (isInRange) {
                    mDatabase.getReference().child("statData").child(dataKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final StatData statData = dataSnapshot.getValue(StatData.class);
                            final String digitalTime = Utils.formatDigitalTime(statData.getTimeStamp());
                            statData.setDataKey(dataSnapshot.getKey());
                            statData.setDataMap(Utils.parseStringToSparseArray(statData.getDataString()));
                            Log.i(TAG, "onChildAdded: Data date: " + digitalTime + " whit key: " + statData.getDataKey() + " and data is: " + statData.getDataString());
                            if (dayInterval) {
                                mListerner.onShortDataFetched(statData);
                            } else {
                                mListerner.onLongDataFetched(statData);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        }

    }

    public interface OnDatabaseResultListener {
        void onDataFetched(List<StatData> statData);

        void onShortDataFetched(StatData statData);

        void onLongDataFetched(StatData statDatas);

        void onCommentFetched(Comment comment);
    }


    public void fetchComment(HashMap<String, Boolean> hashMap) {
        if (hashMap != null) {
            for (String key : hashMap.keySet()) {
                final DatabaseReference reference = mDatabase.getReference().child("comments").child(key);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Comment comment = dataSnapshot.getValue(Comment.class);

                        mListerner.onCommentFetched(comment);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        }
    }


    @NonNull
    public Comment insertComment(String text, String userName) {
        final DatabaseReference commentReference = mDatabase.getReference();
        final String commentKey = commentReference.push().getKey();
        mDatabase.getReference()
                .child("student")
                .child(mStudentKey)
                .child("commentsKeyMap")
                .child(commentKey)
                .setValue(true);

        final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
        comment.setTag(Utils.hashtagFinder(text));
        comment.setTimeAndDate();
        commentReference.child("comments").child(commentKey).setValue(comment);
        comment.setCommentKey(commentKey);
        return comment;
    }
}
