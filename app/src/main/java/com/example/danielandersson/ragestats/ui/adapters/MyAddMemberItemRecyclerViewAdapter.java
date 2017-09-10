package com.example.danielandersson.ragestats.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.danielandersson.ragestats.AdapterCommunicator;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment.OnComfirmed;

import java.util.ArrayList;

/**
 * specified {@link OnComfirmed}.
 */
public class MyAddMemberItemRecyclerViewAdapter extends RecyclerView.Adapter<MyAddMemberItemRecyclerViewAdapter.ViewHolder> {

    private final OnComfirmed mListener;
    private ArrayList<String> mMemberNames;
    private ArrayList<String> mMemberKeys;
    private final AdapterCommunicator mAdapterCommunicator;
    private boolean mSearchMode;

    public ArrayList<String> getMemberNames() {
        return mMemberNames;
    }

    public MyAddMemberItemRecyclerViewAdapter(ArrayList<String> memberNames, OnComfirmed listener, AdapterCommunicator adapterCommunicator) {
        mMemberNames = memberNames;
        mListener = listener;
        mAdapterCommunicator = adapterCommunicator;
        mMemberKeys = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_addmemberitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mMemberName.setText(mMemberNames.get(position));
        if (mSearchMode) {
            holder.mRemoveBtn.setVisibility(View.INVISIBLE);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapterCommunicator.addMemberToGroup(
                            mMemberNames.get(position),
                            mMemberKeys.get(position));
                    setSearchMode(false);
                    // TODO: 2017-09-09 reload the old list.
                }
            });
        } else {
            holder.mRemoveBtn.setVisibility(View.VISIBLE);
        }
        holder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener && mMemberNames.size() > position) {
                    mMemberNames.remove(position);
                    notifyItemRemoved(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mMemberNames.size();
    }

    public void addMember(String memberName, String memberKey) {
        if (!mMemberNames.contains(memberName)) {
            mMemberNames.add(memberName);
            mMemberKeys.add(memberKey);
        }
        if (!mSearchMode) {
            setSearchMode(true);
        }
        notifyItemInserted(0);
    }

    public void addMembers(ArrayList<String> members) {
        mMemberNames = members;
        notifyDataSetChanged();
        // FIXME: 2017-09-09 it adds two members to mGroup and I dont know where.
    }

    public void setSearchMode(boolean searchMode) {
        if (mSearchMode != searchMode) {
            mMemberKeys.clear();
            mMemberNames.clear();
            if (mSearchMode) {
                mAdapterCommunicator.getMembers();
            }
        }
        mSearchMode = searchMode;

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mMemberName;
        private final ImageButton mRemoveBtn;
        private final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMemberName = (TextView) view.findViewById(R.id.member_name_add_fragment);
            mRemoveBtn = (ImageButton) view.findViewById(R.id.remove_member_button);
        }

    }
}
