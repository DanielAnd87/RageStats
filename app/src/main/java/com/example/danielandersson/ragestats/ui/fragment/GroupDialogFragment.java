package com.example.danielandersson.ragestats.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.adapters.MyAddMemberItemRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnComfirmed}
 * interface.
 */
public class GroupDialogFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnComfirmed mListener;
    private EditText mEditText;
    private Button mAddBtn;
    private boolean mIsUpdating;
    private Group mGroup;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupDialogFragment() {
    }

    @SuppressWarnings("unused")
    public static GroupDialogFragment newInstance(int columnCount) {
        GroupDialogFragment fragment = new GroupDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String GROUP_ = "param1";
    private static final String IS_UPDATING = "param2";

    public static GroupDialogFragment newInstance(Group group, boolean isUpdating) {
        GroupDialogFragment fragment = new GroupDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(GROUP_, group);
        args.putBoolean(IS_UPDATING, isUpdating);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mIsUpdating = true;
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mGroup = getArguments().getParcelable(GROUP_);
            mIsUpdating = getArguments().getBoolean(IS_UPDATING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addmemberitem_list, container, false);


        mEditText = (EditText) view.findViewById(R.id.group_name_edittext);
        mAddBtn = (Button) view.findViewById(R.id.group_add_btn);


        // Set the adapter
        Context context = view.getContext();
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        final ArrayList<String> memberString = new ArrayList<>();

        if (mIsUpdating) {
            mEditText.setText(mGroup.getGroupName());
            mAddBtn.setText(R.string.label_update_button);
            recyclerView.setAdapter(new MyAddMemberItemRecyclerViewAdapter(
                    (ArrayList) mGroup.getMembers(),
                    mListener));
        } else {
            recyclerView.setAdapter(new MyAddMemberItemRecyclerViewAdapter(memberString, mListener));
        }


        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGroup.setGroupName(mEditText.getText().toString());
                if (mIsUpdating) {
                    mListener.onConfirmUpdateGroup(mGroup.getGroupKey(), mGroup.getGroupName(), mGroup.getMembers());
                } else {

                    mListener.onConfirmInsertGroup(mGroup);
                }
            }
        });

        final View layout = view.findViewById(R.id.group_fragment_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

// comment! This is new
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnComfirmed) {
            mListener = (OnComfirmed) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnComfirmed {
        void onConfirmInsertGroup(Group group);

        void onConfirmUpdateGroup(String key, String name, List<String> members);
    }
}
