package com.example.danielandersson.ragestats;

import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.StatData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by danielandersson on 2017-09-06.
 */

public class StatDatabaseHelper {

    private OnDatabaseResultListerner mListerner;
    private FirebaseDatabase mDatabase;
    private int mIndex;
    private String mStudentKey;


    public StatDatabaseHelper(FirebaseDatabase firebaseDatabase, OnDatabaseResultListerner listerner, String studentKey) {
        mDatabase = firebaseDatabase;
        mListerner = listerner;
        mStudentKey = studentKey;
    }
// TODO: 2017-09-06 use interface to send results to activity.

    public void insertData(SparseIntArray dataMap) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final String dataKey = dataReference.push().getKey();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());
        dataReference.child("student").child(mStudentKey).child("dataKeyMap").child(dataKey).setValue(true);
        dataReference.child("statData").child(dataKey).setValue(statData);
    }

    public void updateData(SparseIntArray dataMap, String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());
        dataReference.child("statData").child(dataKey).setValue(statData);
    }

    public void removeData(SparseIntArray dataMap, String dataKey) {
        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference.child("student").child(mStudentKey).child("dataKeyMap").child(dataKey).removeValue();
        dataReference.child("statData").child(dataKey).removeValue();
    }

    public void fetchData(String statDataKey) {

        final DatabaseReference dataReference = mDatabase.getReference();
        // FIXME: 2017-09-06 loop trough a keymap instead of a list.
        dataReference
                .child("statData")
                .child(statDataKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // adding data member list
                List<StatData> statDatas = dataSnapshot.getValue(new GenericTypeIndicator<List<StatData>>() {
                });

                mListerner.onDataFetched(statDatas);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface OnDatabaseResultListerner {
        void onDataFetched(List<StatData> statData);
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
    public Comment insertComment(String text, String userName ) {
        final DatabaseReference commentReference = mDatabase.getReference();
        final String commentKey = commentReference.push().getKey();
        mDatabase.getReference().child("student").child(mStudentKey).child(mIndex + "").child("commentsKeyMap").child(commentKey).setValue(true);

        final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
        comment.setTag(Utils.hashtagFinder(text));
        comment.setTimeAndDate();
        commentReference.child("comments").child(commentKey).setValue(comment);
        comment.setCommentKey(commentKey);
        return comment;
    }
}
