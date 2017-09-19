package com.example.danielandersson.ragestats.ui.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.VerticalSeekBar;
import com.example.danielandersson.ragestats.ui.fragment.BlockGraphItemFragment.OnListFragmentInteractionListener;

import static android.content.ContentValues.TAG;

/**
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyBlockGraphItemRecyclerViewAdapter extends RecyclerView.Adapter<MyBlockGraphItemRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final FragmentActivity mActivity;
    private int[] mData = new int[48];
    private SparseIntArray mDataMap;


    public MyBlockGraphItemRecyclerViewAdapter(OnListFragmentInteractionListener listener, FragmentActivity activity) {
        for (int i = 0; i < mData.length; i++) {
            mData[i] = 15;
        }
        mDataMap = new SparseIntArray();
        mListener = listener;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_block_graph_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // get the hour by dividing the position by two
        boolean isEven = (position % 2) == 0;
        String timeString;
        if (isEven) {
            timeString = position / 2 + ":00";
        } else {
            timeString = (position / 2) + ":30";
        }
        holder.mTimeString.setText(timeString);


        holder.mGraphBar.setProgressDrawable(mActivity.getResources().getDrawable(R.drawable.seek_bar));

        holder.mGraphBar.setProgress(mData[position]);
        holder.mGraphBar.setMax(100);
        holder.mGraphBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged: progress: " + progress);
                if (position != RecyclerView.NO_POSITION) {
                    if (seekBar.isShown())
                        mData[position] = progress;
                        mDataMap.put(position, progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public SparseIntArray getDataMap() {
        return mDataMap;
    }

    public void updateList(SparseIntArray dataMap) {
        if (dataMap != null) {
            Log.i(TAG, "updateList: Block list is updating.");

            for (int i = 0; i < dataMap.size(); i++) {
                mData[dataMap.keyAt(i)] = dataMap.valueAt(i);
            }
            mDataMap = dataMap;
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "updateList: WARNING, THE DATA IS NULL");
        }
    }

    public int[] getArray() {
        return mData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTimeString;
        private final VerticalSeekBar mGraphBar;
        private final LinearLayout mBackground;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBackground = (LinearLayout) view.findViewById(R.id.block_background);
            mTimeString = (TextView) view.findViewById(R.id.time_block);
            mGraphBar = (VerticalSeekBar) view.findViewById(R.id.seekBar2);
        }

    }
}
