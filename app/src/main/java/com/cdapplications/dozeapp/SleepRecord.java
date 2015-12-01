package com.cdapplications.dozeapp;

import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Colin on 2015-09-17.
 */

/*
SleepRecord is an object that holds information about a record of sleep such as date, length and (for future additions) rating
 */


public class SleepRecord {

    private long mMillisOfSleep;
    private double mSleepRating;
    private String mComment;
    private Calendar mCalendar;
    private UUID mId;

    //Constructor to make a new SleepRecord
    public SleepRecord(String uuid, long timeInMillis, long millisOfSleep, double sleepRating, String comment){
        mMillisOfSleep = millisOfSleep;
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(timeInMillis);
        mSleepRating = sleepRating;
        if (comment!=null){
            mComment = comment;
        }else mComment = "";

        if (uuid!=null){
            mId = UUID.fromString(uuid);
            return;
        }
        mId = UUID.randomUUID();

    }

    //Returns day of week as an abbreviated string
    public String getDayOfWeekAsString(){
        switch(mCalendar.get(Calendar.DAY_OF_WEEK)){
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
            case 1: return "Sun";
        }
        return "Err";
    }

    //Allows addition of sleep hours
    //Useful for combining multiple sleep sessions in one day
    public void addHoursOfSleep(long millisOfSleep){
        mMillisOfSleep += millisOfSleep;
    }

    //Getters & setters

    public String getId(){
        return mId.toString();
    }

    public double getSleepRating() {
        return mSleepRating;
    }

    public Calendar getDate() {
        return mCalendar;
    }

    public double getMillisOfSleep(){
        return mMillisOfSleep;
    }

    public String getComment(){
        return mComment;
    }

    public void setComment(String comment){
        mComment = comment;
    }

    public void setSleepRating(double rating){
        mSleepRating = rating;
    }


}
