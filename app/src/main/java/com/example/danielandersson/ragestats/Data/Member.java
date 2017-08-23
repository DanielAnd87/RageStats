package com.example.danielandersson.ragestats.Data;

import java.util.HashMap;

/**
 * Created by danielandersson on 2017-07-26.
 */

public class Member {
    private HashMap<String, Boolean> mGroupKeys;
    private String memberName;

    public HashMap<String, Boolean> getGroupKeys() {
        return mGroupKeys;
    }

    public void setGroupKeys(HashMap<String, Boolean> groupKeys) {
        mGroupKeys = groupKeys;
    }

    public Member(String memberName) {
        this.memberName = memberName;
    }

    public Member() {

    }

    public String getMemberName() {

        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}
