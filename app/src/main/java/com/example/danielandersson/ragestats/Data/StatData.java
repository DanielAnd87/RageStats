package com.example.danielandersson.ragestats.Data;

import android.util.SparseIntArray;

import com.example.danielandersson.ragestats.Utils;
import com.google.firebase.database.Exclude;

import java.util.Calendar;

/**
 *
 * Created by danielandersson on 2017-07-12.
 */

public class StatData {
    private long mTimeStamp;
    private String mDataString;
    private SparseIntArray mDataMap;
    private Calendar mCalendar;
    private String mDataKey;
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

    @Exclude
    public Calendar getCalendar() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(mTimeStamp);
        }
        return mCalendar;
    }

    @Exclude
    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    @Exclude
    public String getDataKey() {
        if (mDataKey == null) {
            mDataKey = new String();
        }
        return mDataKey;
    }

    @Exclude
    public void setDataKey(String dataKey) {
        mDataKey = dataKey;
        mDataMap = Utils.parseStringToSparseArray(dataKey);
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

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
}
