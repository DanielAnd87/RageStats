package com.example.danielandersson.ragestats.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.danielandersson.ragestats.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddStudentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddStudentFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_GROUPS = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String[] mGroups;
    private int mPosition;

    private OnFragmentInteractionListener mListener;
    private EditText mNameEdittext;
    private Button mButton;
    private Spinner mSpinner;

    public AddStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddStudentFragment.
     */
    public static AddStudentFragment newInstance(String[] groups) {
        AddStudentFragment fragment = new AddStudentFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_GROUPS, groups);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String[] array = getArguments().getStringArray(ARG_GROUPS);
            mGroups = new String[array.length + 1];

            for (int i = 0; i < mGroups.length; i++) {
                if (i < array.length) {
                    mGroups[i] = array[i];
                } else {
                    mGroups[i] = getActivity().getResources().getString(R.string.label_new_group);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_student, container, false);
        final View layout = view.findViewById(R.id.student_fragment_layout);
        mButton = (Button) view.findViewById(R.id.student_add_button);
        mNameEdittext = (EditText) view.findViewById(R.id.student_name_edittext);
        mSpinner = (Spinner) view.findViewById(R.id.student_add_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, mGroups);
        mSpinner.setAdapter(adapter);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSaveStudent(mNameEdittext.getText().toString(), mSpinner.getSelectedItemPosition());
            }
        });


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
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
        void onSaveStudent(String name, int groupPos);
    }
}
