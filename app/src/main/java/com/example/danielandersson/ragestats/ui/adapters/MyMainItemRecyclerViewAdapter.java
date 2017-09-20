package com.example.danielandersson.ragestats.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Student;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.fragment.MainItemFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyMainItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int GROUP = 0;
    public static final int STUDENT = 1;
    private final OnListFragmentInteractionListener mListener;
    private final ArrayList<Group> mGroups;
    private final Context mContext;
    private int mSize;
    private int[][] mIndexes;


    public MyMainItemRecyclerViewAdapter(ArrayList<Group> items, OnListFragmentInteractionListener listener, Context context) {
        mListener = listener;
        mContext = context;
        mGroups = items;
        if (mGroups.size() > 0) {
            indexGroups();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewStudent = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_main_student_item, parent, false);
        View viewGroup = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_main_memeber_item, parent, false);


        if (viewType == STUDENT) {
            return new ViewHolderStudents(viewStudent);
        } else {
            return new ViewHolderGroups(viewGroup);
        }
    }


    @Override
    public int getItemViewType(int position) {
        final boolean isGroupViews = mIndexes[position][STUDENT] == -1;
        if (isGroupViews) {
            return GROUP;
        } else {
            return STUDENT;
        }
    }

    private void indexGroups() {
        mSize = 0;
        // counting positions
        for (Group group : mGroups) {
            mSize++;
            for (int i = 0; i < group.getStudents().size(); i++) {
                mSize++;
            }
        }

        mIndexes = new int[mSize][2];


        int currentPos = 0;
        for (int groupIndex = 0; groupIndex < mGroups.size(); groupIndex++) {
            mIndexes[currentPos][GROUP] = groupIndex;
            mIndexes[currentPos][STUDENT] = -1;
            currentPos++;

            final int classSize = mGroups.get(groupIndex).getStudents().size();
            for (int studentIndex = 0; studentIndex < classSize; studentIndex++) {
                // index them all
                mIndexes[currentPos][GROUP] = groupIndex;
                mIndexes[currentPos][STUDENT] = studentIndex;
                currentPos++;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Group group;
        if (mGroups.size() > 0) {
            final int groupIndex = mIndexes[position][GROUP];
            group = mGroups.get(groupIndex);
        } else {
            group = null;
        }
        switch (holder.getItemViewType()) {
            case STUDENT:
                Log.i(TAG, "onBindViewHolder: position:" + position);
                final ViewHolderStudents holderStudents = (ViewHolderStudents) holder;


                final Student student;
                try {
                    final ArrayList<Student> students = group.getStudents();
                    if (students.size() != 0) {
                        final int studentIndex = mIndexes[position][1];
                        student = students.get(studentIndex);


                        holderStudents.mStudendNameTextView.setText(student.getName());


                        holderStudents.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (null != mListener) {
                                    mListener.onItemClick(student, group.getGroupKey(), student.getStudentKey());
                                }
                            }

                        });
                        holderStudents.mCommentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (null != mListener) {
                                    // TODO: 2017-07-30 start a comment dialog instead
                                    // FIXME: 2017-08-01 could create problems if individual items changes and this doesnt update.
                                    mListener.startCommentDialog(position);
                                }
                            }

                        });

                        holderStudents.mSmileyBtn.setBackground(mContext.getDrawable(Constants.SMILEY_SRC[student.getSmileyIndex()]));
                        holderStudents.mSmileyBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                student.addToSmileyIndex();
                                if (student.getSmileyIndex() == Constants.SMILEY_SRC.length) {
                                    student.setSmileyIndex(0);
                                }
                                holderStudents.mSmileyBtn.setBackground(mContext.getDrawable(Constants.SMILEY_SRC[student.getSmileyIndex()]));
                                mListener.saveSmiley(student, (student.getSmileyIndex() * 10) + 15);
                            }
                        });
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();

                    Log.w(TAG, "onBindViewHolder: No students in list at this position: " +
                            position);

                }
                break;

            case GROUP:
                ViewHolderGroups holderGroups = (ViewHolderGroups) holder;
                final StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(group.getGroupName());
                if (group.getMembers() != null) {
                    for (String member : group.getMembers()) {
                        stringBuilder.append("\n" + member);
                    }
                }

                holderGroups.mGroupLabelTextView.setText(stringBuilder.toString());
                holderGroups.mEditButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onEditBtnClick(group);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSize;
    }

    public boolean addGroup(Group group) {
        for (Group currentGroup : mGroups) {
            if (currentGroup.getGroupKey().equals(group.getGroupKey())) return false;
        }
        mGroups.add(0, group);
        indexGroups();
        notifyDataSetChanged();
        return true;
    }

    public void updateGroup(String key, String name) {
        for (int i = 0; i < mGroups.size(); i++) {
            if (mGroups.get(i).getGroupKey().equals(key)) {
                // changing values
                mGroups.get(i).setGroupName(name);
                // finding correct index
                for (int j = 0; j < mIndexes.length; j++) {
                    if (mIndexes[j][GROUP] == i) {
                        notifyItemChanged(j);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public Group addStudent(Student student, int groupPos) {
        final Group group = mGroups.get(groupPos);
        group.addStudent(student);

        indexGroups();
        notifyDataSetChanged();

        return group;
    }

    public ArrayList<Group> getGroups() {
        return mGroups;
    }

    public void addStudent(Student student, String key) {
        for (Group group : mGroups) {
            boolean containsStudent = containsStudent(group.getStudents(), student.getStudentKey());
            if (group.getGroupKey().equals(key) && !containsStudent) {
                group.addStudent(student);
            }
        }
        indexGroups();
        notifyDataSetChanged();
        // FIXME: 2017-07-27 try to do this as rarely as possible
    }

    private boolean containsStudent(ArrayList<Student> students, String studentKey) {

        for (Student student : students) {
            if (student.getStudentKey().equals(studentKey)) {
                return true;
            }
        }
        return false;
    }

    public void addStudents(List<Student> studentList, String groupKey) {

        for (Group group : mGroups) {
            if (group.getGroupKey().equals(groupKey)) {
                group.setStudents((ArrayList<Student>) studentList);
                indexGroups();
                notifyDataSetChanged();
                return;
            }
        }
    }

    public Student getStudent(int position) {
        return mGroups
                .get(mIndexes[position][GROUP]).getStudents()
                .get(mIndexes[position][STUDENT]);
    }

    public int getStudentPos(int studentPosition) {
        return mIndexes[studentPosition][STUDENT];
    }

    public String getGroupKey(int groupPos) {
        return mGroups.get(groupPos).getGroupKey();
    }

    public void updateStudent(String studentKey, String dataKey) {
        for (Group group : mGroups) {
            ArrayList<Student> students = group.getStudents();


            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                if (student.getStudentKey().equals(studentKey)) {
                    if (student.getDataKeyMap()!=null) {
                        student.getDataKeyMap().put(dataKey, System.currentTimeMillis());
                    }
                    student.setLastDataSave(System.currentTimeMillis()/1000);
                    return;
                }

            }
        }
    }

    public class ViewHolderGroups extends RecyclerView.ViewHolder {
        private TextView mGroupLabelTextView = null;
        private Button mEditButton = null;

        public ViewHolderGroups(View view) {
            super(view);
            mGroupLabelTextView = (TextView) view.findViewById(R.id.group_label_textview);
            mEditButton = (Button) view.findViewById(R.id.edit_button_main);
        }

    }

    public class ViewHolderStudents extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mStudendNameTextView = null;
        private Button mCommentBtn = null;
        private Button mSmileyBtn = null;

        public ViewHolderStudents(View view) {
            super(view);

            mView = view;

            mStudendNameTextView = (TextView) view.findViewById(R.id.student_name_main_list);
            mCommentBtn = (Button) view.findViewById(R.id.comment_button_main);
            mSmileyBtn = (Button) view.findViewById(R.id.smiley_button_main);
        }

    }
}
