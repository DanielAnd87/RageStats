package com.example.danielandersson.ragestats.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.fragment.GroupDialogFragment.OnComfirmed;

import java.util.ArrayList;

/**
 * specified {@link OnComfirmed}.
 */
public class MyAddMemberItemRecyclerViewAdapter extends RecyclerView.Adapter<MyAddMemberItemRecyclerViewAdapter.ViewHolder> {

    private final OnComfirmed mListener;
    private final ArrayList<String> mMemberNames;

    public ArrayList<String> getMemberNames() {
        return mMemberNames;
    }

    public MyAddMemberItemRecyclerViewAdapter(ArrayList<String> memberNames, OnComfirmed listener) {
        mMemberNames = memberNames;
        mListener = listener;
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

        holder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mMemberName;
        private final ImageButton mRemoveBtn;

        public ViewHolder(View view) {
            super(view);
            mMemberName = (TextView) view.findViewById(R.id.member_name_add_fragment);
            mRemoveBtn = (ImageButton) view.findViewById(R.id.remove_member_button);
        }

    }
}
