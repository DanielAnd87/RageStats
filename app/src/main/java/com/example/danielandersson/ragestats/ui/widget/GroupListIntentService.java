package com.example.danielandersson.ragestats.ui.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences mSharedPreferences;
    private int mSmileyToUpdate;


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
                mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
                } else {
                    mSmileyToUpdate = mSharedPreferences.getInt(Constants.TEMP_SMILEY_INDEX, -1);
                    if (mSmileyToUpdate != -1) {
                        mGroup.getStudents().get(mSmileyToUpdate).addToSmileyIndex();
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putInt(Constants.TEMP_SMILEY_INDEX, -1);
                        editor.apply();
                    }
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


                Intent fillInIntent = new Intent();
//                fillInIntent.putExtra(GroupListIntentService.EXTRA_LABEL, mCursor.getString(1));
                views.setOnClickFillInIntent(R.id.list_item_container, fillInIntent);


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
                return true;
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

    private boolean containsStudent(ArrayList<Student> students, String studentKey) {
        for (Student student : students) {
            if (student.getStudentKey().equals(studentKey)) {
                return true;
            }
        }
        return false;
    }
}
