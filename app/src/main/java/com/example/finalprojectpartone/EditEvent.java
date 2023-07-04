package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import data.Event;
import database.DBManager;


public class EditEvent extends AppCompatActivity {
    private String username,dangerLevel,eventType,district;
    private int userId;
    private String eventId;
    private final int CAMERA_CODE = 123;
    private final int EDIT_CODE = 321;
    private Button buttonSave;
    private Button buttonCamera;
    private ImageView imageViewEvent;
    private Spinner dangerLevelSpinner, eventTypeSpinner, districtSpinner;
    private EditText descriptionEditText, locationOrAddressText;
    private Event event;
    private Bitmap imageBitmap = null;
    boolean isImageTaken = false;
    private DBManager dbManager;

    // activity launch set fields and listeners
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        dbManager = new DBManager(this);
        dbManager.open();

        init_fields();
        setSpinnersListeners();
        setListenerForGetPicture();

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        username = intent.getStringExtra("username");
        eventId = intent.getStringExtra("eventId");
        setTitle("Edit event");
        setFields();
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "update event1: " + eventType + " userid: " + userId + " user name: " + username );
                Drawable drawable = imageViewEvent.getDrawable();
                Bitmap bitmap;

                if (drawable instanceof BitmapDrawable) {
                    // The drawable is a BitmapDrawable
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                } else if (drawable instanceof VectorDrawable) {
                    // The drawable is a VectorDrawable
                    VectorDrawable vectorDrawable = (VectorDrawable) drawable;
                    bitmap = Bitmap.createBitmap(
                            vectorDrawable.getIntrinsicWidth(),
                            vectorDrawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                    Canvas canvas = new Canvas(bitmap);
                    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    vectorDrawable.draw(canvas);
                } else {
                    // Handle other types of drawables or null case
                    // You can set a default bitmap or handle the case as per your requirements
                    bitmap = null;
                }

                // Continue using the 'bitmap' variable in your code
                event = new Event(
                        eventType,
                        bitmap,
                        descriptionEditText.getText().toString(),
                        locationOrAddressText.getText().toString(),
                        district,
                        dangerLevel,
                        new UserProfile(userId, username)
                );

                //event = new Event(eventType, ((BitmapDrawable) imageViewEvent.getDrawable()).getBitmap(),descriptionEditText.getText().toString(),
                //        locationOrAddressText.getText().toString(), district, dangerLevel,
                //        new UserProfile(userId, username));

                event.setId(Integer.parseInt(eventId));
                Log.d(TAG, "update event2: " + event.toString());
                dbManager.updateEvent(event);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
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
    // get UI controls
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

    // load event data and set fields values
    private void setFields() {
        Event oldEvent = dbManager.getEvent(eventId);
        descriptionEditText.setText(oldEvent.getDescription(), TextView.BufferType.EDITABLE);
        locationOrAddressText.setText(oldEvent.getLocation(), TextView.BufferType.EDITABLE);
        dangerLevelSpinner.setSelection(((ArrayAdapter)dangerLevelSpinner.getAdapter()).getPosition(oldEvent.getSeverity()));
        eventTypeSpinner.setSelection(((ArrayAdapter)eventTypeSpinner.getAdapter()).getPosition(oldEvent.getEventType()));
        districtSpinner.setSelection(((ArrayAdapter)districtSpinner.getAdapter()).getPosition(oldEvent.getDistrict()));
        if (oldEvent.getImgBitmap() != null) imageViewEvent.setImageBitmap(oldEvent.getImgBitmap());
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