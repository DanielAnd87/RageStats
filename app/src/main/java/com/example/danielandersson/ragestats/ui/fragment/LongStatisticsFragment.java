package com.example.danielandersson.ragestats.ui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.danielandersson.ragestats.Data.StatData;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LongStatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LongStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LongStatisticsFragment extends Fragment {
    // the fr agment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;
    private BarChart mBarChart;
    private TextView mMonthTextView;
    private TextView mWeekdayTextView;
    private List<StatData> mStatData;
    private long mMinValue;
    private long mMaxValue;

    public LongStatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LongStatisticsFragment.
     */
    public static LongStatisticsFragment newInstance(String param1, String param2) {
        LongStatisticsFragment fragment = new LongStatisticsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_long_statistics, container, false);

        mMonthTextView = (TextView) view.findViewById(R.id.month_view_long_graph);
        setDateLabel(Utils.formatMonth(Calendar.getInstance().getTimeInMillis() / 1000));

        mMinValue = 0;
        mMaxValue = 24;

        mBarChart = (BarChart) view.findViewById(R.id.bar_chart);

        List<BarEntry> entries = new ArrayList<BarEntry>();


        for (int day = 0; day < 31; day++) {
            entries.add(new BarEntry(day, 15));
        }
        // FIXME: 2017-07-14 replace whit real data when implementing reatime database.
        final BarDataSet barSet = new BarDataSet(entries, "Anger");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            barSet.setColor(getActivity().getColor(R.color.colorAccent));
            barSet.setValueTextColor(getActivity().getColor(R.color.color_white));
        }


        final BarData barData = new BarData(barSet);
        mBarChart.setData(barData);
        mBarChart.invalidate(); // refresh

        final CrystalRangeSeekbar crystalRangeSeekbar = (CrystalRangeSeekbar) view.findViewById(R.id.seekbar_time_range_long_graph);

// get min and max text view
        final TextView tvMin = (TextView) view.findViewById(R.id.hour_long_graph);
        final TextView tvMax = (TextView) view.findViewById(R.id.hour_end_long_graph);

// set listener


        crystalRangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                formatTime((long) minValue, tvMin);
                formatTime((long) maxValue, tvMax);
                mMinValue = (Long) minValue;
                mMaxValue = (Long) maxValue;
                tvMax.setText(String.valueOf(maxValue));

            }

            private void formatTime(long minValue, TextView textView) {
                if (minValue < 10) {
                    textView.setText(String.format("0%s", String.valueOf(minValue)));
                } else {
                    textView.setText(String.valueOf(minValue));
                }
            }
        });

        crystalRangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                updateChart();
            }
        });


        mListener.onAdapterReady();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<StatData> data) {
        mStatData = data;
        updateChart();
    }


    private void updateChart() {
        List<BarEntry> entries = new ArrayList<BarEntry>();


        for (int day = 0; day < mStatData.size(); day++) {
            StatData statData = mStatData.get(day);
            final SparseIntArray intArray = statData.getDataMap();
            int medianInt = 0;
            int numValues = 0;
            for (int i = 0; i < intArray.size(); i++) {
                if (i >= mMinValue && i <= mMaxValue) {
                    medianInt += intArray.valueAt(i);
                    numValues++;
                }
            }

            // FIXME: 2017-08-02 doesn't add anything at all if there is nothing saved
            if (0 < numValues) {

                int day_in_month = statData.getCalendar().get(Calendar.DAY_OF_MONTH);

                entries.add(new BarEntry(day_in_month, medianInt / numValues));
            }
        }
        final BarDataSet barSet = new BarDataSet(entries, getString(R.string.label_statistics_anger));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            barSet.setColor(getActivity().getColor(R.color.colorAccent));
            barSet.setValueTextColor(getActivity().getColor(R.color.color_white));
        }


        final BarData barData = new BarData(barSet);
        mBarChart.setData(barData);
        mBarChart.invalidate(); // refresh
    }

    private void updateChartLegasy() {
        List<BarEntry> entries = new ArrayList<BarEntry>();


        for (int day = 0; day < mStatData.size(); day++) {
            final SparseIntArray intArray = mStatData.get(day).getDataMap();
            int medianInt = 0;
            int numValues = 0;
            for (int i = 0; i < intArray.size(); i++) {
                medianInt += intArray.valueAt(i);
                numValues++;
            }

            // FIXME: 2017-08-02 doesn't add anything at all if there is nothing saved
            if (0 < numValues) {

                entries.add(new BarEntry(day, medianInt / numValues));
            }
        }
        final BarDataSet barSet = new BarDataSet(entries, getString(R.string.label_statistics_anger));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            barSet.setColor(getActivity().getColor(R.color.colorAccent));
            barSet.setValueTextColor(getActivity().getColor(R.color.color_white));
        }


        final BarData barData = new BarData(barSet);
        mBarChart.setData(barData);
        mBarChart.invalidate(); // refresh
    }

    public void setDateLabel(String dateLabel) {
        mMonthTextView.setText(dateLabel);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onAdapterReady();
    }
}
