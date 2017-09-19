package com.example.danielandersson.ragestats.ui.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.StatData;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.StatDatabaseHelper;
import com.example.danielandersson.ragestats.Utils;
import com.example.danielandersson.ragestats.ui.adapters.CommentsAdapter;
import com.example.danielandersson.ragestats.ui.fragment.BlockGraphItemFragment;
import com.example.danielandersson.ragestats.ui.fragment.LongStatisticsFragment;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity
        implements
        BlockGraphItemFragment.OnListFragmentInteractionListener,
        LongStatisticsFragment.OnFragmentInteractionListener,
        StatDatabaseHelper.OnDatabaseResultListener {

    private static final String BLOCK_TAG = BlockGraphItemFragment.class.getSimpleName();
    private static final String LONG_TAG = LongStatisticsFragment.class.getSimpleName();
    private static final String TAG = StatisticsActivity.class.getSimpleName();
    private boolean hasStartFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private EditText mCommentEditText;
    private TextView mCommentsTimeTextView;
    private List<StatData> mStatDatas;
    private Student mStudent;


    private BlockGraphItemFragment mBlockFragment;
    private LongStatisticsFragment mLongFragment;
    private SharedPreferences mSharedPreferences;
    private StatData mStatData;
    private ArrayAdapter<String> mSpinnerAdapter;
    private CommentsAdapter mAdapter;
    private StatDatabaseHelper mStatDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final Intent intent = getIntent();
        String studentKey = intent.getStringExtra(Constants.STATS_STUDENT_KEY_TAG);
        mStudent = intent.getParcelableExtra(Constants.STATS_STUDENT_TAG);
        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mCommentEditText = (EditText) findViewById(R.id.comment_edittext);
        mCommentsTimeTextView = (TextView) findViewById(R.id.comment_time_label);
        toolbar.setTitle(mStudent.getName());
        mStatDatabaseHelper = new StatDatabaseHelper(
                FirebaseDatabase.getInstance(),
                this,
                studentKey);


        mStatDatabaseHelper.fetchData(mStudent.getDataKeyMap(), Calendar.getInstance(), true);
        mStatDatabaseHelper.fetchData(mStudent.getDataKeyMap(), Calendar.getInstance(), false);


        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();


        mBlockFragment = new BlockGraphItemFragment();
        mTransaction.add(R.id.block_fragment, mBlockFragment, BLOCK_TAG);
        mTransaction.commit();
        hasStartFragment = true;


// TODO: 2017-08-01 filter comments when chosing a tag
        final Spinner spinner = (Spinner) findViewById(R.id.tag_spinner);
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        spinner.setAdapter(mSpinnerAdapter);
        mSpinnerAdapter.add(getResources().getString(R.string.default_comment_tag));
        spinner.setVisibility(View.INVISIBLE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setTag(mSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Collapsed
                    spinner.setVisibility(View.VISIBLE);
                } else if (verticalOffset == 0) {
                    // Expanded
                    spinner.setVisibility(View.INVISIBLE);
                } else {
                    spinner.setVisibility(View.INVISIBLE);
                    // Somewhere in between
                }
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (hasStartFragment) {


                    mTransaction = mFragmentManager.beginTransaction();


                    if (mLongFragment == null) {
                        mLongFragment = new LongStatisticsFragment();
                        mTransaction
                                .add(R.id.block_fragment, mLongFragment, LONG_TAG)
//                                .addToBackStack(LONG_TAG)
                        ;
                    } else {
                        mTransaction.show(mLongFragment);
                        mLongFragment.setData(mStatDatas);
                        setDateTextView(false);
                    }
                    mTransaction
                            .hide(mBlockFragment)
                            .commit();
                    fab.setImageDrawable(getDrawable(R.mipmap.calendar_icon_30));


                    hasStartFragment = false;

                } else {
                    mTransaction = mFragmentManager.beginTransaction();
                    mTransaction
//                            .add(R.id.block_fragment, mBlockFragment, BLOCK_TAG)
//                            .addToBackStack(BLOCK_TAG)
                            .show(mBlockFragment)
                            .hide(mLongFragment)
                            .commit();

                    fab.setImageDrawable(getDrawable(R.mipmap.calender_icon_1));
                    setDateTextView(true);

                    hasStartFragment = true;
                }


            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.comments_list);
        mAdapter = new CommentsAdapter(getResources().getString(R.string.default_comment_tag));
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);


        // FIXME: 2017-09-08 send correct hashmap
        mStatDatabaseHelper.fetchComment(mStudent.getCommentsKeyMap());

        mCommentEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {

                    final String text = mCommentEditText.getText().toString();
                    String userName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
                    final Comment comment = mStatDatabaseHelper.insertComment(text, userName);
                    mStudent.addCommentKey(comment.getCommentKey());
                    mAdapter.addComment(comment);
                    return true;
                } else {
                    return false;
                }
            }
        });


    }

    public void setDateTextView(boolean dayInterfal) {

        if (dayInterfal) {
            String dayOfWeek = Utils.formatToDayOfWeek(mStatData.getTimeStamp());
            mBlockFragment.setDateLabel(dayOfWeek);
        } else {
            String monthString;
            if (mStatDatas.size() > 0) {
                monthString = Utils.formatMonth(mStatDatas.get(0).getTimeStamp());
            } else {
                // FIXME: 2017-09-15 should save witch month is the current as a member.
                monthString = Utils.formatMonth(mStatData.getTimeStamp());
            }
            mLongFragment.setDateLabel(monthString);
        }
    }


    public static void start(Context context, Student student, String groupKey, String studentKey) {
        Intent starter = new Intent(context, StatisticsActivity.class);
        starter.putExtra(Constants.STATS_STUDENT_TAG, student);
        starter.putExtra(Constants.STATS_GROUP_KEY_TAG, groupKey);
        starter.putExtra(Constants.STATS_STUDENT_KEY_TAG, studentKey);

        context.startActivity(starter);
    }

    @Override
    public void onAdapterReady() {
        for (StatData statData : mStatDatas) {
            Log.i(TAG, "onAdapterReady: " + statData.getDataKey());
        }
        mLongFragment.setData(mStatDatas);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hasStartFragment = !hasStartFragment;

    }


    @Override
    public void onStopSaveFragment(SparseIntArray dataMap, String dataKey) {
        if (Utils.isTimestampToday(mStudent.getLastDataSave())) {

            mStatDatabaseHelper.updateData(dataMap, dataKey);
        } else {
            mBlockFragment.setDataKey(
                    mStatDatabaseHelper.insertData(dataMap));
        }
    }


    @Override
    public void onDataFetched(List<StatData> statDatas) {
        mStatDatas = statDatas;
        // FIXME: 2017-08-01 return the same saved blocks as the day before
        if (hasStartFragment) {
            mBlockFragment = (BlockGraphItemFragment) mFragmentManager.findFragmentByTag(BLOCK_TAG);
            boolean hasBeenSavedToday;
            if (mStatDatas == null) {
                mStatData = new StatData("", 0);
                mStatDatas = new ArrayList<>();
                hasBeenSavedToday = false;
            } else {
                // getting the first since it should be the latest
                mStatData = mStatDatas.get(0);
                hasBeenSavedToday = Utils.isTimestampToday(mStatData.getTimeStamp());
                mStatData.setDataMap(Utils.parseStringToSparseArray(mStatData.getDataString()));
            }
            long timestamp;
            if (!hasBeenSavedToday) {
                timestamp = Utils.getCurrentTimestamp();
            } else {
                timestamp = mStatData.getTimeStamp();
            }
            mStatData.setTimeStamp(timestamp);
            mStatDatas.add(mStatData);
            mCommentsTimeTextView.setText(Utils.formatDigitalTime(timestamp));
            mBlockFragment.updateBlocks(mStatData);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mStatDatas.size(); i++) {
                        final StatData data = mStatDatas.get(i);
                        data.setDataMap(Utils.parseStringToSparseArray(data.getDataString()));
                    }
                }
            });
        }

    }

    @Override
    public void onShortDataFetched(StatData statData) {
        if (statData != null) {
            mStatData = statData;
            mBlockFragment.updateBlocks(statData);
        }
    }

    @Override
    public void onLongDataFetched(StatData statData) {
        if (mStatDatas == null) {
            mStatDatas = new ArrayList<>();
        }
        if (!mStatDatas.contains(statData)) {
            mStatDatas.add(statData);
        }
    }

    @Override
    public void onCommentFetched(Comment comment) {
        comment.setTimeAndDate();

        // add tag here if a new one

        List<String> commentTags = Utils.hashtagFinder(comment.getComment());
        comment.setTag(commentTags);
        if (commentTags != null) {
            final int count = mSpinnerAdapter.getCount();
            // searching
            for (int i = 0; i < count; i++) {
                final String spinnerItem = mSpinnerAdapter.getItem(i);
                for (String commentTag : commentTags) {
                    if (commentTag.equals(spinnerItem)) {
                        break;
                    }
                    mSpinnerAdapter.add(commentTag);
                    mSpinnerAdapter.notifyDataSetChanged();
                }
            }
        }
        mAdapter.addComment(comment);

    }
}
