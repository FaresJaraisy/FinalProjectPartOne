package database;

import static android.content.ContentValues.TAG;
import static database.DatabaseHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import com.example.finalprojectpartone.FilterSettings;
import com.example.finalprojectpartone.R;
import com.example.finalprojectpartone.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import data.Event;

public class DBManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;
    public static final String INC = "1";
    public DBManager(Context c) {
        context = c;
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
    }


    // Save new Event to DB and add current date time to the event row
    public void insertEvent(Event event) {
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
        contentValue.put(REJECTIONS_COL, 0);
        database.insert(DatabaseHelper.EVENTS_TABLE_NAME, null, contentValue);
    }

    // This method confirms the event
    // A check is made if it was already approved by this user, if so early exit without changes
    public void confirmEvent(String id, int userid) {
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
        String insSql = "INSERT OR IGNORE INTO " + DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE +
                "(" + DatabaseHelper.USER_ID_COL + ", " + DatabaseHelper.EVENT_ID_COL +") VALUES(" +
                String.valueOf(userid) + ", " + id + " )";
        database.execSQL(insSql);
        database.execSQL("UPDATE " + DatabaseHelper.USERS_TABLE +
                " SET " + DatabaseHelper.CONFIRMATIONS_COL +
                " = " + DatabaseHelper.CONFIRMATIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + userid);
    }

    // This method rejects the event (as a separate value of the confirmations)
    // A check is made if it was already confirmed by this user, if so early exit without changes
    public void rejectEvent(String id, int userId) {
        String[] bindingArgs = new String[]{ INC, DatabaseHelper._ID };
        database.execSQL("UPDATE " + DatabaseHelper.EVENTS_TABLE_NAME +
                " SET " + REJECTIONS_COL +
                " = " + REJECTIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + id);

        database.execSQL("UPDATE " + DatabaseHelper.USERS_TABLE +
                " SET " + REJECTIONS_COL +
                " = " + REJECTIONS_COL + " + " + INC +
                " WHERE " + DatabaseHelper._ID + " = " + userId);
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
            R.id.buttonReject
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
            R.id.buttonDeleteEvent
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

    public int getPointsOfCreatedEventsByUser(int id)
    {
        int userConfirmations = getUserConfirmations(id);
        int userRejections = getUserRejections(id);
        int totalPoints = (userConfirmations + userRejections) * 3;
        return totalPoints;
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

    // delete event by event id and remove conformation from EVENT_TO_USER_CONFIRMATION_TABLE
    public void deleteEvent(String id, int userId) {
        String[] whereArgs = {id};
        String[] whereConfArgs = {String.valueOf(userId),id};
        database.delete(DatabaseHelper.EVENTS_TABLE_NAME, DatabaseHelper._ID + "=?" , whereArgs);
        // DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE
        database.delete(DatabaseHelper.EVENT_TO_USER_CONFIRMATION_TABLE,
                DatabaseHelper.USER_ID_COL + "=? AND " + DatabaseHelper.EVENT_ID_COL + "=?",
                whereConfArgs);
    }

    // get event by id
    public Event getEvent(String eventId) {
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
                REJECTIONS_COL,
                DatabaseHelper.USER_COL,
                DatabaseHelper.USER_ID_COL
        };

        String[] selectionArgs = {eventId};
        Cursor cursor = database.query(DatabaseHelper.EVENTS_TABLE_NAME, eventCols, DatabaseHelper._ID + "=?", selectionArgs, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            byteArray = cursor.getBlob(2);
            if (byteArray != null) bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            ret = new Event(
                    cursor.getString(1), bitmap,
                    cursor.getString(3),cursor.getString(4),cursor.getString(5),
                    cursor.getString(6), new UserProfile(Integer.parseInt(cursor.getString(11)),cursor.getString(10))
            );
        }
        //Event(eventType, Bitmap imgBitmap, description, location, district, severity, UserProfile user)
        cursor.close();
        return ret;
    }

    // update event by an event object
    public void updateEvent(Event event) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.TYPE_COL,event.getEventType());
        contentValues.put(DatabaseHelper.IMAGE_COL,event.getBitmapAsByteArray());
        contentValues.put(DatabaseHelper.DESCRIPTION_COL,event.getDescription());
        contentValues.put(DatabaseHelper.LOCATION_COL,event.getLocation());
        contentValues.put(DatabaseHelper.DISTRICT_COL,event.getDistrict());
        contentValues.put(DatabaseHelper.SEVERITY_COL,event.getSeverity());

        String[] whereArgs = {String.valueOf(event.getId())};
        int i = database.update(DatabaseHelper.EVENTS_TABLE_NAME, contentValues, DatabaseHelper._ID + " =?", whereArgs);
    }
}
