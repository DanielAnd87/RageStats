package com.example.danielandersson.ragestats.ui.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                String groupKey = "-KqJTio7hIsv-1lboLAs";

                if (getCount() == 0) {
                    fetchData(groupKey);
                }
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
                    String groupKey = "-KqJTio7hIsv-1lboLAs";
                    fetchData(groupKey);
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


    public void fetchData(String groupKey) {

        final DatabaseReference reference1 = mFirebaseDatabase.getReference(Constants.PATH_GROUP + "/" + groupKey + "/");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group fetchedGroup = dataSnapshot.getValue(Group.class);
                Log.i(TAG, "onDataChange: "
                        + mGroup.getGroupName());
                final String key = dataSnapshot.getKey();
                mGroup = fetchedGroup;
                mGroup.setGroupKey(key);

                final DatabaseReference studentReference = mFirebaseDatabase.getReference(Constants.PATH_STUDENTS + "/" + mGroup.getStudentListKey() + "/");

                studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<Student> studentList = dataSnapshot.getValue(new GenericTypeIndicator<List<Student>>() {
                        });
                        mGroup.setStudents((ArrayList<Student>) studentList);
                        for (Student student : studentList) {
                        Log.i(TAG, "onDataChange: "+ student.getName());
                        }

                        GroupListWidget.sendUpdateBroadcast(mContext);
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


    }

}
