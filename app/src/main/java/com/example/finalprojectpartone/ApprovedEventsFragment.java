package com.example.finalprojectpartone;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ApprovedEventsFragment extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserEventsSummary activity = (UserEventsSummary) getActivity();
        view = inflater.inflate(R.layout.fragment_approved_events, container, false);
        TextView textViewApprovedEvents = view.findViewById(R.id.textViewApprovedEvents);
        textViewApprovedEvents.setText(String.valueOf(activity.getTotalApproved()));
        return view;
    }
}
