package com.example.finalprojectpartone;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class FilterDialogFragmentMyEvents extends android.app.DialogFragment {

    private static final String TAG = "DialogFragment";
    private Spinner dangerLevelSpinner, eventTypeSpinner, districtSpinner;
    private Switch dateorderSwitch;
    private String username,dangerLevel ="All",eventType="All",district="All";

    public interface OnInputListener {
        void sendInput(FilterSettings filterSettings);
    }
    public OnInputListener mOnInputListener;

    private EditText mInput;
    private TextView mActionOk, mActionCancel;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(
                R.layout.dialog_fragment_filter_settings_my, container, false);
        mActionCancel
                = view.findViewById(R.id.action_cancel);
        dangerLevelSpinner = view.findViewById(R.id.danger_levels_filter_spinner);
        eventTypeSpinner = view.findViewById(R.id.type_filter_spinner);
        districtSpinner = view.findViewById(R.id.district_filter_spinner);
        dateorderSwitch = view.findViewById(R.id.switchOrderByDate);
        mActionOk = view.findViewById(R.id.action_ok);
        setSpinnersListeners();

        mActionCancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v)
                    {
                        Log.d(TAG, "onClick: closing dialog");
                        getDialog().dismiss();
                    }
                });

        mActionOk.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v)
                    {
                        FilterSettings filterSettings = new FilterSettings(eventType,
                                district,
                                dangerLevel,
                                dateorderSwitch.isChecked(),
                                false);
                        mOnInputListener.sendInput(filterSettings);
                        getDialog().dismiss();
                    }
                });

        return view;
    }

    @Override public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mOnInputListener = (OnInputListener)getActivity();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: "
                    + e.getMessage());
        }
    }


    private void setSpinnersListeners() {
        dangerLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dangerLevel = dangerLevelSpinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        eventTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventType = eventTypeSpinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                district = districtSpinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
