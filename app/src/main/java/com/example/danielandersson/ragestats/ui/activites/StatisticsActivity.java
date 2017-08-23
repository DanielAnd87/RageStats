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
import com.example.danielandersson.ragestats.Utils;
import com.example.danielandersson.ragestats.ui.adapters.CommentsAdapter;
import com.example.danielandersson.ragestats.ui.fragment.BlockGraphItemFragment;
import com.example.danielandersson.ragestats.ui.fragment.LongStatisticsFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements BlockGraphItemFragment.OnListFragmentInteractionListener, LongStatisticsFragment.OnFragmentInteractionListener {

    private static final String BLOCK_TAG = BlockGraphItemFragment.class.getSimpleName();
    private static final String LONG_TAG = LongStatisticsFragment.class.getSimpleName();
    private boolean hasStartFragnent;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private EditText mCommentEdittext;
    private TextView mTimeTextView;
    private FirebaseDatabase mDatabase;
    private int mIndex;
    private Student mStudent;
    private long mTimestamp;
    private boolean hasBeenSavedToday;
    private int mDatePos;
    private String mGroupKey;
    private String mStudentKey;
    private List<StatData> mStatDatas;
    private int mDataListIndex;
    private SharedPreferences mSharedPreferences;
    private StatData mStatData;
    private BlockGraphItemFragment mBlockFragment;
    private LongStatisticsFragment mLongFragment;
    private ArrayAdapter<String> mSpinnerAdapter;
    private CommentsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final Intent intent = getIntent();
        final boolean newComment = intent.getBooleanExtra(Constants.STATS_STARTER_COMMENT_TAG, false);
        mGroupKey = intent.getStringExtra(Constants.STATS_GROUP_KEY_TAG);
        mStudentKey = intent.getStringExtra(Constants.STATS_STUDENT_KEY_TAG);
        mStudent = intent.getParcelableExtra(Constants.STATS_STUDENT_TAG);
        mIndex = intent.getIntExtra(Constants.STATS_STARTER_INDEX_TAG, 0);
        mDatePos = 0;

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);

        mDataListIndex = mSharedPreferences.getInt(Constants.LAST_DATA_INDEX + mStudent.getStatDataKey(), 0);


        mCommentEdittext = (EditText) findViewById(R.id.comment_edittext);
        mTimeTextView = (TextView) findViewById(R.id.comment_time_label);

        if (newComment) {
            mCommentEdittext.requestFocus();
        }

        // FIXME: 2017-07-18 correct when I have data

        mDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference dataReference = mDatabase.getReference();
        dataReference.child("statData").child(mStudent.getStatDataKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // adding data member list
                mStatDatas = dataSnapshot.getValue(new GenericTypeIndicator<List<StatData>>() {
                });

                toolbar.setTitle(mStudent.getName());

                // FIXME: 2017-08-01 return the same saved blocks as the day before
                if (hasStartFragnent) {
                    mBlockFragment = (BlockGraphItemFragment) mFragmentManager.findFragmentByTag(BLOCK_TAG);
                    if (mStatDatas == null) {
                        mStatData = new StatData("", 0);
                        mStatDatas = new ArrayList<>();
                        hasBeenSavedToday = false;
                    } else {
                        mStatData = mStatDatas.get(mDataListIndex >= mStatDatas.size() ? mStatDatas.size() - 1 : mDataListIndex);
                        hasBeenSavedToday = Utils.isTimestampToday(mStatData.getTimeStamp());
                        mStatData.setDataMap(Utils.parseStringToSparseArray(mStatData.getDataString()));
                    }
                    if (!hasBeenSavedToday) {
                        mTimestamp = Utils.getCurrentTimestamp();
                        mDataListIndex = mStatDatas.size();
                    } else {
                        mTimestamp = mStatData.getTimeStamp();
                    }
                    mStatData.setTimeStamp(mTimestamp);
                    mStatDatas.add(mStatData);
                    mTimeTextView.setText(Utils.formatDigitalTime(mTimestamp));

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt(Constants.LAST_DATA_INDEX + mStudent.getStatDataKey(), mDataListIndex);
                    editor.apply();

                    mBlockFragment.updateBlocks(mStatData.getDataMap());


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
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();


        mBlockFragment = new BlockGraphItemFragment();
        mTransaction.add(R.id.block_fragment, mBlockFragment, BLOCK_TAG);
        mTransaction.commit();
        hasStartFragnent = true;


        // TODO: 2017-08-01 filter comments when chosing a tag
        final Spinner spinner = (Spinner) findViewById(R.id.tag_spinner);
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        spinner.setAdapter(mSpinnerAdapter);
        mSpinnerAdapter.add(getResources().getString(R.string.default_comment_tag));
        spinner.setVisibility(View.INVISIBLE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.filterByTag(mSpinnerAdapter.getItem(position));
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


                if (hasStartFragnent) {


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
                    }
                    mTransaction
                            .hide(mBlockFragment)
                            .commit();
                    fab.setImageDrawable(getDrawable(R.mipmap.calendar_icon_30));


                    hasStartFragnent = false;

                } else {
                    mTransaction = mFragmentManager.beginTransaction();
                    mTransaction
//                            .add(R.id.block_fragment, mBlockFragment, BLOCK_TAG)
//                            .addToBackStack(BLOCK_TAG)
                            .show(mBlockFragment)
                            .hide(mLongFragment)
                            .commit();

                    fab.setImageDrawable(getDrawable(R.mipmap.calender_icon_1));


                    hasStartFragnent = true;
                }


            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.comments_list);
        mAdapter = new CommentsAdapter(new ArrayList<Comment>());
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);


        final HashMap<String, Boolean> hashMap = mStudent.getCommentsKeyMap();
        if (hashMap != null) {
            for (String key : hashMap.keySet()) {
                final DatabaseReference reference = mDatabase.getReference().child("comments").child(key);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Comment comment = dataSnapshot.getValue(Comment.class);
                        comment.setTimeAndDate();

                        // add tag here if a new one

                        List<String> tag;
                        tag = comment.getTag();
                        if (tag == null) {
                            tag = new ArrayList<String>();
                        }
                        final int count = mSpinnerAdapter.getCount();
                        // searching
                        for (int i = 0; i < count; i++) {
                            final String item = mSpinnerAdapter.getItem(i);
                            if (tag.contains(item)) {
                                mSpinnerAdapter.add(item);
                                mSpinnerAdapter.notifyDataSetChanged();
                            }
                        }
                        mAdapter.addComment(comment);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        }

        mCommentEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {

                    final String text = mCommentEdittext.getText().toString();
                    final DatabaseReference commentReference = mDatabase.getReference();
                    final String key = commentReference.push().getKey();
                    mStudent.addCommentKey(key);
                    mDatabase.getReference().child("student").child(mStudentKey).child(mIndex + "").child("commentsKeyMap").setValue(mStudent.getCommentsKeyMap());
                    String userName = mSharedPreferences.getString(Constants.KEY_MEMBER_NAME, "");
                    final Comment comment = new Comment(text, Utils.getCurrentTimestamp(), userName);
                    comment.setTag(Utils.hashtagFinder(text));
                    comment.setTimeAndDate();
                    commentReference.child("comments").child(key).setValue(comment);

                    mAdapter.addComment(comment);
                    return true;
                } else {
                    return false;
                }
            }
        });


    }

    public static void start(Context context, int studentIndex, Student student, String groupKey, String studentKey) {
        Intent starter = new Intent(context, StatisticsActivity.class);
        starter.putExtra(Constants.STATS_STUDENT_TAG, student);
        starter.putExtra(Constants.STATS_STUDENT_INDEX_TAG, studentIndex);
        starter.putExtra(Constants.STATS_GROUP_KEY_TAG, groupKey);
        starter.putExtra(Constants.STATS_STUDENT_KEY_TAG, studentKey);

        context.startActivity(starter);
    }

    @Override
    public void onAdapterReady() {
        mLongFragment.setData(mStatDatas);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hasStartFragnent = !hasStartFragnent;

    }


    @Override
    public void onStopSaveFragment(SparseIntArray dataMap) {
        final DatabaseReference dataReference = mDatabase.getReference();
        final String indexString;
        indexString = mDataListIndex + "";
        final StatData statData = new StatData(Utils.parseSparseArrayToString(dataMap), Utils.getCurrentTimestamp());
        dataReference.child("statData").child(mStudent.getStatDataKey()).child(indexString).setValue(statData);
    }

}
