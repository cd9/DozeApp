package com.cdapplications.dozeapp;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.util.Calendar;

/**
 * Created by Colin on 2015-09-01.
 */

/*
AlarmService is used to manage our alarm.  It runs even when Doze is closed, waiting for the correct alarm time.
 */
public class AlarmService extends Service {
    private static final String TAG = "AlarmService";

    //Store the alarm set for when opening app later
    private static final String CALENDAR_KEY = "com.cdapplications.dozeapp.CALENDAR_KEY";

    //Notify when to start RingFragment
    public static final String RING_ACTION = "com.cdapplications.dozeapp.RING_ACTION";

    //Notification action to notify DozeActivity if application was started from notification
    public static final String NOTIFICATION_CLICK_ACTION = "com.cdapplications.dozeapp.NOTIFICATION_CLICK_ACTION";
    Calendar snoozeCalendar;

    public static Intent newIntent(Context context, Calendar alarmTimeCalendar){
        Intent i = new Intent(context, AlarmService.class);
        i.putExtra(CALENDAR_KEY, alarmTimeCalendar);
        return i;
    }


    //Called on creation of service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //Get alarm time value of fragment that started this service
        snoozeCalendar = (Calendar) intent.getExtras().get(CALENDAR_KEY);
        Log.i("TAG", snoozeCalendar.getTime().toString());


        //Create intent to open Doze
        //Store intent in a PendingIntent.  This will be assigned to a notification later
        Resources resources = getResources();
        Intent in = new Intent(this, DozeActivity.class);
        in.setAction(NOTIFICATION_CLICK_ACTION);
        PendingIntent pi = PendingIntent.getActivity(this, 0, in, 0 );

        //Activate the alarm
        setAlarm(true);

        //Create a notification
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.sleep_mode_ticker)) //Message when notification starts
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) //Notification icon
                .setContentTitle(resources.getString(R.string.sleep_mode_title)) //Notification title
                .setContentText(resources.getString(R.string.sleep_mode_text)) //Notification text
                .setContentIntent(pi) //Assign the PendingIntent to the notification
                .setAutoCancel(false) //Set the notification as non-dissmissible
                .build();

        //Display the notification while service is running.
        startForeground(1, notification);

        //Keep the service running indefinitely
        return IntentService.START_STICKY;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        //Cancel the alarm when the service is destroyed
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(123);
        setAlarm(false);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Binding provides an interface for components to interact with a service.  Don't need this
        //Return null because we're not allowing binding
        return null;
    }

    //Set the alarm on or cancel the alarm
    public void setAlarm(Boolean setOn){


        //Create an intent that sends the action RING_ACTION to signal the alarm has been activated
        Intent i = new Intent(this, DozeActivity.class);
        i.setAction(RING_ACTION);

        //Store the intent in a PendingIntent.  This bundles up the intent in a one-time use package.
        PendingIntent ringPendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

        //Get the system's alarm manager
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (setOn){
            //If the alarm is being set, set the alarm with our alarm manager
            Log.i(TAG, "ALARM SET FOR "+snoozeCalendar.getTime().toString());
            //RTC_WAKEUP causes the screen to turn on when the alarm goes off
           alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeCalendar.getTimeInMillis(), ringPendingIntent);
            //Store boolean value that alarm is running
            DozeActivity.setAlarmRunning(this, true);
            
        }else{
            //Cancel the alarm
            alarmManager.cancel(ringPendingIntent);
        }

    }
}
