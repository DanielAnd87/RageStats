package com.example.danielandersson.ragestats.ui.activites;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Member;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.Utils;
import com.example.danielandersson.ragestats.ui.adapters.MyMainItemRecyclerViewAdapter;
import com.example.danielandersson.ragestats.ui.fragment.AddStudentFragment;
import com.example.danielandersson.ragestats.ui.fragment.CommentFragment;
import com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment;
import com.example.danielandersson.ragestats.ui.fragment.MainItemFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment.newInstance;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity
        implements
        MainItemFragment.OnListFragmentInteractionListener,
        GroupDialogFragment.OnComfirmed,
        AddStudentFragment.OnFragmentInteractionListener,
        CommentFragment.OnFragmentInteractionListener {

    private static final String GROUP_FRAGMENT_TAG = GroupDialogFragment.class.getSimpleName();
    private static final String STUDENT_FRAGMENT_TAG = AddStudentFragment.class.getSimpleName();
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String COMMENT_FRAGMENT_TAG = CommentFragment.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private MyMainItemRecyclerViewAdapter mAdapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseGroupReference;
    private ChildEventListener mChildEventListener;
    private String mTemporaryStudentName;
    private SharedPreferences mSharedPreferences;
    private String mMyMemeberKey;
    private Member mMyMemberValue;
    private Member mMember;
    private CommentFragment mCommentFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();
        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();


        // TODO: 2017-08-02 testint querruing
//
//        final DatabaseReference reference = mDatabase.getReference();
//        reference.child("members").orderByChild("memberName").addChildEventListener()


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName(), user.getUid());
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    private void onSignedInInitialize(String userDisplayName, String userUid) {

        mMyMemeberKey = mSharedPreferences.getString(Constants.KEY_MEMBER, "");
        if (!mMyMemeberKey.equals(userUid)) {
            final DatabaseReference membersReferens = mDatabase.getReference();
            // TODO: 2017-08-02 Because the displayName is null it wont save
            if (userDisplayName == null) {
                userDisplayName = getString(R.string.label_user);
            }
            membersReferens.child("/members/" + userUid + "/memberName/").setValue(userDisplayName);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.KEY_MEMBER, userUid);
            editor.putString(Constants.KEY_MEMBER_NAME, userDisplayName);
            editor.apply();
        }

        // testing comment for git

        // the reference for the users own member profile.
        final DatabaseReference reference = mDatabase.getReference("/members/" + mMyMemeberKey);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMember = dataSnapshot.getValue(Member.class);
                try {
                    // add the member object right here
                    final HashMap<String, Boolean> hashMap = mMember.getGroupKeys();
                    for (String key : hashMap.keySet()) {
                        final DatabaseReference reference1 = mDatabase.getReference(Constants.PATH_GROUP + "/" + key + "/");
                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Group group = dataSnapshot.getValue(Group.class);
                                final String key = dataSnapshot.getKey();
                                group.setGroupKey(key);

                                mAdapter.addGroup(group);


                                final DatabaseReference studentReference = mDatabase.getReference(Constants.PATH_STUDENTS + "/" + group.getStudentListKey() + "/");

                                studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final List<Student> studentList = dataSnapshot.getValue(new GenericTypeIndicator<List<Student>>() {
                                        });
                                        mAdapter.addStudents(studentList, group.getGroupKey());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // ...
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFragmentManager.findFragmentByTag(STUDENT_FRAGMENT_TAG) == null) {
                    mTransaction = mFragmentManager.beginTransaction();

                    final ArrayList<Group> groups = mAdapter.getGroups();
                    final String[] groupStrings = new String[groups.size()];
                    for (int i = 0; i < groups.size(); i++) {
                        groupStrings[i] = groups.get(i).getGroupName();

                    }
                    final AddStudentFragment addStudentFragment = AddStudentFragment.newInstance(groupStrings);
                    mTransaction
                            .add(R.id.main_container, addStudentFragment, STUDENT_FRAGMENT_TAG)
                            .addToBackStack(STUDENT_FRAGMENT_TAG)
                            .commit();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        // TODO: 2017-08-02 detach all listerners here
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Student student, String groupKey, String studentKey, int studentIndex) {
        StatisticsActivity.start(MainActivity.this, studentIndex, student, groupKey, studentKey);
    }

    @Override
    public void onEditBtnClick(Group group) {
        if (mFragmentManager.findFragmentByTag(GROUP_FRAGMENT_TAG) == null) {
            mTransaction = mFragmentManager.beginTransaction();

            final GroupDialogFragment groupDialogFragment = newInstance(group, true);

            mTransaction
                    .add(R.id.main_container, groupDialogFragment, GROUP_FRAGMENT_TAG)
                    .addToBackStack(GROUP_FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void pairAdapters(MyMainItemRecyclerViewAdapter adapter) {
        mAdapter = adapter;
        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void startCommentDialog(int studentIndex) {
        if (mFragmentManager.findFragmentByTag(COMMENT_FRAGMENT_TAG) == null) {
            mTransaction = mFragmentManager.beginTransaction();

            mCommentFragment = CommentFragment.newInstance(studentIndex);

            mTransaction
                    .add(R.id.main_container, mCommentFragment, GROUP_FRAGMENT_TAG)
                    .addToBackStack(GROUP_FRAGMENT_TAG)
                    .commit();
        }
    }

    private void startAddGroupFragment(Group group, boolean isUpdating) {
        if (mFragmentManager.findFragmentByTag(GROUP_FRAGMENT_TAG) == null) {
            mTransaction = mFragmentManager.beginTransaction();


            if (group.getMembers() == null) {
                SharedPreferences mSharedPreferences = getPreferences(Context.MODE_PRIVATE);
                final String memberKey = mSharedPreferences.getString(Constants.KEY_MEMBER, "");
                final String memberName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
                group.addMember(memberKey, memberName);
            }

            final GroupDialogFragment groupDialogFragment = GroupDialogFragment.newInstance(group, isUpdating);
            mTransaction
                    .add(R.id.main_container, groupDialogFragment, GROUP_FRAGMENT_TAG)
                    .addToBackStack(GROUP_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onConfirmInsertGroup(Group group) {

        android.support.v4.app.Fragment addGroupFragment = mFragmentManager.findFragmentByTag(GROUP_FRAGMENT_TAG);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        if (addGroupFragment != null) {
            final Student student = new Student(mTemporaryStudentName);
            group.addStudent(student);

            // Adding a student list to the database and saving the key
            final DatabaseReference reference = mDatabase.getReference();
            final String studentKey = reference.push().getKey();

            final ArrayList<Student> students = new ArrayList<Student>();
            // setting new key references for comments and data
            student.setStatDataKey(reference.push().getKey());
            students.add(student);
            reference.child("student").child(studentKey).setValue(students);

            // FIXME: 2017-07-30 the student isnt saved
            mTemporaryStudentName = null;

            transaction.remove(addGroupFragment);
            transaction.commit();

            group.setStudentListKey(studentKey);
            // TODO: 2017-07-30 send intire group when starting AddGroupFragment

            // saving to Firebase Realtime Database.
            final DatabaseReference groupReference = mDatabase.getReference();
            group.setGroupKey(groupReference.push().getKey());
            groupReference.child("group").child(group.getGroupKey()).setValue(group);
            // updating its members key maps in database
            updateMembersGroupMap(group.getGroupKey(), group.getMembersMap(), groupReference);
            mAdapter.addGroup(group);
            // TODO: 2017-07-30 add student

        } else

        {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConfirmUpdateGroup(String key, String name, List<String> members) {

        android.support.v4.app.Fragment addGroupFragment = mFragmentManager.findFragmentByTag(GROUP_FRAGMENT_TAG);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (addGroupFragment != null) {
            // FIXME: 2017-07-30 my dialogs doesnt close properly and derefore cant be opened whitout pressing the backbutton first.
            transaction.remove(addGroupFragment);
            transaction.commit();
            mAdapter.updateGroup(key, name);
            // saving to Firebase Realtime Database.
            Map<String, Boolean> membersMap = new HashMap<>();
            for (String member : members) {
                membersMap.put(member, true);
            }
            final DatabaseReference reference = mDatabase.getReference();
            reference.child("group").child(key + "/groupName/").setValue(name);
            reference.child("group").child(key + "/membersMap/").setValue(membersMap);
            updateMembersGroupMap(key, membersMap, reference);
        } else {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMembersGroupMap(String key, Map<String, Boolean> membersMap, DatabaseReference reference) {
        // updating its members key maps in database
        for (String keyString : membersMap.keySet()) {
            reference.child("members").child(keyString).child("groupKeys").child(key).setValue(true);
        }
    }


    @Override
    public void onSaveStudent(String name, int groupPos) {

        if (groupPos < mAdapter.getGroups().size()) {
            // getting reference for db
            final DatabaseReference reference = mDatabase.getReference();
            // setting keys for comments and statData
            final Student student = new Student(name);
            student.setStatDataKey(reference.push().getKey());
            // adding to list adapter
            final Group group = mAdapter.addStudent(name, groupPos);
            // adding the student object to db list
            reference.child(Constants.PATH_STUDENTS).child(group.getStudentListKey()).child((group.getStudents().size() - 1) + "").setValue(student);


        } else {
            mTemporaryStudentName = name;
            startAddGroupFragment(new Group(), false);
        }


        final android.support.v4.app.Fragment addFragment = mFragmentManager.findFragmentByTag(STUDENT_FRAGMENT_TAG);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        if (addFragment != null) {
            transaction.remove(addFragment);
            transaction.commit();
        } else {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddCommentToDatabase(String text, int studentPosition) {
        final DatabaseReference commentReference = mDatabase.getReference();
        final String key = commentReference.push().getKey();
        final Student student = mAdapter.getStudent(studentPosition);
        student.addCommentKey(key);

        mDatabase.getReference().child("student").child(mAdapter.getStudentKey(studentPosition)).child(mAdapter.getStudentPos(studentPosition) + "").child("commentsKeyMap").setValue(student.getCommentsKeyMap());
        String userName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
        final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
        comment.setTimeAndDate();
        commentReference.child("comments").child(key).setValue(comment);


        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Toast.makeText(this, "'" + text + "'" + getString(R.string.toast_text), Toast.LENGTH_SHORT).show();


        if (mCommentFragment != null) {
            transaction.remove(mCommentFragment);
            transaction.commit();
        } else {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }
    }
}
