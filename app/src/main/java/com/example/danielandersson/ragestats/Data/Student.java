package com.example.danielandersson.ragestats.Data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by danielandersson on 2017-07-10.
 */

public class Student implements Parcelable {


    private String mStudentKey;
    private String mName;
    private HashMap<String, Boolean> mCommentsKeyMap;
    private HashMap<String, Long> mDataKeyMap;
    private long mLastDataSave;

    public Student(String name) {
        mName = name;
    }

    public Student(String name, HashMap<String, Long> dataKeyMap) {
        mName = name;
        mDataKeyMap = dataKeyMap;
    }

    public Student() {
    }

    public long getLastDataSave() {
        return mLastDataSave;
    }

    public void setLastDataSave(long lastDataSave) {
        mLastDataSave = lastDataSave;
    }

    @Exclude
    public String getStudentKey() {
        return mStudentKey;
    }

    @Exclude
    public void setStudentKey(String studentKey) {
        mStudentKey = studentKey;
    }

    public HashMap<String, Boolean> getCommentsKeyMap() {
        return mCommentsKeyMap;
    }

    public void setCommentsKeyMap(HashMap<String, Boolean> commentsKeyMap) {
        mCommentsKeyMap = commentsKeyMap;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public HashMap<String, Long> getDataKeyMap() {
        if (mDataKeyMap == null) {
            mDataKeyMap = new HashMap<String, Long>();
        }
        return mDataKeyMap;
    }

    public void setDataKeyMap(HashMap<String, Long> dataKeyMap) {
        mDataKeyMap = dataKeyMap;
    }

    public void addCommentKey(String key) {
        if (mCommentsKeyMap == null) {
            mCommentsKeyMap = new HashMap<String, Boolean>();
        }

        mCommentsKeyMap.put(key, true);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStudentKey);
        dest.writeString(this.mName);
        dest.writeSerializable(this.mCommentsKeyMap);
        dest.writeSerializable(this.mDataKeyMap);
        dest.writeLong(this.mLastDataSave);
    }

    protected Student(Parcel in) {
        this.mStudentKey = in.readString();
        this.mName = in.readString();
        this.mCommentsKeyMap = (HashMap<String, Boolean>) in.readSerializable();
        this.mDataKeyMap = (HashMap<String, Long>) in.readSerializable();
        this.mLastDataSave = in.readLong();
    }

    public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel source) {
            return new Student(source);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };
}
