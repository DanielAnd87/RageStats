package com.example.danielandersson.ragestats.ui.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.MainDatabaseHelper;
import com.example.danielandersson.ragestats.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class GroupListIntentService extends RemoteViewsService {
    private int mAppId;
    private Group mGroup;
    private FirebaseDatabase mFirebaseDatabase;
    private String mMyMemeberKey;
    private Context mContext;
    private DataHelper mDataHelper;


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        mAppId = intent.getIntExtra(Constants.WIDGET_ID_INTENT, 1);

        return new RemoteViewsFactory() {

            @Override
            public void onCreate() {
                mContext = getApplicationContext();
                mGroup = new Group();
//                String groupKey = GroupListWidgetConfigureActivity.loadTitlePref(getApplicationContext(), mAppId);
                Log.i(TAG, "onCreate: Widget Service has started!");
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                SharedPreferences preferences = getSharedPreferences( getPackageName() + "_preferences", MODE_PRIVATE);
//                mFirebaseDatabase = FirebaseDatabase.getInstance();
//                String groupKey = "-KthPqxJpvq33BhGRR4D";

                mDataHelper = new DataHelper(getApplicationContext());
//
//                if (getCount() == 0) {
//                    fetchData(groupKey);
//                }
            }

            @Override
            public void onDataSetChanged() {
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // mGroup. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                // TODO: 2017-08-16 get the correct group from configure activity
                Log.i(TAG, "onDataSetChanged: ");
                if (getCount() == 0) {
                    Log.i(TAG, "onDataSetChanged: " +
                            "zero items in list!");
                    String groupKey = "-KthPqxJpvq33BhGRR4D";
//                    fetchData(groupKey);

                    mDataHelper.fetchData(groupKey);
                }

                // TODO: 2017-08-09 use firebase db implementation

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
            }

            @Override
            public int getCount() {
                return mGroup == null ? 0 : mGroup.getStudents().size();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mGroup == null || position >= mGroup.getStudents().size()) {
                    return null;
                }

                final Student student = mGroup.getStudents().get(position);
                Log.i(TAG, "getViewAt: " +
                        "Current widget name is " +
                        student.getName());
                // Construct the RemoteViews object for the list
                RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.fragment_main_student_item);


                views.setTextViewText(R.id.student_name_main_list, student.getName());
                // TODO: 2017-08-16 add clickListerners
//                views.setTextViewText(R.id.smiley_button_main, student.getMeasure());
//                views.setTextViewText(R.id.comment_button_main, student.getQuantity() + "");

                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.fragment_main_student_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }







    private class DataHelper implements MainDatabaseHelper.OnAdapterCallBack {
        private MainDatabaseHelper mMainDatabaseHelper;

        DataHelper(Context context) {
            mMainDatabaseHelper = new MainDatabaseHelper(
                    context,
                    this,
                    PreferenceManager.getDefaultSharedPreferences(context));
        }

        public void fetchData(String groupKey) {
            mMainDatabaseHelper.fetchGroup(groupKey);
        }

        @Override
        public boolean addGroup(Group group) {
            if (mGroup.getStudents().size() == 0) {
                mGroup = group;
            }
            return false;
        }

        @Override
        public void addStudent(Student student, String groupKey) {
            if (!containsStudent(mGroup.getStudents(), student.getStudentKey())) {
                mGroup.addStudent(student);
                GroupListWidget.sendUpdateBroadcast(mContext);
            }
        }

        @Override
        public Student getStudent(int studentPosition) {
            return null;
        }

        @Override
        public void updateStudent(String studentKey, String dataKey) {

        }


    }


//    public void fetchData(String groupKey) {
//
//        final Query query = mFirebaseDatabase.getReference("group").orderByKey();
//
//        query.equalTo(groupKey);
//
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                final Group group = dataSnapshot.getValue(Group.class);
//                final String key = dataSnapshot.getKey();
//                group.setGroupKey(key);
//
//                Log.i(TAG, "onChildAdded: query success for: " + group.getGroupName());
//
//                final Map<String, Boolean> studentMap = group.getStudentMap();
//                if (studentMap != null || studentMap.size() > 0) {
//                    for (String keyString : studentMap.keySet()) {
//                        mFirebaseDatabase.getReference().child("student").child(keyString).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Student student = dataSnapshot.getValue(Student.class);
//                                student.setStudentKey(dataSnapshot.getKey());
//
//                                if (!containsStudent(mGroup.getStudents(), dataSnapshot.getKey())) {
//                                    mGroup.addStudent(student);
//                                    GroupListWidget.sendUpdateBroadcast(mContext);
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }

    private boolean containsStudent(ArrayList<Student> students, String studentKey) {
        for (Student student : students) {
            if (student.getStudentKey().equals(studentKey)) {
                return true;
            }
        }
        return false;
    }

    // TODO: 2017-09-11 refactor MainDatabaseHelper to use a adapterListener
    // TODO: 2017-09-11 create a inner class that uses the helper and the interface

}
