package com.example.danielandersson.ragestats.Data;

import com.example.danielandersson.ragestats.Utils;
import com.google.firebase.database.Exclude;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Comment {
    private String mComment;
    private List<String> mTag;
    private long mTimeStamp;
    private String mMemberName;
    private String mDate;
    private String mTime;
    private String mStudentKey;
    private String mCommentKey;

    @Exclude
    public String getCommentKey() {
        return mCommentKey;
    }

    @Exclude
    public void setCommentKey(String commentKey) {
        mCommentKey = commentKey;
    }

    public Comment(String comment, long timeStamp, String memberName) {
        mComment = comment;
        mTimeStamp = timeStamp;
        mMemberName = memberName;
        setTimeAndDate();
    }

    public Comment() {
    }

    public String getStudentKey() {
        return mStudentKey;
    }

    public void setStudentKey(String studentKey) {
        mStudentKey = studentKey;
    }

    public List<String> getTags() {
        return mTag;
    }

    public void setTag(List<String> tag) {
        mTag = tag;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public String getMemberName() {
        return mMemberName;
    }

    public void setMemberName(String memberName) {
        mMemberName = memberName;
    }

    @Exclude
    public String getDate() {
        return mDate;
    }

    @Exclude
    public void setDate(String date) {
        mDate = date;
    }

    @Exclude
    public String getTime() {
        return mTime;
    }

    @Exclude
    public void setTime(String time) {
        mTime = time;
    }

    public void setTimeAndDate() {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        Date date = new Date(mTimeStamp);
        mTime = Utils.formatDigitalTime(mTimeStamp);
        mDate = dateFormat.format(date);


    }

}
