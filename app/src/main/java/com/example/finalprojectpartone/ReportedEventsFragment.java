package com.example.finalprojectpartone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import database.DBManager;

public class ReportedEventsFragment extends Fragment {
    View view;
    public ReportedEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserEventsSummary activity = (UserEventsSummary) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reported_events, container, false);
        TextView textViewReportedEvents = view.findViewById(R.id.textViewReportedEvents);
        textViewReportedEvents.setText(activity.getTotalReportedEvents());

        return view;
    }
}
