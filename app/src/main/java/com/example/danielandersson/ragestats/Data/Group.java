package com.example.danielandersson.ragestats.Data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 */

public class Group implements Parcelable {
    private String mGroupName;
    private ArrayList<String> mMembers;
    private String mGroupKey;
    private ArrayList<Student> mStudents = new ArrayList<>();
    private Map<String, Boolean> mMembersMap = new HashMap<>();
    private String mStudentListKey;
    private Group mGroup;

    public Group() {
    }

    public Group(String groupName, ArrayList<String> members, ArrayList<Student> students) {
        mGroupName = groupName;
        mMembers = members;
        mStudents = students;
    }

    public Group(String name, ArrayList<String> memebers) {
        mMembers = memebers;
        mGroupName = name;
    }

    public String getStudentListKey() {
        return mStudentListKey;
    }

    public void setStudentListKey(String studentListKey) {
        mStudentListKey = studentListKey;
    }


    public Map<String, Boolean> getMembersMap() {
        return mMembersMap;
    }

    public void setMembersMap(Map<String, Boolean> membersMap) {
        mMembersMap = membersMap;
    }

    @Exclude
    public String getGroupKey() {
        return mGroupKey;
    }

    @Exclude
    public void setGroupKey(String groupKey) {
        mGroupKey = groupKey;
    }

    public ArrayList<String> getMembers() {
        return mMembers;
    }

    public void setMembers(ArrayList<String> members) {
        mMembers = members;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }
    @Exclude
    public ArrayList<Student> getStudents() {
        return mStudents;
    }
    @Exclude
    public void setStudents(ArrayList<Student> students) {
        mStudents = students;
    }

    public void addStudent(Student student) {
        if (mStudents== null) {
            mStudents = new ArrayList<>();
        }
        mStudents.add(student);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mGroupName);
        dest.writeStringList(this.mMembers);
        dest.writeString(this.mGroupKey);
        dest.writeList(this.mStudents);
        dest.writeInt(this.mMembersMap.size());
        for (Map.Entry<String, Boolean> entry : this.mMembersMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
        dest.writeString(this.mStudentListKey);
    }

    protected Group(Parcel in) {
        this.mGroupName = in.readString();
        this.mMembers = in.createStringArrayList();
        this.mGroupKey = in.readString();
        this.mStudents = new ArrayList<Student>();
        in.readList(this.mStudents, Student.class.getClassLoader());
        int mMembersMapSize = in.readInt();
        this.mMembersMap = new HashMap<String, Boolean>(mMembersMapSize);
        for (int i = 0; i < mMembersMapSize; i++) {
            String key = in.readString();
            Boolean value = (Boolean) in.readValue(Boolean.class.getClassLoader());
            this.mMembersMap.put(key, value);
        }
        this.mStudentListKey = in.readString();
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public void addMember(String memberKey, String memberName) {
        mMembersMap.put(memberKey, true);

        if (mMembers == null) {
            mMembers = new ArrayList<>();
        }
        mMembers.add(memberName);
    }

    public void setGroup(Group group) {
        mGroup = group;
    }
}
