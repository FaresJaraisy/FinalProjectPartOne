package com.example.finalprojectpartone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class UserEventsCreatedEventsPointsFragment extends Fragment {
    View view;
    public UserEventsCreatedEventsPointsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserEventsSummary activity = (UserEventsSummary) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_events_created_events_points, container, false);
        TextView textViewCreatedEvents = view.findViewById(R.id.textViewCreatedEventsPoints);
        textViewCreatedEvents.setText(String.valueOf(activity.getTotalPointsByEventsCreated()));



        return view;
    }
}
