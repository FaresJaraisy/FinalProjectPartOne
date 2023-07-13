package com.example.finalprojectpartone;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import data.UserEventCount;
import database.DBManager;

public class TotalEventsSummary extends AppCompatActivity {

    private TextView topUsersTitleTextView, bestUserTitleTextView, totalEventsTitleTextView;
    private TextView bestUserTextView, lowEventsTextView, mediumEventsTextView, highEventsTextView;

    DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_events_summary);

        topUsersTitleTextView = findViewById(R.id.topUsersTitleTextView);
        bestUserTitleTextView = findViewById(R.id.bestUserTitleTextView);
        totalEventsTitleTextView = findViewById(R.id.totalEventsTitleTextView);
        bestUserTextView = findViewById(R.id.bestUserTextView);
        lowEventsTextView = findViewById(R.id.lowEventsTextView);
        mediumEventsTextView = findViewById(R.id.mediumEventsTextView);
        highEventsTextView = findViewById(R.id.highEventsTextView);

        dbManager = new DBManager(this);
        dbManager.open();


        List<UserEventCount> userEventCountList = dbManager.getTop10ReportingUsers();

        // Set top users title
        topUsersTitleTextView.setText("Top " + Math.min(userEventCountList.size(), 10) + " reporting users:");

        // Set top users list
        List<String> topUsers = new ArrayList<>();
        for (int i = 0; i < userEventCountList.size(); i++) {
            UserEventCount userEventCount = userEventCountList.get(i);
            topUsers.add(userEventCount.getUsername() + ": " + userEventCount.getEventCount());
        }
        ArrayAdapter<String> topUsersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, topUsers);
        ListView topUsersListView = findViewById(R.id.topUsersListView);
        topUsersListView.setAdapter(topUsersAdapter);

        // Set best user title
        bestUserTitleTextView.setText("Best user point:");

        // Set best user
        if (!userEventCountList.isEmpty()) {
            UserEventCount bestUser = dbManager.getBestUserPoints();
            bestUserTextView.setText(bestUser.getUsername() + ": " + bestUser.getEventCount());
        }

        // Set total events title
        totalEventsTitleTextView.setText("Total events");

        // Set total events
        int var1 = dbManager.getNumberOfLowSeverityEvents();
        int var2 = dbManager.getNumberOfMediumSeverityEvents();
        int var3 = dbManager.getNumberOfHighSeverityEvents();

        lowEventsTextView.setText("Low events: " + var1);
        mediumEventsTextView.setText("Medium events: " + var2);
        highEventsTextView.setText("High events: " + var3);
    }
}
