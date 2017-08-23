package com.example.danielandersson.ragestats.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.danielandersson.ragestats.Data.StatData;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.Utils;
import com.example.danielandersson.ragestats.ui.adapters.MyBlockGraphItemRecyclerViewAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BlockGraphItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private TextView mWeekdayTextView;
    private MyBlockGraphItemRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlockGraphItemFragment() {
    }

    @SuppressWarnings("unused")
    public static BlockGraphItemFragment newInstance(int columnCount) {
        BlockGraphItemFragment fragment = new BlockGraphItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_block_graph_item_list, container, false);

        mWeekdayTextView = (TextView) view.findViewById(R.id.weekday_label_block_fragment);

        // Set the adapter
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.block_list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        final StatData statData = new StatData(Utils.getCurrentTimestamp());
        mAdapter = new MyBlockGraphItemRecyclerViewAdapter(mListener, getActivity());
        recyclerView.setAdapter(mAdapter);

        mWeekdayTextView.setText(Utils.formatToDayOfWeek(statData.getTimeStamp()));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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
    public void onStop() {
        super.onStop();

        mListener.onStopSaveFragment(mAdapter.getDataMap());
    }

    public void updateBlocks(SparseIntArray dataMap) {
        mAdapter.updateList(dataMap);
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
    public interface OnListFragmentInteractionListener {
        void onStopSaveFragment(SparseIntArray dataMap);

    }
}
