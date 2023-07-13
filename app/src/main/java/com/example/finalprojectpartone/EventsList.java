package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import database.DBManager;

public class EventsList extends AppCompatActivity
        implements FiltersDialogFragment.OnInputListener {
    DBManager dbManager;
    int userId;
    String username;
    SimpleCursorAdapter eventsAdapter;
    ListView listViewEvents;
    TextView textViewFilterSeverity,
            textViewFilterEventType,
            textViewFilterDistrict,
            textViewFilterOrderByActive,
            textViewFilterApprovedByMe;

    // receive current Filter setting from filter dialog fragment
    @Override public void sendInput(FilterSettings filterSettings)
    {
        Toast.makeText(EventsList.this, "Filter Applied" ,Toast.LENGTH_LONG).show();
        textViewFilterSeverity.setText(filterSettings.getRiskLevel());
        textViewFilterEventType.setText(filterSettings.getType());
        textViewFilterDistrict.setText(filterSettings.getDistrict());
        if (filterSettings.isOrderByDate()) {
            textViewFilterOrderByActive.setText("Active");
        } else {
            textViewFilterOrderByActive.setText("Not active");
        }
        if (filterSettings.isApprovedByMe()) {
            textViewFilterApprovedByMe.setText("Active");
        } else {
            textViewFilterApprovedByMe.setText("Not active");
        }
        filterSettings.setUserId(String.valueOf(userId));
        eventsAdapter.changeCursor(dbManager.getAllEventsCursorFiltered(filterSettings));
    }

    //register confirm on event item and reload to refresh view
    public void confirmOnClickHandler(View v) {
        String id = (String)v.getTag();
        Log.d(TAG, "confirm id: " + id + " userId: " + userId );
        dbManager.confirmEvent(id,userId);
        eventsAdapter.changeCursor(dbManager.getAllEventsCursor());
        Toast.makeText(EventsList.this, "Item " +
                        id ,Toast.LENGTH_LONG).show();
    }

    //register reject on event item and reload to refresh view
    public void rejectOnClickHandler(View v) {
        String id = (String)v.getTag();
        Log.d(TAG, "rejected event id: " + id);
        dbManager.rejectEvent(id,userId);
        eventsAdapter.changeCursor(dbManager.getAllEventsCursor());
        Toast.makeText(EventsList.this, "Item " +
                id + "rejected",Toast.LENGTH_LONG).show();
    }
    public void commentsOnClickHandler(View v) {
        String id = (String)v.getTag();
        Log.d(TAG, "set event id: " + id + " set username " + username );
        Intent intent = new Intent(getApplicationContext(), ViewCommentsActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("eventId", id);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", -1);
        username = intent.getStringExtra("username");
        setTitle("Events List, (all users)");
        listViewEvents = findViewById(R.id.listViewEvents);

        textViewFilterSeverity = findViewById(R.id.TextViewFilterSeverity);
        textViewFilterEventType = findViewById(R.id.TextViewFilterEventType);
        textViewFilterDistrict = findViewById(R.id.TextViewFilterDistrict);
        textViewFilterOrderByActive = findViewById(R.id.TextViewFilterOrderByActive);
        textViewFilterApprovedByMe = findViewById(R.id.TextViewFilterApprovedByMe);
        dbManager = new DBManager(this);
        dbManager.open();

        eventsAdapter = dbManager.populateAllUsersEventsListView();

       eventsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.imageViewEventImage) {
                    // Get the byte array from the database.
                    byte[] byteArray = cursor.getBlob(columnIndex);
                    ImageView imageView = (ImageView) view;
                    // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                    // check if there is actually an image saved
                    if (byteArray != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        // Set the bitmap.
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setVisibility(View.INVISIBLE);
                    }

                    return true;
                } else if (view.getId() == R.id.buttonConfirm) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));
                    // user is checked to set the button enable, user cannot confirm his events
                    btn.setEnabled(!cursor.getString(0).equals(username));
                    return true;
                } else if (view.getId() == R.id.buttonReject) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));
                    // user is checked to set the button enable, user cannot reject his events
                    btn.setEnabled(!cursor.getString(0).equals(username));
                    return true;
                } else if (view.getId() == R.id.showCommentsId) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));

                    return true;
                } else {  // Process the rest of the adapter with default settings.
                    return false;
                }
            }
        });
        listViewEvents.setAdapter(eventsAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.button_set_filter) {
            FiltersDialogFragment dialog = new FiltersDialogFragment();
            dialog.show(getFragmentManager(),"FilterDialogFragment");
            return true;
        } else if  (id == R.id.addEvent) {
            Intent addEventIntent = new Intent(this, AddEvent.class);
            startActivity(addEventIntent);
            return true;
        } else if (id == R.id.eventList) {
            Intent eventListIntent = new Intent(this, EventsList.class);
            startActivity(eventListIntent);
            return true;
        } else if (id == R.id.eventsInfo) {
            Intent eventListInfoIntent = new Intent(this, UserEventsSummary.class);
            startActivity(eventListInfoIntent);
            return true;
        } else if (id == R.id.totalEventsInfo) {
            Intent allEventListInfoIntent = new Intent(this, TotalEventsSummary.class);
            startActivity(allEventListInfoIntent);
            return true;
        } else {
            return true;
        }
    }
}