package database;
import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.finalprojectpartone.MainActivity;
import com.example.finalprojectpartone.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import data.Comment;
import data.Event;
import data.EventFirebase;
import data.EventToUserConfirmation;
import data.user;

public class FirebaseManager {
    private static final String COLLECTION_NAME = "events_project";
    private static final String COMMENTS_COLLECTION_NAME = "comments";
    private static final String USERS_COLLECTION_NAME = "users";
    private static final String EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME = "event_to_user_confirmation";
    private static final String EVENTS_COLLECTION_NAME = "events";
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private ListenerRegistration listenerRegistration;
    private ListenerRegistration usersListenerRegistration;
    private ListenerRegistration eventToUserConfirmationListenerRegistration;
    private ListenerRegistration eventsListenerRegistration;
    private DBManager dbManager;

    private View rootView; // Root view of the activity/fragment to display Snackbar

    public FirebaseManager(View rootView, DBManager db) {
        this.rootView = rootView;
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        dbManager = db;
        dbManager.open();
    }

    public void startSyncWithSQLite() {
        CollectionReference commentsCollection = firestore.collection(COLLECTION_NAME).document(COMMENTS_COLLECTION_NAME).collection(COMMENTS_COLLECTION_NAME);
        listenerRegistration = commentsCollection.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            // Process the changes in querySnapshot
            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                QueryDocumentSnapshot document = documentChange.getDocument();
                int id = Integer.parseInt(document.getId());
                String creator = document.getString("username");
                String content = document.getString("content");
                int eventId = document.getLong("eventId").intValue();

                switch (documentChange.getType()) {
                    case ADDED:
                        try {
                            dbManager.addComment(id, creator, content, eventId);
                        } catch (DBManagerException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                    case MODIFIED:
                        dbManager.updateComment(id, content, false);
                        break;
                    case REMOVED:
                        dbManager.deleteComment(id, false);
                        break;
                }
            }
        });

        // Listen for changes in the users collection
        CollectionReference usersCollection = firestore.collection(COLLECTION_NAME).document(USERS_COLLECTION_NAME).collection(USERS_COLLECTION_NAME);
        usersListenerRegistration = usersCollection.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            // Process the changes in querySnapshot
            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                QueryDocumentSnapshot document = documentChange.getDocument();
                String userId = document.getId();
                String userName = document.getString("userName");
                String userPassword = document.getString("userPassword");
                Long confirmations = document.getLong("confirmations"); // Use getLong for numbers
                Long rejections = document.getLong("rejections"); // Use getLong for numbers


                switch (documentChange.getType()) {
                    case ADDED:
                        // Insert the user into SQLite database
                        dbManager.insertUser(Integer.parseInt(userId), userName, userPassword, confirmations != null ? confirmations.toString() : "0", rejections != null ? rejections.toString() : "0");
                        break;
                    case MODIFIED:
                        // Update the user in SQLite database
                        dbManager.updateUserConfirmationAndRejection(Integer.parseInt(userId), userName, confirmations != null ? confirmations.toString() : "0");
                        break;
                }
            }
        });

        // Listen for changes in the event_to_user_confirmation collection
        CollectionReference eventToUserConfirmationCollection = firestore.collection(COLLECTION_NAME).document(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME).collection(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME);
        eventToUserConfirmationListenerRegistration = eventToUserConfirmationCollection.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            // Process the changes in querySnapshot
            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                QueryDocumentSnapshot document = documentChange.getDocument();
                int _ID = Integer.parseInt(document.getId());
                int eventId = document.getLong("eventId").intValue();
                int userId = document.getLong("userId").intValue();

                switch (documentChange.getType()) {
                    case ADDED:
                        // Insert the entry into SQLite database
                        dbManager.insertEventToUserConfirmation(_ID, eventId, String.valueOf(userId));
                        break;
                    case REMOVED:
                        // Delete the entry from SQLite database
                        dbManager.deleteEventToUserConfirmation(_ID);
                        break;
                }
            }
        });

        // Listen for changes in the events collection
        CollectionReference eventsCollection = firestore.collection(COLLECTION_NAME).document(EVENTS_COLLECTION_NAME).collection(EVENTS_COLLECTION_NAME);
        eventsListenerRegistration = eventsCollection.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            // Process the changes in querySnapshot
            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                QueryDocumentSnapshot document = documentChange.getDocument();
                int eventId = Integer.parseInt(document.getId());
                String type = document.getString("type");
                String description = document.getString("description");
                String location = document.getString("location");
                String district = document.getString("district");
                String severity = document.getString("severity");
                String date = document.getString("date");
                int confirmations = document.getLong("confirmations").intValue();
                int rejections = document.getLong("rejections").intValue();
                String user = document.getString("user");
                int userId = document.getLong("userId").intValue();
                String imageUrl = document.getString("imageUrl");

                switch (documentChange.getType()) {
                    case ADDED:
                        // Insert the event into SQLite database
                        insertEvent(eventId, type, description, location, district, severity, date,
                                confirmations, rejections, user, userId, imageUrl);
                        break;
                    case MODIFIED:
                        // Update the event in SQLite database
                        updateEvent(eventId, type, description, location, district, severity, date,
                                confirmations, rejections, user, userId, imageUrl);
                        break;
                    case REMOVED:
                        // Delete the event from SQLite database
                        dbManager.deleteEventFromFB(eventId);
                        break;
                }
            }
        });


    }

    public void stopSyncWithSQLite() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        if (usersListenerRegistration != null) {
            usersListenerRegistration.remove();
            usersListenerRegistration = null;
        }
        if (eventToUserConfirmationListenerRegistration != null) {
            eventToUserConfirmationListenerRegistration.remove();
            eventToUserConfirmationListenerRegistration = null;
        }
        if (eventsListenerRegistration != null) {
            eventsListenerRegistration.remove();
            eventsListenerRegistration = null;
        }
    }

    public void addComment(int id, String username, String commentContent, int eventId) {
        CollectionReference commentsCollection = firestore.collection(COLLECTION_NAME).document(COMMENTS_COLLECTION_NAME).collection(COMMENTS_COLLECTION_NAME);

        // Create a new comment document with the given ID
        commentsCollection.document(String.valueOf(id)).set(new Comment(id, commentContent, username, eventId))
                .addOnSuccessListener(aVoid -> showSnackbar("Comment added successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to add comment"));
    }

    public void deleteComment(int id) {
        CollectionReference commentsCollection = firestore.collection(COLLECTION_NAME).document(COMMENTS_COLLECTION_NAME).collection(COMMENTS_COLLECTION_NAME);

        // Delete the comment document with the given ID
        commentsCollection.document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> showSnackbar("Comment deleted successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to delete comment"));
    }

    public void updateComment(int id, String newContent) {
        CollectionReference commentsCollection = firestore.collection(COLLECTION_NAME).document(COMMENTS_COLLECTION_NAME).collection(COMMENTS_COLLECTION_NAME);

        // Update the comment document with the given ID
        commentsCollection.document(String.valueOf(id)).update("content", newContent)
                .addOnSuccessListener(aVoid -> showSnackbar("Comment updated successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to update comment"));
    }

    public void addUser(String userId, String userName, String userPassword, String confirmations, String rejections) {
        CollectionReference usersCollection = firestore.collection(COLLECTION_NAME).document(USERS_COLLECTION_NAME).collection(USERS_COLLECTION_NAME);

        // Create a new user document with the given ID
        usersCollection.document(userId).set(new user(userId, userName, userPassword, confirmations, rejections))
                .addOnSuccessListener(aVoid -> showSnackbar("User added successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to add user"));
    }


    public void updateUserConfirmations(String userId, int incrementValue) {
        CollectionReference usersCollection = firestore.collection(COLLECTION_NAME).document(USERS_COLLECTION_NAME).collection(USERS_COLLECTION_NAME);
        Log.e(TAG, "update user confirmations: user id: " + userId);
        // Update the confirmations column for the user with the given ID
        usersCollection.document(userId).update("confirmations", incrementValue)
                .addOnSuccessListener(aVoid -> showSnackbar("User confirmations updated successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to update user confirmations"));
    }

    public void updateUserRejections(String userId, int incrementValue) {
        CollectionReference usersCollection = firestore.collection(COLLECTION_NAME).document(USERS_COLLECTION_NAME).collection(USERS_COLLECTION_NAME);

        // Update the rejections column for the user with the given ID
        usersCollection.document(userId).update("rejections", incrementValue)
                .addOnSuccessListener(aVoid -> showSnackbar("User rejections updated successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to update user rejections"));
    }

    public void addEventToUserConfirmation(int _ID, int eventId, int userId) {
        CollectionReference eventToUserConfirmationCollection = firestore.collection(COLLECTION_NAME).document(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME).collection(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME);

        // Create a new entry document with the given _ID, eventId, and userId
        eventToUserConfirmationCollection.document(String.valueOf(_ID)).set(new EventToUserConfirmation(_ID, eventId, userId))
                .addOnSuccessListener(aVoid -> showSnackbar("Event to User Confirmation added successfully"))
                .addOnFailureListener(e -> showSnackbar("Failed to add Event to User Confirmation"));
    }


    public void deleteEventToUserConfirmation(int eventId, int userId) {
        CollectionReference eventToUserConfirmationCollection = firestore.collection(COLLECTION_NAME).document(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME).collection(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME);

        // Delete the entry document with the given eventId and userId
        eventToUserConfirmationCollection.whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> showSnackbar("Event to User Confirmation deleted successfully"))
                                .addOnFailureListener(e -> showSnackbar("Failed to delete Event to User Confirmation"));
                    }
                })
                .addOnFailureListener(e -> showSnackbar("Failed to retrieve Event to User Confirmation for deletion"));
    }

//*******************

    private void uploadImage(String eventId, byte[] imageBytes, OnCompleteListener<Uri> onCompleteListener) {
        // Create a reference to the Firebase Storage location
        StorageReference storageRef = firebaseStorage.getReference().child("event_images").child(eventId);

        if (imageBytes == null) {
            // Handle the case when imageBytes is null
            onCompleteListener.onComplete(Tasks.forException(new IllegalArgumentException("Image bytes cannot be null")));
            return;
        }

        // Upload the image byte array to Firebase Storage
        UploadTask uploadTask = storageRef.putBytes(imageBytes);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL of the uploaded image
            storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Uri downloadUri = uri;
                        Task<Uri> task = Tasks.forResult(downloadUri);
                        onCompleteListener.onComplete(task);
                    })
                    .addOnFailureListener(e -> onCompleteListener.onComplete(Tasks.forException(e)));
        }).addOnFailureListener(e -> onCompleteListener.onComplete(Tasks.forException(e)));
    }



    public void addEvent(Event event, byte[] imageBytes) {
        CollectionReference eventsCollection = firestore.collection(COLLECTION_NAME).document(EVENTS_COLLECTION_NAME).collection(EVENTS_COLLECTION_NAME);

        // Generate a unique ID for the event
        String eventId = eventsCollection.document().getId();

        // Upload the image to Firebase Storage
        uploadImage(String.valueOf(event.getId()), imageBytes, task -> {
            if (task.isSuccessful()) {
                // Get the download URL of the uploaded image
                String imageUrl = task.getResult().toString();

                // Set the image URL in the event object
                event.setImageUrl(imageUrl);

                // Save the event document in Firestore
                EventFirebase efirebaseData = new EventFirebase(event.getId(), event.getEventType(), event.getDescription(), event.getLocation(), event.getDistrict(), event.getSeverity(), event.getEventDateString(), event.getConfirmations(), event.getRejections(), event.getUser().getUserName(), event.getUserId(), imageUrl);
                eventsCollection.document(String.valueOf(event.getId())).set(efirebaseData)
                        .addOnSuccessListener(aVoid -> showSnackbar("Event added successfully"))
                        .addOnFailureListener(e -> showSnackbar("Failed to add event"));
            } else {
                event.setImageUrl("");
                Log.d(TAG, "saving date in firebase like this: "  + event.getEventDateString());
                // Save the event document in Firestore
                EventFirebase efirebaseData = new EventFirebase(event.getId(), event.getEventType(), event.getDescription(), event.getLocation(), event.getDistrict(), event.getSeverity(), event.getEventDateString(), event.getConfirmations(), event.getRejections(), event.getUser().getUserName(), event.getUserId(), "");
                eventsCollection.document(String.valueOf(event.getId())).set(efirebaseData)
                        .addOnSuccessListener(aVoid -> showSnackbar("Event added successfully"))
                        .addOnFailureListener(e -> showSnackbar("Failed to add event"));
                showSnackbar("There is no image to add");
            }
        });
    }

    public void updateEvent(Event event, byte[] imageBytes) {
        CollectionReference eventsCollection = firestore.collection(COLLECTION_NAME).document(EVENTS_COLLECTION_NAME).collection(EVENTS_COLLECTION_NAME);

        // Check if the event has an existing image URL
        if (event.getImageUrl() != null && !event.getImageUrl().equals("")) {
            // Update the event document in Firestore without updating the image
            EventFirebase efirebaseData = new EventFirebase(event.getId(), event.getEventType(), event.getDescription(), event.getLocation(), event.getDistrict(), event.getSeverity(), event.getEventDateString(), event.getConfirmations(), event.getRejections(), event.getUser().getUserName(), event.getUserId(), "");

            eventsCollection.document(String.valueOf(event.getId())).set(efirebaseData)
                    .addOnSuccessListener(aVoid -> showSnackbar("Event updated successfully"))
                    .addOnFailureListener(e -> showSnackbar("Failed to update event"));
        } else {
            Log.e(TAG, "updating event part2: " + event.toString());
            // Upload the new image to Firebase Storage
            uploadImage(String.valueOf(event.getId()), imageBytes, task -> {
                if (task.isSuccessful()) {
                    // Get the download URL of the uploaded image
                    String imageUrl = task.getResult().toString();

                    // Set the image URL in the event object
                    event.setImageUrl(imageUrl);

                    // Update the event document in Firestore
                    EventFirebase efirebaseData = new EventFirebase(event.getId(), event.getEventType(), event.getDescription(), event.getLocation(), event.getDistrict(), event.getSeverity(), event.getEventDateString(), event.getConfirmations(), event.getRejections(), event.getUser().getUserName(), event.getUserId(), imageUrl);
                    eventsCollection.document(String.valueOf(event.getId())).set(efirebaseData)
                            .addOnSuccessListener(aVoid -> showSnackbar("Event updated successfully"))
                            .addOnFailureListener(e -> showSnackbar("Failed to update event"));
                } else {
                    EventFirebase efirebaseData = new EventFirebase(event.getId(), event.getEventType(), event.getDescription(), event.getLocation(), event.getDistrict(), event.getSeverity(), event.getEventDateString(), event.getConfirmations(), event.getRejections(), event.getUser().getUserName(), event.getUserId(), "");
                    eventsCollection.document(String.valueOf(event.getId())).set(efirebaseData)
                            .addOnSuccessListener(aVoid -> showSnackbar("Event updated successfully"))
                            .addOnFailureListener(e -> showSnackbar("Failed to update event"));
                    showSnackbar("Failed to upload image");
                }
            });
        }
    }

    public void deleteEvent(int eventId) {
        CollectionReference eventsCollection = firestore.collection(COLLECTION_NAME).document(EVENTS_COLLECTION_NAME).collection(EVENTS_COLLECTION_NAME);

        // Delete the event document from Firestore
        eventsCollection.document(String.valueOf(eventId)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    EventFirebase event = documentSnapshot.toObject(EventFirebase.class);
                    if (event != null && event.getImageUrl() != null && !event.getImageUrl().equals("")) {
                        // Delete the image from Firebase Storage
                        StorageReference storageRef = firebaseStorage.getReferenceFromUrl(event.getImageUrl());
                        storageRef.delete().addOnSuccessListener(aVoid -> {
                            // Delete the event document from Firestore
                            eventsCollection.document(String.valueOf(eventId)).delete()
                                    .addOnSuccessListener(aVoid2 -> showSnackbar("Event deleted successfully"))
                                    .addOnFailureListener(e -> showSnackbar("Failed to delete event"));
                        }).addOnFailureListener(e -> showSnackbar("Failed to delete event image"));
                    } else {
                        // Delete the event document from Firestore if no image URL is present
                        eventsCollection.document(String.valueOf(eventId)).delete()
                                .addOnSuccessListener(aVoid -> showSnackbar("Event deleted successfully"))
                                .addOnFailureListener(e -> showSnackbar("Failed to delete event"));
                    }
                })
                .addOnFailureListener(e -> showSnackbar("Failed to delete event"));
    }



    //***************
    public static void downloadImage(String imageUrl, ImageDownloadCallback callback) {
        if (imageUrl == null) {
            callback.onImageDownloaded(new byte[0]);
            return;
        }

        AsyncTask<Void, Void, byte[]> imageDownloadTask = new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(Void... voids) {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    return outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(byte[] result) {
                callback.onImageDownloaded(result);
            }
        };

        imageDownloadTask.execute();
    }


    public void insertEvent(int eventId, String type, String description, String location, String district,
                            String severity, String date, int confirmations, int rejections, String user, int userId, String imageUrl) {
        // Download the image and insert the event into SQLite with the image BLOB
        downloadImage(imageUrl, imageBytes -> {
            // imageBytes will contain the downloaded image data
            dbManager.insertEventFromFB(eventId, type, description, location, district, severity, date,
                    confirmations, rejections, user, userId, imageBytes);
        });
    }

    public void updateEvent(int eventId, String type, String description, String location, String district,
                            String severity, String date, int confirmations, int rejections, String user, int userId, String imageUrl) {
        // Download the image and update the event in SQLite with the new image BLOB
        downloadImage(imageUrl, imageBytes -> {
            // imageBytes will contain the downloaded image data
            dbManager.updateEventFromFB(eventId, type, description, location, district, severity, date,
                    confirmations, rejections, user, userId, imageBytes);
        });
    }


    public void     fetchCommentsFromFirebaseAndInsertToSQL() {
        //dbManager.open(); // Ensure the database is open
        CollectionReference commentsCollection = firestore.collection(COLLECTION_NAME).document(COMMENTS_COLLECTION_NAME).collection(COMMENTS_COLLECTION_NAME);
        commentsCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        int id = Integer.parseInt(document.getId());
                        String content = document.getString("content");
                        String username = document.getString("username");
                        int eventId = document.getLong("eventId").intValue();

                        // Insert the comment data into the SQL comment table
                        try {
                            dbManager.addComment(id, username, content, eventId);
                        } catch (DBManagerException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Log.d(TAG, "Inserted comment data into SQL table");
                } else {
                    Log.d(TAG, "No comments found in Firebase");
                }
            } else {
                Log.d(TAG, "Failed to fetch comments from Firebase: " + task.getException());
            }

            //dbManager.close(); // Close the database connection
        });
    }

    public void fetchUsersDataFromFirebase() {
        CollectionReference usersCollection = firestore.collection(COLLECTION_NAME).document(USERS_COLLECTION_NAME).collection(USERS_COLLECTION_NAME);
        usersCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String userId = document.getId();
                        String userName = document.getString("userName");
                        String userPassword = document.getString("userPassword");
                        Long confirmations = document.getLong("confirmations"); // Use getLong for numbers
                        Long rejections = document.getLong("rejections"); // Use getLong for numbers

                        // Insert the user into the users table
                        dbManager.insertUser(Integer.parseInt(userId), userName, userPassword, confirmations != null ? confirmations.toString() : "0", rejections != null ? rejections.toString() : "0");
                    }

                })
                .addOnFailureListener(e -> showSnackbar("Failed to populate users table from Firebase"));
    }


    public void fillEventToUserConfirmationTableSQLite() {

        // Fetch event-to-user confirmations data from Firebase
        CollectionReference eventToUserConfirmationCollection = firestore.collection(COLLECTION_NAME).document(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME).collection(EVENT_TO_USER_CONFIRMATION_COLLECTION_NAME);

        eventToUserConfirmationCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<EventToUserConfirmation> eventToUserConfirmations = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        int _ID = Integer.parseInt(document.getId());
                        int eventId = document.getLong("eventId").intValue();
                        int userId = document.getLong("userId").intValue();

                        dbManager.insertEventToUserConfirmation(_ID, eventId, String.valueOf(userId));
                    }


                    Log.d(TAG, "Filled event_to_user_confirmation table in SQLite");

                    showSnackbar("Fetched event-to-user confirmations data from Firebase and filled SQLite table");
                })
                .addOnFailureListener(e -> showSnackbar("Failed to fetch event-to-user confirmations data from Firebase"));
    }

    public void fillEventsTableSQLite() {

        // Fetch events data from Firebase
        CollectionReference eventsCollection = firestore.collection(COLLECTION_NAME).document(EVENTS_COLLECTION_NAME).collection(EVENTS_COLLECTION_NAME);

        eventsCollection.get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        int id = Integer.parseInt(document.getId());
                        String type = document.getString("type");
                        String description = document.getString("description");
                        String location = document.getString("location");
                        String district = document.getString("district");
                        String severity = document.getString("severity");
                        String date = document.getString("date");
                        int confirmations = document.getLong("confirmations").intValue();
                        int rejections = document.getLong("rejections").intValue();
                        String user = document.getString("user");
                        int userId = document.getLong("userId").intValue();
                        String imageUrl = document.getString("imageUrl");

                        insertEvent(id, type, description, location, district, severity, date,
                                confirmations, rejections, user, userId, imageUrl);
                    }

                    Log.d(TAG, "Filled EVENTS table in SQLite");

                    showSnackbar("Fetched events data from Firebase and filled SQLite table");
                })
                .addOnFailureListener(e -> showSnackbar("Failed to fetch events data from Firebase"));

    }

    private void showSnackbar(String message) {
        if (rootView != null) {
            Snackbar.make(rootView, "firebase message: " + message, Snackbar.LENGTH_SHORT).show();
        }
    }

    public interface ImageDownloadCallback {
        void onImageDownloaded(byte[] imageBytes);
    }
}
