package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import database.DBManager;


public class MyEventsList extends AppCompatActivity
        implements FilterDialogFragmentMyEvents.OnInputListener {
    private final int EDIT_CODE = 321;
    DBManager dbManager;
    int userId;
    String username;
    SimpleCursorAdapter eventsAdapter;
    ListView listViewEvents;
    TextView textViewFilterSeverity, textViewFilterEventType, textViewFilterDistrict, textViewFilterOrderByActive;
    AlertDialog.Builder builder;

    // receive current Filter setting from filter dialog fragment
    @Override public void sendInput(FilterSettings filterSettings)
    {
        Toast.makeText(MyEventsList.this, "Filter Applied" ,Toast.LENGTH_LONG).show();
        textViewFilterSeverity.setText(filterSettings.getRiskLevel());
        textViewFilterEventType.setText(filterSettings.getType());
        textViewFilterDistrict.setText(filterSettings.getDistrict());
        if (filterSettings.isOrderByDate()) {
            textViewFilterOrderByActive.setText("Active");
        } else {
            textViewFilterOrderByActive.setText("Not active");
        }
        filterSettings.setUserId(String.valueOf(userId));
        eventsAdapter.changeCursor(dbManager.getMyEventsCursorFiltered(filterSettings));
    }

    // reload to refresh view
    public void editOnClickHandler(View v) {
        String eventId = (String)v.getTag();

        Intent intent = new Intent(getApplicationContext(), EditEvent.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        intent.putExtra("eventId", eventId);
        startActivityForResult(intent,EDIT_CODE);
    }

    //register reject on event item and reload to refresh view
    public void deleteOnClickHandler(View v) {
        String eventId = (String)v.getTag();

        builder.setMessage("Do you want to delete the event ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbManager.deleteEvent(eventId, userId);
                        eventsAdapter.changeCursor(dbManager.getMyEventsCursor(username));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Delete event alert");
        alert.show();
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
        setContentView(R.layout.activity_my_events_list);

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", -1);
        username= intent.getStringExtra("username");
        setTitle("My reported events("+ username + ")");
        builder = new AlertDialog.Builder(this);
        listViewEvents = findViewById(R.id.listViewEvents);

        textViewFilterSeverity = findViewById(R.id.TextViewFilterSeverity);
        textViewFilterEventType = findViewById(R.id.TextViewFilterEventType);
        textViewFilterDistrict = findViewById(R.id.TextViewFilterDistrict);
        textViewFilterOrderByActive = findViewById(R.id.TextViewFilterOrderByActive);

        dbManager = new DBManager(this);
        dbManager.open();

        FilterSettings filterSettings = new FilterSettings("All", "All", "All", false, false);
        Log.d(TAG, "populateMyEventsListView usernmae : " + username);
        eventsAdapter = dbManager.populateMyEventsListView(username);


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
                } else if (view.getId() == R.id.buttonEditEvent) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));
                    return true;
                } else if (view.getId() == R.id.buttonDeleteEvent) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));
                    return true;
                }  else if (view.getId() == R.id.showCommentsId) {
                    Button btn = (Button) view;
                    btn.setTag(cursor.getString(columnIndex));
                    return true;
                }else {  // Process the rest of the adapter with default settings.
                    return false;
                }
            }
        });
        listViewEvents.setAdapter(eventsAdapter);
    }

    // returned from edit event - refresh the listview to show changes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //called when image was captured from camera
        if (requestCode == EDIT_CODE) {
            eventsAdapter.changeCursor(dbManager.getMyEventsCursor(username));
            eventsAdapter.notifyDataSetChanged();
            listViewEvents.invalidate();
            listViewEvents.invalidateViews();
        }
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
            FilterDialogFragmentMyEvents dialog = new FilterDialogFragmentMyEvents();
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
            Intent totalEventsSummary = new Intent(this, TotalEventsSummary.class);
            startActivity(totalEventsSummary);
            return true;
        }
        else {
            return true;
        }
    }
}