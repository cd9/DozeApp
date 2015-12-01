package com.cdapplications.dozeapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Colin on 2015-08-25.
 */

/*
Object for every alarm time on WakeViewFragment.
todo get rid of WakeTime object and replace it with a list of Calendars.
*/

public class WakeTime {


    //Holds alarm time and if it's active
    private Calendar alarmTime;
    private boolean isActive;

    //constructor for current time
    public WakeTime(){
        alarmTime = Calendar.getInstance();
        isActive = false;
    }

    //Returns alarm time as string
    public String getAlarmTime() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("h:mm a");
        return simpleDate.format(alarmTime.getTime());
    }


    //Getters, setters

    public Calendar getAlarmCalendarTime(){
        return alarmTime;
    }

    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isWakeTimeActive) {
        isActive = isWakeTimeActive;
    }



}
