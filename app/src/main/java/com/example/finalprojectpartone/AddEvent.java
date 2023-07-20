package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.Event;
import database.DBManager;


public class AddEvent extends AppCompatActivity {
    private String username,dangerLevel,eventType,district;
    private int userId;
    private final int CAMERA_CODE = 123;

    private Button buttonSave;
    private Button buttonCamera;
    private ImageView imageViewEvent;
    private Spinner dangerLevelSpinner, eventTypeSpinner, districtSpinner;
    private EditText descriptionEditText, locationOrAddressText;
    private Event event;
    private Bitmap imageBitmap = null;
    boolean isImageTaken = false;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        dbManager = new DBManager(this, findViewById(android.R.id.content));
        dbManager.open();

        init_fields();
        setSpinnersListeners();
        setListenerForGetPicture();

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", -1);
        username= intent.getStringExtra("username");
        Log.d(TAG, "open add event with" + username);
        setTitle("Add event");

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss XXX yyyy");
                String strDate = sdf.format(new Date());
                Date date;
                try {
                    date = sdf.parse(strDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                event = new Event(
                        0,
                        eventType,
                        imageBitmap,
                        descriptionEditText.getText().toString(),
                        locationOrAddressText.getText().toString(),
                        district,
                        dangerLevel,
                        date, // Use the current date
                        0,
                        0,
                        new UserProfile(userId, username),
                        ""
                );

                try {
                    dbManager.insertEvent(event);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(AddEvent.this, "Event Saved",Toast.LENGTH_LONG).show();
                clear_fields();

            }
        });

    }

    // When the image is clicked start camera capture
    private void setListenerForGetPicture() {
        imageViewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CODE);
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CODE);
            }
        });
    }

    // When cameraIntent returns this method get the bitmap and set the image of the imageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //called when image was captured from camera
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) intent.getExtras().get("data");
            if (imageBitmap != null) {
                imageViewEvent.setImageBitmap(imageBitmap);
                isImageTaken = true;
            }
        }
    }
    private void init_fields() {
        buttonSave = findViewById(R.id.ButtonSave);
        imageViewEvent = findViewById(R.id.imageViewEvent);
        dangerLevelSpinner = findViewById(R.id.DangerLevelSpinner);
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        districtSpinner = findViewById(R.id.districtSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationOrAddressText = findViewById(R.id.locationOrAddressText);
        buttonCamera = findViewById(R.id.ButtonCaptureImage);
        isImageTaken = false;
        imageViewEvent.setImageResource(R.drawable.baseline_camera_alt_24);
    }

    private void clear_fields() {
        descriptionEditText.setText("", TextView.BufferType.EDITABLE);
        locationOrAddressText.setText("", TextView.BufferType.EDITABLE);
        isImageTaken = false;
        imageBitmap = null;
        imageViewEvent.setImageResource(R.drawable.baseline_camera_alt_24);
    }

    // set listeners to dangerLevel, eventType and district spinners and extract value when selected
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

}