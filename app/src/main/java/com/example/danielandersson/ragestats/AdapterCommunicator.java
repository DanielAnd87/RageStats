package com.example.danielandersson.ragestats;

/**
 * Created by danielandersson on 2017-09-09.
 */

public interface AdapterCommunicator {
    void addMemberToGroup(String memberName, String memberKey);

    void getMembers();
}
