package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import database.DBManager;


public class MainActivity extends AppCompatActivity {
    private static final int ADDEVENT_ACTIVITY_REQUEST_CODE = 1;
    private static final int EVENTLIST_ACTIVITY_REQUEST_CODE = 2;
    private static final int EVENTLISTINFO_ACTIVITY_REQUEST_CODE = 3;
    private String username;
    private int userId;
    FirebaseAuth auth;
    Button button;
    FirebaseUser user;

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        auth = FirebaseAuth.getInstance();
        button = findViewById (R.id.Logout);
        user = auth.getCurrentUser () ;

        dbManager = new DBManager(this);
        dbManager.open();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            UserProfile userdata = dbManager.getUser(user.getEmail());
            if (userdata != null) {
                //Intent intent = getIntent();
                //intent.putExtra("id", userdata.getId());
                //intent.putExtra("username", userdata.getUserName());
                Log.d(TAG, "in main activity user id: " + userdata.getId() +
                        "username: " + userdata.getUserName());
                Intent main_intent = new Intent(getApplicationContext(), MainActivity.class);
                main_intent.putExtra("id", userdata.getId());
                main_intent.putExtra("username", userdata.getUserName());
                userId = userdata.getId();
                username = userdata.getUserName();
             } else {
                Log.d(TAG, "in main activity user data is null");
            }
            //Intent intent = getIntent();
            //intent.putExtra("username", user.getEmail());
            //userId = intent.getIntExtra("id", -1);
            //username= intent.getStringExtra("username");
            setTitle("Logged as: " + userdata.getUserName());
        }
        button.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // set menu layout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addEvent) {
            Intent addEventIntent = new Intent(MainActivity.this, AddEvent.class);
            addEventIntent.putExtra("id", userId);
            addEventIntent.putExtra("username", username);
            startActivity(addEventIntent);
            return true;
        } else if (id == R.id.eventList) {
            Intent eventListIntent = new Intent(MainActivity.this, EventsList.class);
            eventListIntent.putExtra("id", userId);
            eventListIntent.putExtra("username", username);
            startActivity(eventListIntent);
            return true;
        } else if (id == R.id.myEventsInfo) {
            Intent eventListIntent = new Intent(MainActivity.this, MyEventsList.class);
            eventListIntent.putExtra("id", userId);
            eventListIntent.putExtra("username", username);
            startActivity(eventListIntent);
            return true;
        } else if (id == R.id.eventsInfo) {
            Intent eventListSummaryIntent = new Intent(MainActivity.this, UserEventsSummary.class);
            eventListSummaryIntent.putExtra("id", userId);
            eventListSummaryIntent.putExtra("username", username);
            startActivity(eventListSummaryIntent);
            return true;
        }
        else {
            return true;
        }
    }
}

