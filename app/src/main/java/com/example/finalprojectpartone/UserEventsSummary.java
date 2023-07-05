package com.example.finalprojectpartone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import database.DBManager;

public class UserEventsSummary extends AppCompatActivity {

    Button buttonReportedEvents, buttonApprovedEvents, buttonRejectedEvents, buttenConfirmationEventsPoints, buttenAddedEventsPoints;
    DBManager dbManager;
    int totalReportedEvents;
    int totalApproved;
    int totalRejected;

    int totalPointsByConfirmations;

    int totalPointsByEventsCreated;
    private String username;
    private int userId;
    public String getTotalReportedEvents() {
        return String.valueOf(totalReportedEvents);
    }

    public int getTotalApproved() {
        return totalApproved;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public int getTotalPointsByConfirmations() {
        return totalPointsByConfirmations;
    }

    public int getTotalPointsByEventsCreated() {
        return totalPointsByEventsCreated;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events_summary);
        init();
        setTitle("Logged as: " + username);

        buttonReportedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ReportedEventsFragment());
            }
        });

        buttonApprovedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ApprovedEventsFragment());
            }
        });

        buttonRejectedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new RejectedEventsFragment());
            }
        });

        buttenConfirmationEventsPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new UserEventsConfirmationPointsFragment());
            }
        });
        buttenAddedEventsPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new UserEventsCreatedEventsPointsFragment());
            }
        });

    }

    // init variables and get UI controls view
    private void init() {
        buttonReportedEvents = findViewById(R.id.button_reported_events);
        buttonApprovedEvents = findViewById(R.id.button_approved_events);
        buttonRejectedEvents = findViewById(R.id.button_rejected_events);
        buttenConfirmationEventsPoints = findViewById(R.id.button_points_confirmations);
        buttenAddedEventsPoints = findViewById(R.id.button_points_created_events);;

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", -1);
        username= intent.getStringExtra("username");

        dbManager = new DBManager(this);
        dbManager.open();
        totalReportedEvents = dbManager.getUsersEventsReportedCount(username);
        totalApproved = dbManager.getUserConfirmations(userId);
        totalRejected = dbManager.getUserRejections(userId);
        totalPointsByConfirmations = dbManager.getPointsOfConfirmationAndRejectionsEventsByUser(userId);
        totalPointsByEventsCreated = dbManager.getCreatedEventsPoints(username);;
    }

    // used at button click handlers to set the shown fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
}