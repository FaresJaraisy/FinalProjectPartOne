package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "COUNTRIES";
    public static final String EVENTS_TABLE_NAME = "EVENTS";

    // Events table columns
    public static final String ID_COL = "ID";
    public static final String TYPE_COL = "TYPE";
    public static final String IMAGE_COL = "IMAGE";
    public static final String DESCRIPTION_COL = "DESCRIPTION";
    public static final String LOCATION_COL = "LOCATION";
    public static final String DISTRICT_COL = "DISTRICT";
    public static final String SEVERITY_COL = "SEVERITY";
    public static final String USER_COL = "USER";
    public static final String DATE_COL = "DATE";
    public static final String CONFIRMATIONS_COL = "CONFIRMATIONS";
    public static final String REJECTIONS_COL = "REJECTIONS";
    public static final String USER_NAME_COL = "USER_NAME";
    public static final String USER_PASSWORD_COL = "USER_PASSWORD";
    public static final String USERS_TABLE = "USERS";
    public static final String COMMENTS_TABLE = "COMMENTS";
    public static final String CREATOR_COL = "CREATOR";
    public static final String CONTENT_COL = "CONTENT";


    public static final String EVENT_TO_USER_CONFIRMATION_TABLE = "EVENT_TO_USER_CONFIRMATION";
    public static final String USER_ID_COL = "USER_ID";
    public static final String EVENT_ID_COL = "EVENT_ID";
    private static final String CREATE_EVENTS_TABLE = "create table " + EVENTS_TABLE_NAME + "(" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TYPE_COL + " TEXT, " +
            IMAGE_COL + " BLOB, " +
            DESCRIPTION_COL + " TEXT, " +
            LOCATION_COL + " TEXT, " +
            DISTRICT_COL + " TEXT, " +
            SEVERITY_COL + " TEXT, " +
            DATE_COL + " TEXT, " +
            CONFIRMATIONS_COL + " INTEGER, " +
            REJECTIONS_COL + " INTEGER, " +
            USER_COL + " TEXT, " +
            USER_ID_COL + " INTEGER);";
    // Table columns
    public static final String _ID = "_id";
    public static final String SUBJECT = "subject";
    public static final String DESC = "description";
    public static final String DISTRICT_TABLE_NAME = "DISTRICTS";


    // Database Information
    static final String DB_NAME = "EVENTS.DB";

    // database version
    static final int DB_VERSION = 2;

    // Creating table query
    private static final String CREATE_TABLE_EVENT_TO_USER_CONF =
            "create table " + EVENT_TO_USER_CONFIRMATION_TABLE + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EVENT_ID_COL + " TEXT , " + USER_ID_COL + " TEXT, "
            + "UNIQUE(" + EVENT_ID_COL + ", " + USER_ID_COL + "));";

    private static final String CREATE_DISTRICT_TABLE = "create table " + DISTRICT_TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DESC + " TEXT);";

    private static final String CREATE_USERS_TABLE = "create table " + USERS_TABLE + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_NAME_COL + " TEXT, " +
            USER_PASSWORD_COL + " TEXT, " +
            CONFIRMATIONS_COL + " TEXT, " +
            REJECTIONS_COL + " TEXT" +
            ");";
    private static final String CREATE_COMMENTS_TABLE = "create table " + COMMENTS_TABLE + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CREATOR_COL + " TEXT, " +
            CONTENT_COL + " TEXT, " +
            EVENT_ID_COL + " INTEGER, " +
            "FOREIGN KEY (CREATOR) REFERENCES USERS(USER_NAME)," +
            "FOREIGN KEY (EVENT_ID) REFERENCES EVENTS(_id)" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates the initial tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(CREATE_DISTRICT_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_COMMENTS_TABLE);
        // insert initial users - new user registration will be implemented in part 2
        //db.execSQL("insert into " + USERS_TABLE +" (" +
        //            USER_NAME_COL + ", " +
        //            USER_PASSWORD_COL+ ", " +
        //            CONFIRMATIONS_COL+ ", " +
        //            REJECTIONS_COL +
        //            ") values ('user1', '1234', 0, 0), ('user2' , '1234', 0, 0), ('user3','1234', 0, 0)");
        db.execSQL(CREATE_TABLE_EVENT_TO_USER_CONF);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS USERS");
        //db.execSQL("DROP TABLE IF EXISTS " + DISTRICT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TO_USER_CONFIRMATION_TABLE);
        onCreate(db);
    }
}