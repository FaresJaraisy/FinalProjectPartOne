package database;

import static android.content.ContentValues.TAG;
import static database.DatabaseHelper.*;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;

import com.example.finalprojectpartone.FilterSettings;
import com.example.finalprojectpartone.R;
import com.example.finalprojectpartone.UserProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Comment;
import data.Event;
import data.UserEventCount;

public class DBManager {
    private DatabaseHelper dbHelper;
    private final Context context;
    private SQLiteDatabase database;
    public static final String INC = "1";

    FirebaseManager firebaseManager;

    public DBManager(Context c, View rootView) {
        context = c;
        firebaseManager = new FirebaseManager(rootView, this);
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public int update(long _id, String name, String desc) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SUBJECT, name);
        contentValues.put(DatabaseHelper.DESC, desc);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

     public boolean checkUserLogin(String username,String password){
        String whereclause = "USER_NAME=? and USER_PASSWORD=?"; //<<<<<<<<<< ?'s will be replaced according to whereargs on a 1 by 1 basis
        String[] whereargs = new String[]{username,password};
        Cursor cursor = database.query(
                "USERS",
                new String[]{"USER_NAME","USER_PASSWORD"},
                whereclause,
                whereargs,
                null,null,null
        );
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    /*
    This METHOD is used to get the user and check if the provided password is correct
    If the user and password exists it returns a UserProfile object else returns null
     */
    public UserProfile getUser(String username){
        UserProfile userProfile = null;
        String whereclause = "USER_NAME=?"; //<<<<<<<<<< ?'s will be replaced according to whereargs on a 1 by 1 basis
        String[] whereargs = new String[]{username};
        Cursor cursor = database.query(
                "USERS",
                new String[]{"_id","USER_NAME"},
                whereclause,
                whereargs,
                null,null,null
        );

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            userProfile = new UserProfile(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1));
        }
        cursor.close();
        return userProfile;
    }

    public void insertUser(String username, String password) {

        ContentValues values = new ContentValues();
        values.put(USER_NAME_COL, username);
        values.put(USER_PASSWORD_COL, password);
        values.put(CONFIRMATIONS_COL, "0");
        values.put(REJECTIONS_COL, "0");

        long newRowId = database.insert(USERS_TABLE, null, values);

        if (newRowId != -1) {
            Log.d(TAG, "User added to sql successfully.");
        } else {
            Log.d(TAG, "Failed to add user to sql.");
        }
        firebaseManager.addUser(String.valueOf(newRowId), username, password, "0", "0");
    }

    public void insertUser(int id, String username, String password, String confirmations, String rejections) {

        ContentValues values = new ContentValues();
        values.put(_ID, id);
        values.put(USER_NAME_COL, username);
        values.put(USER_PASSWORD_COL, password);
        values.put(CONFIRMATIONS_COL, confirmations);
        values.put(REJECTIONS_COL, rejections);

        long newRowId = database.insert(USERS_TABLE, null, values);

        if (newRowId != -1) {
            Log.d(TAG, "User added to sql successfully.");
        } else {
            Log.d(TAG, "Failed to add user to sql.");
        }
    }

    public void updateUserConfirmationAndRejection(int userId, String newConfirmation, String newRejection) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.CONFIRMATIONS_COL, newConfirmation);
        contentValues.put(DatabaseHelper.REJECTIONS_COL, newRejection);

        // Execute the UPDATE query
        int rowsUpdated = database.update(DatabaseHelper.USERS_TABLE, contentValues, DatabaseHelper._ID + " = ?", new String[]{String.valueOf(userId)});

        if (rowsUpdated > 0) {
            Log.d(TAG, "Columns updated successfully.");
        } else {
            Log.d(TAG, "Failed to update columns.");
        }
    }



    // Save new Event to DB and add current date time to the event row
    public void insertEvent(Event event) throws ParseException {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.TYPE_COL, event.getEventType());
        contentValue.put(DatabaseHelper.IMAGE_COL, event.getBitmapAsByteArray());
        contentValue.put(DatabaseHelper.DESCRIPTION_COL, event.getDescription());
        contentValue.put(DatabaseHelper.LOCATION_COL, event.getLocation());
        contentValue.put(DatabaseHelper.DISTRICT_COL, event.getDistrict());
        contentValue.put(DatabaseHelper.SEVERITY_COL, event.getSeverity());
        contentValue.put(DatabaseHelper.USER_COL, event.getUser().getUserName());
        contentValue.put(DatabaseHelper.USER_ID_COL, event.getUser().getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(new Date());
        contentValue.put(DatabaseHelper.DATE_COL, strDate);
        contentValue.put(DatabaseHelper.CONFIRMATIONS_COL, 0);
        contentValue.put(DatabaseHelper.REJECTIONS_COL, 0);

        long rowId = database.insert(DatabaseHelper.EVENTS_TABLE_NAME, null, contentValue);
        if (rowId != -1) {
            Event updatedEvent = getEvent(String.valueOf(rowId));
            Log.d(TAG, "event to add: " + updatedEvent.toString());
            firebaseManager.addEvent(updatedEvent, updatedEvent.getBitmapAsByteArray());
        }
    }


    // This method confirms the event
    // A check is made if it was already approved by this user, if so early exit without changes
    public void confirmEvent(String id, int userid) throws ParseException {
        String[] selectionArgs = new String[]{String.valueOf(userid),id};
        String[] cols = new String[]{DatabaseHelper.USER_ID_COL,DatabaseHelper.EVENT_ID_COL};

        Cursor cursor = database.query(DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE,cols,
                DatabaseHelper.USER_ID_COL + "=? AND " + DatabaseHelper.EVENT_ID_COL + "=?",
                selectionArgs, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            return;
        }
        String[] whereargs = new String[]{String.valueOf(id)};
        String[] bindingArgs = new String[]{ INC, DatabaseHelper._ID };
        database.execSQL("UPDATE " + DatabaseHelper.EVENTS_TABLE_NAME +
                        " SET " + DatabaseHelper.CONFIRMATIONS_COL +
                        " = " + DatabaseHelper.CONFIRMATIONS_COL + " + " + INC +
                        " WHERE " + DatabaseHelper._ID + " = " + id);

        Event updatedEvent = getEvent(id);
        firebaseManager.updateEvent(updatedEvent, updatedEvent.getBitmapAsByteArray());
        //update event to user confirmation table:
        String insSql = "INSERT OR IGNORE INTO " + DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE +
                "(" + DatabaseHelper.USER_ID_COL + ", " + DatabaseHelper.EVENT_ID_COL +") VALUES(" +
                userid + ", " + id + " )";
        database.execSQL(insSql);

        // Get the last inserted row ID
        cursor = database.rawQuery("SELECT last_insert_rowid() AS rowid", null);
        int lastInsertedRowId = -1;
        if (cursor.moveToFirst()) {
            lastInsertedRowId = cursor.getInt(cursor.getColumnIndex("rowid"));
        }
        cursor.close();
        firebaseManager.addEventToUserConfirmation(lastInsertedRowId, Integer.parseInt(id), userid);

        /*database.execSQL("UPDATE " + DatabaseHelper.USERS_TABLE +
                " SET " + DatabaseHelper.CONFIRMATIONS_COL +
                " = " + DatabaseHelper.CONFIRMATIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + userid);*/
        updateUserConfirmations(userid, 1);

    }
    public void updateUserConfirmations(int userId, int incrementValue) {
        // Get the current value of the confirmations column
        Cursor cursor = database.query(DatabaseHelper.USERS_TABLE, new String[]{DatabaseHelper.CONFIRMATIONS_COL}, DatabaseHelper._ID + " = ?", new String[]{String.valueOf(userId)}, null, null, null);
        int currentValue = 0;
        if (cursor.moveToFirst()) {
            currentValue = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CONFIRMATIONS_COL));
        }
        cursor.close();

        // Calculate the updated value
        int updatedValue = currentValue + incrementValue;

        // Update the confirmations column with the new value
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.CONFIRMATIONS_COL, updatedValue);
        int rowsUpdated = database.update(DatabaseHelper.USERS_TABLE, contentValues, DatabaseHelper._ID + " = ?", new String[]{String.valueOf(userId)});

        if (rowsUpdated > 0) {
            Log.d(TAG, "Updated user " + userId + " confirmations to: " + updatedValue);
            // Call the appropriate method to update the value in Firebase
            firebaseManager.updateUserConfirmations(String.valueOf(userId), updatedValue);
        }
    }

    // This method rejects the event (as a separate value of the confirmations)
    // A check is made if it was already confirmed by this user, if so early exit without changes
    public void rejectEvent(String id, int userId) throws ParseException {
        String[] bindingArgs = new String[]{ INC, DatabaseHelper._ID };
        database.execSQL("UPDATE " + DatabaseHelper.EVENTS_TABLE_NAME +
                " SET " + REJECTIONS_COL +
                " = " + REJECTIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + id);
        Event updatedEvent = getEvent(id);
        firebaseManager.updateEvent(updatedEvent, updatedEvent.getBitmapAsByteArray());
       /* database.execSQL("UPDATE " + DatabaseHelper.USERS_TABLE +
                " SET " + REJECTIONS_COL +
                " = " + REJECTIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + userId);*/
        updateUserRejections(userId, 1);
    }

    public void updateUserRejections(int userId, int incrementValue) {
        // Get the current value of the rejections column
        Cursor cursor = database.query(DatabaseHelper.USERS_TABLE, new String[]{DatabaseHelper.REJECTIONS_COL}, DatabaseHelper._ID + " = ?", new String[]{String.valueOf(userId)}, null, null, null);
        int currentValue = 0;
        if (cursor.moveToFirst()) {
            currentValue = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.REJECTIONS_COL));
        }
        cursor.close();

        // Calculate the updated value
        int updatedValue = currentValue + incrementValue;

        // Update the rejections column with the new value
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.REJECTIONS_COL, updatedValue);
        int rowsUpdated = database.update(DatabaseHelper.USERS_TABLE, contentValues, DatabaseHelper._ID + " = ?", new String[]{String.valueOf(userId)});

        if (rowsUpdated > 0) {
            Log.d(TAG, "Updated user " + userId + " rejections to: " + updatedValue);
            // Call the appropriate method to update the value in Firebase
            firebaseManager.updateUserRejections(String.valueOf(userId), updatedValue);
        }
    }


    //used for mapping the cursor adaptor
    String[] eventsCols = new String[] {
            DatabaseHelper.USER_COL,
            DatabaseHelper._ID,
            DatabaseHelper.DATE_COL,
            DatabaseHelper.TYPE_COL,
            DatabaseHelper.IMAGE_COL,
            DatabaseHelper.DESCRIPTION_COL,
            DatabaseHelper.LOCATION_COL,
            DatabaseHelper.DISTRICT_COL,
            DatabaseHelper.SEVERITY_COL,
            DatabaseHelper.CONFIRMATIONS_COL,
            REJECTIONS_COL,
            DatabaseHelper._ID,
            DatabaseHelper._ID,
            DatabaseHelper._ID
    };

    int[] toViewIDs = new int[]{
            R.id.textViewUserName,
            R.id.textViewId,
            R.id.textViewDate,
            R.id.textViewType,
            R.id.imageViewEventImage,
            R.id.textViewDescription,
            R.id.textViewLocation,
            R.id.textViewDistrict,
            R.id.textViewSeverity,
            R.id.textViewConfirmations,
            R.id.textViewRejections,
            R.id.buttonConfirm,
            R.id.buttonReject,
            R.id.showCommentsId
    };

    int[] toMyEventsViewIDs = new int[]{
            R.id.textViewUserName,
            R.id.textViewId,
            R.id.textViewDate,
            R.id.textViewType,
            R.id.imageViewEventImage,
            R.id.textViewDescription,
            R.id.textViewLocation,
            R.id.textViewDistrict,
            R.id.textViewSeverity,
            R.id.textViewConfirmations,
            R.id.textViewRejections,
            R.id.buttonEditEvent,
            R.id.buttonDeleteEvent,
            R.id.showCommentsId
    };

    // get a cursor adaptor of specific user events
    public SimpleCursorAdapter populateMyEventsListView(String user) {
        Cursor cursor = getMyEventsCursor(user);
        SimpleCursorAdapter eventsAdapter = new SimpleCursorAdapter(
                context,
                R.layout.my_event_item,
                cursor,
                eventsCols,
                toMyEventsViewIDs);
        return eventsAdapter;
    }

    // get a cursor adaptor of all users events
    public SimpleCursorAdapter populateAllUsersEventsListView() {
        Cursor cursor = getAllEventsCursor();
        SimpleCursorAdapter eventsAdapter = new SimpleCursorAdapter(
                context,
                R.layout.event_item,
                cursor,
                eventsCols,
                toViewIDs);
        return eventsAdapter;
    }

    // get a cursor adaptor of all users events
    public Cursor getAllEventsCursor() {
        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, eventsCols, null, null, null, null, null);
        return cursor;
    }

    // get a cursor adaptor of one users events
    public Cursor getMyEventsCursor(String user) {
        String[] selectionArgs = {user};
        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, eventsCols, DatabaseHelper.USER_COL + "=?", selectionArgs, null, null, null);
        return cursor;
    }

    // get a filtered cursor by applying the user required parameters
    // and querying DB accordingly
    public Cursor getAllEventsCursorFiltered(FilterSettings filterSettings) {
        final String ALL = "All";
        String tables = DatabaseHelper.EVENTS_TABLE_NAME;
        String orderBy = null;
        String selectionStr = "";
        ArrayList selectionArgs = new ArrayList<String>();
        String whereStr = "";

        String sql = "SELECT " +
                DatabaseHelper.USER_COL + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID + "," +
                DatabaseHelper.DATE_COL + "," +
                DatabaseHelper.TYPE_COL + "," +
                DatabaseHelper.IMAGE_COL + "," +
                DatabaseHelper.DESCRIPTION_COL + "," +
                DatabaseHelper.LOCATION_COL + "," +
                DatabaseHelper.DISTRICT_COL + "," +
                DatabaseHelper.SEVERITY_COL + "," +
                DatabaseHelper.CONFIRMATIONS_COL + "," +
                REJECTIONS_COL + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID  + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID  +
                " FROM " + DatabaseHelper.EVENTS_TABLE_NAME ;
        if (filterSettings.isApprovedByMe()) {
            sql = sql + " INNER JOIN " + DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE + " ON " +
                    DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID + " = " +
                    DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE+ "." + DatabaseHelper.EVENT_ID_COL;
            whereStr = DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE+ "." +
                    DatabaseHelper.USER_ID_COL + "=?";
            selectionArgs.add(filterSettings.getUserId());
        }

        if (!filterSettings.getRiskLevel().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.SEVERITY_COL + "=" +
                        filterSettings.getRiskLevel();
            else whereStr = DatabaseHelper.SEVERITY_COL + "=?" ;
            selectionArgs.add(filterSettings.getRiskLevel());
        }

        if (!filterSettings.getType().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.TYPE_COL + "=" +
                        filterSettings.getType();
            else whereStr = DatabaseHelper.TYPE_COL + "=?";
            selectionArgs.add(filterSettings.getType());
        }

        if (!filterSettings.getDistrict().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.DISTRICT_COL + "=" +
                        filterSettings.getDistrict();
            else whereStr = DatabaseHelper.DISTRICT_COL + "=?";
            selectionArgs.add(filterSettings.getDistrict());
        }

        if (!whereStr.isEmpty()) sql = sql + " WHERE " + whereStr;
        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgsArray = (String[]) selectionArgs.toArray(selectionArgsArray);
        if (filterSettings.isOrderByDate()) {
            sql = sql + " Order BY " + DatabaseHelper.DATE_COL + " DESC";
        }

        Cursor cursor = database.rawQuery(sql, selectionArgsArray) ;
        return cursor;
    }
    // get all events of specific user filtered
    public Cursor getMyEventsCursorFiltered(FilterSettings filterSettings) {
        final String ALL = "All";
        String tables = DatabaseHelper.EVENTS_TABLE_NAME;
        String orderBy = null;
        String selectionStr = "";
        ArrayList selectionArgs = new ArrayList<String>();
        String whereStr = "";

        String sql = "SELECT " +
                DatabaseHelper.USER_COL + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID + "," +
                DatabaseHelper.DATE_COL + "," +
                DatabaseHelper.TYPE_COL + "," +
                DatabaseHelper.IMAGE_COL + "," +
                DatabaseHelper.DESCRIPTION_COL + "," +
                DatabaseHelper.LOCATION_COL + "," +
                DatabaseHelper.DISTRICT_COL + "," +
                DatabaseHelper.SEVERITY_COL + "," +
                DatabaseHelper.CONFIRMATIONS_COL + "," +
                REJECTIONS_COL + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID  + "," +
                DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper._ID  +
                " FROM " + DatabaseHelper.EVENTS_TABLE_NAME ;

        if (!filterSettings.getRiskLevel().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.SEVERITY_COL + "=" +
                        filterSettings.getRiskLevel();
            else whereStr = DatabaseHelper.SEVERITY_COL + "=?" ;
            selectionArgs.add(filterSettings.getRiskLevel());
        }

        if (!filterSettings.getType().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.TYPE_COL + "=" +
                        filterSettings.getType();
            else whereStr = DatabaseHelper.TYPE_COL + "=?";
            selectionArgs.add(filterSettings.getType());
        }

        if (!filterSettings.getDistrict().equals(ALL)) {
            if (!whereStr.isEmpty())
                whereStr = whereStr + " AND " + DatabaseHelper.DISTRICT_COL + "=" +
                        filterSettings.getDistrict();
            else whereStr = DatabaseHelper.DISTRICT_COL + "=?";
            selectionArgs.add(filterSettings.getDistrict());
        }

        if (!whereStr.isEmpty()) whereStr = whereStr + " AND "  +
            DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper.USER_ID_COL  + "=?";
        else whereStr = whereStr + " " + DatabaseHelper.EVENTS_TABLE_NAME + "." + DatabaseHelper.USER_ID_COL  + "=?";
        selectionArgs.add(filterSettings.getUserId());

        if (!whereStr.isEmpty()) sql = sql + " WHERE " + whereStr;
        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgsArray = (String[]) selectionArgs.toArray(selectionArgsArray);
        if (filterSettings.isOrderByDate()) {
            sql = sql + " Order BY " + DatabaseHelper.DATE_COL + " DESC";
        }

        Cursor cursor = database.rawQuery(sql, selectionArgsArray) ;
        return cursor;
    }

    // get the total rejected events of a specific user
    public int getUserRejections(int id) {
        int rejections = 0;
        String[] whereargs = new String[]{String.valueOf(id)};
        Cursor cursor = database.rawQuery("SELECT " + REJECTIONS_COL + " FROM " +
                DatabaseHelper.USERS_TABLE + " WHERE " +
                DatabaseHelper._ID + " = ?", whereargs);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            rejections = cursor.getInt(0);
        }
        cursor.close();
        return rejections;
    }

    public int getPointsOfConfirmationAndRejectionsEventsByUser(int id)
    {
        int userConfirmations = getUserConfirmations(id);
        int userRejections = getUserRejections(id);
        int totalPoints = (userConfirmations + userRejections) * 3;
        return totalPoints;
    }

    // The method returns the number of events confirmed by the user
    public int getUserConfirmations(int id) {
        int confirmations=0;
        String[] whereargs = new String[]{String.valueOf(id)};
        Cursor cursor = database.rawQuery("SELECT count(" + DatabaseHelper.USER_ID_COL + ")er FROM " +
                DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE + " WHERE " +
                DatabaseHelper.USER_ID_COL + " = ?", whereargs);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            confirmations = cursor.getInt(0);
        }
        cursor.close();
        return confirmations;
    }


    // The method returns the number of events reported by the user
    public int getUsersEventsReportedCount(String user) {
        int eventsCount=0;
        String[] whereargs = new String[]{user};
        Cursor cursor = database.rawQuery("SELECT count(USER) FROM " +
                DatabaseHelper.EVENTS_TABLE_NAME + " WHERE " +
                DatabaseHelper.USER_COL + " = ?", whereargs);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            eventsCount = cursor.getInt(0);
        }
        cursor.close();
        return eventsCount;
    }

    public int getCreatedEventsPoints(String userColValue) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + EVENTS_TABLE_NAME + " WHERE " + USER_COL + " = ? AND " +
                CONFIRMATIONS_COL + " > (0.7 * (" + CONFIRMATIONS_COL + " + " + REJECTIONS_COL + "))";

        String[] selectionArgs = {userColValue};
        Cursor cursor = database.rawQuery(query, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }
        return count * 10;
    }

    /**
     * Get all comments created by author (username)
     * @param author
     * @return
     */
    public List<Comment> getCommentsByCreator(String author) throws DBManagerException {
        List<Comment> result = new ArrayList<>();
        String getCommentsByCreatorQuery = "SELECT * " +
                "FROM " + COMMENTS_TABLE +
                "WHERE " + CREATOR_COL + " = ?;";
        String[] selectionArgs = {author};
        Cursor cursor = database.rawQuery(getCommentsByCreatorQuery, selectionArgs);

        if(cursor.moveToFirst()){
            do{
                int idIndex = cursor.getColumnIndex(_ID);
                int contentIndex = cursor.getColumnIndex(CONTENT_COL);
                int eventIdIndex = cursor.getColumnIndex(EVENT_ID_COL);

                if(contentIndex < 0 || eventIdIndex < 0 || idIndex < 0){
                    //if at least one of the indices is negative
                    // throw exception
                    throw new DBManagerException("wrong column index");
                }
                int _id = cursor.getInt(idIndex);
                String content = cursor.getString(contentIndex);
                int eventId = cursor.getInt(eventIdIndex);

                Comment comment = new Comment(_id, content, author, eventId);

                result.add(comment);
            } while(cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * Get comments by eventId
     * @param eventId
     * @return
     */
    public List<Comment> getCommentsByEventId(int eventId) throws DBManagerException {
        List<Comment> result = new ArrayList<>();
        String getCommentsByEventIdQuery = "SELECT * " +
                "FROM " + COMMENTS_TABLE +
                " WHERE " + EVENT_ID_COL + " = ?;";
        String[] selectionArgs = {String.valueOf(eventId)};
        Cursor cursor = database.rawQuery(getCommentsByEventIdQuery, selectionArgs);
        if(cursor.moveToFirst()){
            do{
                int idIndex = cursor.getColumnIndex(_ID);
                int authorIndex = cursor.getColumnIndex(CREATOR_COL);
                int contentIndex = cursor.getColumnIndex(CONTENT_COL);

                if(authorIndex < 0 || contentIndex < 0 || idIndex < 0){
                    //if at least one of the indices is negative
                    // throw exception
                    throw new DBManagerException("wrong column index");
                }

                int _id = cursor.getInt(idIndex);
                String author = cursor.getString(authorIndex);
                String content = cursor.getString(contentIndex);

                Comment comment = new Comment(_id, content, author, eventId);

                result.add(comment);
            } while(cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * Deletes a comment given an Id
     * @param _id
     * @return
     */

    public boolean deleteComment(int _id, boolean modifyFirebase){
        String whereClause = "_id = ?";
        String[] whereArgs = {String.valueOf(_id)};
        int rowsDeleted = database.delete(COMMENTS_TABLE, whereClause, whereArgs);
        if(modifyFirebase)
        {
            firebaseManager.deleteComment(_id);
        }

        return rowsDeleted > 0;
    }

    /**
     * Updates a comment with the new content given an id.
     * @param _id
     * @param newContent
     * @return
     */
    public boolean updateComment(int _id, String newContent, boolean modifyFireBase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTENT_COL, newContent);
        String whereClause = "_id = ?";
        String[] whereArgs = {String.valueOf(_id)};

        int rowsUpdated = database.update(COMMENTS_TABLE,contentValues, whereClause, whereArgs);
        if(modifyFireBase) {
            firebaseManager.updateComment(_id, newContent);
        }
        return rowsUpdated > 0;
    }


    /**
     *  Adds a new comment to the database
     * @param username
     * @param commentContent
     * @return
     */
    public boolean addComment(String username, String commentContent, int eventId) throws DBManagerException {
        boolean insertSuccess = false;

        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.CREATOR_COL, username);
        contentValue.put(DatabaseHelper.CONTENT_COL, commentContent);
        contentValue.put(DatabaseHelper.EVENT_ID_COL, eventId);

        if(!checkCommentValidity(username, eventId)){
            throw new DBManagerException("Cannot add comment to event reported by comment creator");
        }


        long rowId = database.insert(DatabaseHelper.COMMENTS_TABLE, null, contentValue);
        if (rowId > -1){
            insertSuccess = true;
        }
        firebaseManager.addComment((int)rowId, username, commentContent, eventId);

        return insertSuccess;

    }

    public boolean addComment(int _id, String username, String commentContent, int eventId) throws DBManagerException {
        boolean insertSuccess = false;

        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper._ID, _id);
        contentValue.put(DatabaseHelper.CREATOR_COL, username);
        contentValue.put(DatabaseHelper.CONTENT_COL, commentContent);
        contentValue.put(DatabaseHelper.EVENT_ID_COL, eventId);

        long rowId = database.insert(DatabaseHelper.COMMENTS_TABLE, null, contentValue);
        if (rowId > -1){
            insertSuccess = true;
        }

        return insertSuccess;

    }

    /**
     * Helper function to check if the user creating the comment is adding it to
     * an event NOT created by the user.
     * @param creator
     * @param eventId
     * @return
     */
    private boolean checkCommentValidity(String creator, int eventId){
        boolean result = false;
        Log.d(TAG, "check if " + creator + " has created event " + eventId );
        String getEventCreatorQuery = "SELECT " + DatabaseHelper.USER_COL +
                " FROM " + DatabaseHelper.EVENTS_TABLE_NAME +
                " WHERE _id = ?;";

        String[] selectionArgs = {String.valueOf(eventId)};
        Cursor cursor = database.rawQuery(getEventCreatorQuery, selectionArgs);

        String reporter = null;
        if (cursor != null && cursor.moveToFirst()) {
            reporter = cursor.getString(0);
        }

        if (cursor != null) {
            cursor.close();
        }
        Log.d(TAG, "event reporter " + reporter + " comment creator " + creator);
        if (reporter == null || !reporter.equals(creator)){
            result = true;
        }
        return result;
    }

    // delete event by event id and remove conformation from EVENT_TO_USER_CONFIRMATION_TABLE
    public void deleteEvent(String id, int userId) {
        String[] whereArgs = {id};
        String[] whereConfArgs = {String.valueOf(userId),id};
        database.delete(DatabaseHelper.EVENTS_TABLE_NAME, DatabaseHelper._ID + "=?" , whereArgs);
        firebaseManager.deleteEvent(Integer.parseInt(id));
        // DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE
        database.delete(DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE,
                DatabaseHelper.USER_ID_COL + "=? AND " + DatabaseHelper.EVENT_ID_COL + "=?",
                whereConfArgs);
        firebaseManager.deleteEventToUserConfirmation(Integer.parseInt(id), userId);
    }

    // get event by id
    public Event getEvent(String eventId) throws ParseException {
        Event ret = null;
        Bitmap bitmap = null;
        byte[] byteArray;
        String[] eventCols = {
                DatabaseHelper._ID,
                DatabaseHelper.TYPE_COL,
                DatabaseHelper.IMAGE_COL,
                DatabaseHelper.DESCRIPTION_COL,
                DatabaseHelper.LOCATION_COL,
                DatabaseHelper.DISTRICT_COL,
                DatabaseHelper.SEVERITY_COL,
                DatabaseHelper.DATE_COL,
                DatabaseHelper.CONFIRMATIONS_COL,
                DatabaseHelper.REJECTIONS_COL,
                DatabaseHelper.USER_COL,
                DatabaseHelper.USER_ID_COL
        };

        String[] selectionArgs = {eventId};
        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, eventCols, DatabaseHelper._ID + "=?", selectionArgs, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            byteArray = cursor.getBlob(2);
            if (byteArray != null) {
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            }

            ret = new Event(
                    cursor.getInt(0), // _id column, assuming it's at index 0
                    cursor.getString(1), // TYPE_COL
                    bitmap,
                    cursor.getString(3), // DESCRIPTION_COL
                    cursor.getString(4), // LOCATION_COL
                    cursor.getString(5), // DISTRICT_COL
                    cursor.getString(6), // SEVERITY_COL
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(7)), // DATE_COL
                    cursor.getInt(8), // CONFIRMATIONS_COL
                    cursor.getInt(9), // REJECTIONS_COL
                    new UserProfile(
                            cursor.getInt(11), // USER_ID_COL
                            cursor.getString(10) // USER_COL
                    ),
                    "" // Assuming imageUrl is not stored in the SQLite table
            );


        }
        //Event(eventType, Bitmap imgBitmap, description, location, district, severity, UserProfile user)
        cursor.close();
        return ret;
    }

    // update event by an event object
    public void updateEvent(Event event) throws ParseException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.TYPE_COL,event.getEventType());
        contentValues.put(DatabaseHelper.IMAGE_COL,event.getBitmapAsByteArray());
        contentValues.put(DatabaseHelper.DESCRIPTION_COL,event.getDescription());
        contentValues.put(DatabaseHelper.LOCATION_COL,event.getLocation());
        contentValues.put(DatabaseHelper.DISTRICT_COL,event.getDistrict());
        contentValues.put(DatabaseHelper.SEVERITY_COL,event.getSeverity());

        String[] whereArgs = {String.valueOf(event.getId())};
        int i = database.update(DatabaseHelper.EVENTS_TABLE_NAME, contentValues, DatabaseHelper._ID + " =?", whereArgs);

        Event updatedEvent = getEvent(String.valueOf(event.getId()));

        firebaseManager.updateEvent(updatedEvent, updatedEvent.getBitmapAsByteArray());
    }

    // get top 10 users based on the number of events reported
    public List<UserEventCount> getTop10ReportingUsers() {
        List<UserEventCount> topUsers = new ArrayList<>();

        // Query to retrieve the number of events created by each user
        String query = "SELECT " + USER_COL + ", COUNT(*) AS eventCount " +
                "FROM " + EVENTS_TABLE_NAME +
                " GROUP BY " + USER_COL  +
                " ORDER BY eventCount DESC " +
                "LIMIT 10";

        Cursor cursor = database.rawQuery(query, null);

        // Iterate over the cursor to retrieve user and event count
        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.USER_COL));
                int eventCount = cursor.getInt(cursor.getColumnIndex("eventCount"));

                UserEventCount userEventCount = new UserEventCount(username, eventCount);
                topUsers.add(userEventCount);
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Sort according to top user to less reporting number
        Collections.sort(topUsers, new Comparator<UserEventCount>() {
            @Override
            public int compare(UserEventCount u1, UserEventCount u2) {
                return Integer.compare(u2.getEventCount(), u1.getEventCount());
            }
        });

        return topUsers;
    }

    //getCreatedEventsPoints(String userColValue)
    //getPointsOfConfirmationAndRejectionsEventsByUser(int id)
    //totalPointsByConfirmations = dbManager.getPointsOfConfirmationAndRejectionsEventsByUser(userId);
    //totalPointsByEventsCreated = dbManager.getCreatedEventsPoints(username);
    public UserEventCount getBestUserPoints()
    {
        UserEventCount userEventMaxCount = new UserEventCount("", 0);

        // Query to retrieve all users
        String query = "SELECT * FROM " + USERS_TABLE;

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int userId = cursor.getInt(cursor.getColumnIndex(_ID));
                String username = cursor.getString(cursor.getColumnIndex(USER_NAME_COL));
                int currrentPoints = getPointsOfConfirmationAndRejectionsEventsByUser(userId);
                currrentPoints += getCreatedEventsPoints(username);
                if(currrentPoints > userEventMaxCount.getEventCount())
                {
                    userEventMaxCount.setUsername(username);
                    userEventMaxCount.setEventCount(currrentPoints);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return userEventMaxCount;
    }

    public int getNumberOfLowSeverityEvents() {
        String[] columns = {SEVERITY_COL};
        String selection = SEVERITY_COL + " = ?";
        String[] selectionArgs = {"Low"};

        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        return count;
    }

    public int getNumberOfMediumSeverityEvents() {
        String[] columns = {SEVERITY_COL};
        String selection = SEVERITY_COL + " = ?";
        String[] selectionArgs = {"Medium"};

        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        return count;
    }

    public int getNumberOfHighSeverityEvents() {
        String[] columns = {SEVERITY_COL};
        String selection = SEVERITY_COL + " = ?";
        String[] selectionArgs = {"High"};

        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        return count;
    }

    public void insertEventToUserConfirmation(int _ID, int eventId, String userId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper._ID, _ID);
        values.put(DatabaseHelper.EVENT_ID_COL, eventId);
        values.put(DatabaseHelper.USER_ID_COL, userId);

        long newRowId = database.insert(DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE, null, values);

        if (newRowId != -1) {
            Log.d(TAG, "Event to User Confirmation added to SQLite successfully.");
        } else {
            Log.d(TAG, "Failed to add Event to User Confirmation to SQLite.");
        }
    }

    public void deleteEventToUserConfirmation(int _ID) {
        int rowsDeleted = database.delete(DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE,
                DatabaseHelper._ID + " = ?",
                new String[]{String.valueOf(_ID)});

        if (rowsDeleted > 0) {
            Log.d(TAG, "Event to User Confirmation deleted from SQLite successfully.");
        } else {
            Log.d(TAG, "Failed to delete Event to User Confirmation from SQLite.");
        }
    }

    public void updateEventFromFB(int eventId, String type, String description, String location, String district,
                            String severity, String date, int confirmations, int rejections, String user, int userId, byte[] imageBytes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TYPE_COL, type);
        contentValues.put(DatabaseHelper.DESCRIPTION_COL, description);
        contentValues.put(DatabaseHelper.LOCATION_COL, location);
        contentValues.put(DatabaseHelper.DISTRICT_COL, district);
        contentValues.put(DatabaseHelper.SEVERITY_COL, severity);
        contentValues.put(DatabaseHelper.DATE_COL, date);
        contentValues.put(DatabaseHelper.CONFIRMATIONS_COL, confirmations);
        contentValues.put(DatabaseHelper.REJECTIONS_COL, rejections);
        contentValues.put(DatabaseHelper.USER_COL, user);
        contentValues.put(DatabaseHelper.USER_ID_COL, userId);
        contentValues.put(DatabaseHelper.IMAGE_COL, imageBytes);

        String whereClause = DatabaseHelper._ID + " = ?";
        String[] whereArgs = {String.valueOf(eventId)};

        int rowsUpdated = database.update(DatabaseHelper.EVENTS_TABLE_NAME, contentValues, whereClause, whereArgs);
        if (rowsUpdated > 0) {
            Log.d(TAG, "Event updated successfully");
        }
    }

    public void insertEventFromFB(int eventId, String type, String description, String location, String district,
                            String severity, String date, int confirmations, int rejections, String user, int userId, byte[] imageBytes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper._ID, eventId);
        contentValues.put(DatabaseHelper.TYPE_COL, type);
        contentValues.put(DatabaseHelper.DESCRIPTION_COL, description);
        contentValues.put(DatabaseHelper.LOCATION_COL, location);
        contentValues.put(DatabaseHelper.DISTRICT_COL, district);
        contentValues.put(DatabaseHelper.SEVERITY_COL, severity);
        contentValues.put(DatabaseHelper.DATE_COL, date);
        contentValues.put(DatabaseHelper.CONFIRMATIONS_COL, confirmations);
        contentValues.put(DatabaseHelper.REJECTIONS_COL, rejections);
        contentValues.put(DatabaseHelper.USER_COL, user);
        contentValues.put(DatabaseHelper.USER_ID_COL, userId);
        contentValues.put(DatabaseHelper.IMAGE_COL, imageBytes);

        long rowId = database.insert(DatabaseHelper.EVENTS_TABLE_NAME, null, contentValues);
        if (rowId != -1) {
            Log.d(TAG, "Event inserted successfully");
        }
    }

    public void deleteEventFromFB(int eventId) {
        String whereClause = DatabaseHelper._ID + " = ?";
        String[] whereArgs = {String.valueOf(eventId)};

        int rowsDeleted = database.delete(DatabaseHelper.EVENTS_TABLE_NAME, whereClause, whereArgs);
        if (rowsDeleted > 0) {
            Log.d(TAG, "Event deleted successfully");
        }
    }


}




