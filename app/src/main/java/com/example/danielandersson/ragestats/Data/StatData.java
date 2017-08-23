package com.example.danielandersson.ragestats.Data;

import android.util.SparseIntArray;

import com.example.danielandersson.ragestats.Utils;
import com.google.firebase.database.Exclude;

/**
 * Created by danielandersson on 2017-07-12.
 */

public class StatData {
    private String mStudentKey;
    private long mTimeStamp;
    private String mDataString;
    private SparseIntArray mDataMap;

    public StatData() {
    }

    public StatData(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public StatData(String dataMap, long timestamp) {
        mDataString = dataMap;
        mDataMap = Utils.parseStringToSparseArray(dataMap);
        mTimeStamp = timestamp;
    }

    public String getDataString() {
        return mDataString;
    }

    public void setDataString(String dataString) {
        mDataString = dataString;
    }

    @Exclude
    public SparseIntArray getDataMap() {
        return mDataMap;
    }

    @Exclude
    public void setDataMap(SparseIntArray dataMap) {
        mDataMap = dataMap;
    }

    public String getStudentKey() {
        return mStudentKey;
    }

    public void setStudentKey(String studentKey) {
        mStudentKey = studentKey;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
}
