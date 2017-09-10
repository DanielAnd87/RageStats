package com.example.danielandersson.ragestats.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.danielandersson.ragestats.AdapterCommunicator;
import com.example.danielandersson.ragestats.Data.Group;
import com.example.danielandersson.ragestats.Data.Member;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.adapters.MyAddMemberItemRecyclerViewAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnComfirmed}
 * interface.
 */

public class GroupDialogFragment extends Fragment implements AdapterCommunicator{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnComfirmed mListener;
    private EditText mEditText;
    private Button mAddBtn;
    private boolean mIsUpdating;
    private Group mGroup;
    private SearchView mSearchView;
    private FirebaseDatabase mDatabase;
    private HashMap<String, Boolean> mMemberMap = new HashMap<>();
    private MyAddMemberItemRecyclerViewAdapter mAdapter;

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
        } else {
            mGroup = new Group();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addmemberitem_list, container, false);


        mEditText = (EditText) view.findViewById(R.id.group_name_edittext);
        mSearchView = (SearchView) view.findViewById(R.id.group_search_view);
        mAddBtn = (Button) view.findViewById(R.id.group_add_btn);

        // TODO: 2017-09-09 hide the label and the "X" button on the adapter when searches is displayed.

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
            mAdapter = new MyAddMemberItemRecyclerViewAdapter(
                    (ArrayList) mGroup.getMembers(),
                    mListener, (AdapterCommunicator)this);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter = new MyAddMemberItemRecyclerViewAdapter(memberString, mListener, (AdapterCommunicator)this);
            recyclerView.setAdapter(mAdapter);
        }

        mDatabase = FirebaseDatabase.getInstance();
        final Query memberQuery = mDatabase.getReference("members").orderByChild("memberName");


mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
    @Override
    public boolean onClose() {
        mAdapter.setSearchMode(false);
        return false;
    }
});
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO: 2017-09-09 do a firebase search righ here.
                memberQuery.startAt(query);
                memberQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final Member member = dataSnapshot.getValue(Member.class);
                        final String key = dataSnapshot.getKey();
                        addMemberToSearchList(member.getMemberName(), key);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: 2017-09-09 chould probobly stop the last query for performance
                // TODO: 2017-09-09 do a new query
                return false;
            }
        });
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGroup.setGroupName(mEditText.getText().toString());
                if (mIsUpdating) {
                    mListener.onConfirmUpdateGroup(mGroup);
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

    public void addMemberToSearchList(String memberName, String key) {
        mAdapter.addMember(memberName, key);
    }

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

    @Override
    public void addMemberToGroup(String memberName, String memberKey) {
        if (mGroup == null) {
            mGroup = new Group();
        }
        mGroup.addMember(memberKey, memberName);
    }

    @Override
    public void getMembers() {
        mAdapter.addMembers(mGroup.getMembers());
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
    // TODO: 2017-09-09 add a listener for the adapter to the fragments

    public interface OnComfirmed {
        void onConfirmInsertGroup(Group group);
        void onConfirmUpdateGroup(Group group);
    }


}
