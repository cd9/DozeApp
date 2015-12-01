package com.cdapplications.dozeapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.CalendarContract;

import com.cdapplications.dozeapp.SleepRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Colin on 2015-09-19.
 */

/*

SleepRecordDbHelper provides easy access to the database

 */

public class SleepRecordDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 3;
    private static final String DATABASE_NAME = "sleepRecordBase.db";
    private static final String SLEEP_RECORD_TABLE = "sleeprecords";

    //Keys for database
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_MILLIS = "millis";
    private static final String KEY_RATING = "rating";
    private static final String KEY_COMMENT = "comment";


    public SleepRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //creates tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+SLEEP_RECORD_TABLE+"("+KEY_ID+" TEXT PRIMARY KEY,"
                +KEY_DATE+" INTEGER,"+KEY_MILLIS+" REAL,"+KEY_RATING+" STRING,"+KEY_COMMENT+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop table if exists
        db.execSQL("DROP TABLE IF EXISTS " + SLEEP_RECORD_TABLE);
        //new table
        onCreate(db);
    }

    //Adds a sleep record to the database
    //by separating the record into longs and strings.
    public void addSleepRecord(SleepRecord record){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, record.getId());
        values.put(KEY_DATE, record.getDate().getTimeInMillis());
        values.put(KEY_MILLIS, record.getMillisOfSleep());
        values.put(KEY_RATING, record.getSleepRating());
        values.put(KEY_COMMENT, record.getComment());
        db.insert(SLEEP_RECORD_TABLE, null, values);
        db.close();
    }


    //Finds a sleep record given a date.
    public SleepRecord getSleepRecordByDate(long date){
        SQLiteDatabase db = this.getReadableDatabase();

        //Set cursor to select all records
        Cursor cursor = db.rawQuery("SELECT * FROM " + SLEEP_RECORD_TABLE, null);

        //Init the calendar and set the date
        Calendar dateToTestCalendar = Calendar.getInstance();
        dateToTestCalendar.setTimeInMillis(date);


        //Temporary calendar to store values of compared sleep records
        Calendar queryCalendar = Calendar.getInstance();
        if (cursor.moveToFirst()){
            do{
                SleepRecord tempRecord = new SleepRecord(cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getDouble(3), cursor.getString(4));
                queryCalendar.setTimeInMillis(tempRecord.getDate().getTimeInMillis());

                //Comparing to see if the days and years match.  If they do, return the sleep record
                if (queryCalendar.get(Calendar.DAY_OF_YEAR)==dateToTestCalendar.get(Calendar.DAY_OF_YEAR)
                        &&queryCalendar.get(Calendar.YEAR)==dateToTestCalendar.get(Calendar.YEAR)) return tempRecord;


            }while(cursor.moveToNext());
        }

        //If no sleep record is found with the given date, return null
        return null;
    }

    //Finds a sleep record with a matching ID then replaces it with the given sleep record.
    public int updateSleepRecord(SleepRecord record){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, record.getId());
        values.put(KEY_DATE, record.getDate().getTimeInMillis());
        values.put(KEY_MILLIS, record.getMillisOfSleep());
        values.put(KEY_RATING, record.getSleepRating());
        values.put(KEY_COMMENT, record.getComment());

        return db.update(SLEEP_RECORD_TABLE, values, KEY_ID+" = ?", new String[]{record.getId()} );
    }


    //Builds a list of sleep records based on database entries then returns it
    public List<SleepRecord> getAllSleepRecords(){
        List<SleepRecord> sleepRecords = new ArrayList<>();

        //Select all from database
        String selectQuery = "SELECT * FROM "+SLEEP_RECORD_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                //Adding a new sleep record to the list that will be returned
                sleepRecords.add(new SleepRecord(cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getDouble(3), cursor.getString(4)));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return sleepRecords;
    }

    //Returns the size of the database
    public int getDbSize(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SLEEP_RECORD_TABLE, null);
        int counter = 0;
        //Counts up until there are no more sleep records
        if (cursor.moveToFirst()){
            do{
                counter++;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return counter;
    }

}
