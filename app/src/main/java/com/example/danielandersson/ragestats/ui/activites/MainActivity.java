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

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.MainDatabaseHelper;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.adapters.MyMainItemRecyclerViewAdapter;
import com.example.danielandersson.ragestats.ui.fragment.AddStudentFragment;
import com.example.danielandersson.ragestats.ui.fragment.CommentFragment;
import com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment;
import com.example.danielandersson.ragestats.ui.fragment.MainItemFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import static com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment.newInstance;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity
        implements
        MainItemFragment.OnListFragmentInteractionListener,
        GroupDialogFragment.OnComfirmed,
        AddStudentFragment.OnFragmentInteractionListener,
        CommentFragment.OnFragmentInteractionListener,
        MainDatabaseHelper.OnAdapterCallBack
{

    private static final String GROUP_FRAGMENT_TAG = GroupDialogFragment.class.getSimpleName();
    private static final String STUDENT_FRAGMENT_TAG = AddStudentFragment.class.getSimpleName();
    private static final String COMMENT_FRAGMENT_TAG = CommentFragment.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private MyMainItemRecyclerViewAdapter mAdapter;
    private String mTemporaryStudentName;
    private CommentFragment mCommentFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private MainDatabaseHelper mMainDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainDatabaseHelper = new MainDatabaseHelper(this, this, this.getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();

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
                    mMainDatabaseHelper.onSignedInInitialize(user.getDisplayName(), user.getUid());
//                    mMainDatabaseHelper.onSignedInInitialize(user.getDisplayName(), user.getUid());
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
    public void onItemClick(Student student, String groupKey, String studentKey) {
        StatisticsActivity.start(MainActivity.this, student, groupKey, studentKey);
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

    @Override
    public void saveSmiley(Student student, int smileyValue) {
        mMainDatabaseHelper.saveSmiley(student, smileyValue);
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
            mMainDatabaseHelper.insertGroup(group, new Student(mTemporaryStudentName));
            mTemporaryStudentName = null;

            transaction.remove(addGroupFragment);
            transaction.commit();

        } else

        {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onConfirmUpdateGroup(Group group) {

        android.support.v4.app.Fragment addGroupFragment = mFragmentManager.findFragmentByTag(GROUP_FRAGMENT_TAG);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (addGroupFragment != null) {
            // FIXME: 2017-07-30 my dialogs doesnt close properly and derefore cant be opened whitout pressing the backbutton first.
            transaction.remove(addGroupFragment);
            transaction.commit();
            mAdapter.updateGroup(group.getGroupKey(), group.getGroupName());
            // saving to Firebase Realtime Database.
            mMainDatabaseHelper.updateGroup(group.getGroupKey(), group.getGroupName(), group.getMembers());
        } else {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onInsertStudent(String name, int groupPos) {

        if (groupPos < mAdapter.getGroups().size()) {
            Student student = new Student(name);
            student.setStudentKey(
                    mMainDatabaseHelper.insertStudent(student, mAdapter.getGroupKey(groupPos))
            );
            mAdapter.addStudent(student, groupPos);

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

        mMainDatabaseHelper.insertComment(text, studentPosition);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Toast.makeText(this, "'" + text + "'" + getString(R.string.toast_text), Toast.LENGTH_SHORT).show();

        if (mCommentFragment != null) {
            transaction.remove(mCommentFragment);
            transaction.commit();
        } else {
            Toast.makeText(this, "The Fragment was not added before", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean addGroup(Group group) {
        final boolean groupWasAdded = mAdapter.addGroup(group);

        return groupWasAdded;
    }

    @Override
    public void addStudent(Student student, String groupKey) {
        mAdapter.addStudent(student, groupKey);
    }

    @Override
    public Student getStudent(int studentPosition) {
        return mAdapter.getStudent(studentPosition);
    }

    @Override
    public void updateStudent(String studentKey, String dataKey) {
        mAdapter.updateStudent(studentKey, dataKey);
    }
}
