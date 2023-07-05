package com.example.finalprojectpartone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class UserEventsConfirmationPointsFragment extends Fragment {
    View view;
    public UserEventsConfirmationPointsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UserEventsSummary activity = (UserEventsSummary) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_events_confirmation_points, container, false);
        TextView textViewConfirmationEvents = view.findViewById(R.id.textViewConfirmationEventsPoints);
        textViewConfirmationEvents.setText(String.valueOf(activity.getTotalPointsByConfirmations()));

        return view;
    }
}
