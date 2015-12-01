package com.cdapplications.dozeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
    DozeActivity is the foundation of Doze.  Everything else runs on top of DozeActivity.
    It houses common methods that may be used by any of our many fragments and dialog fragments.
 */

public class DozeActivity extends FragmentActivity {

    private static final String TAG = "DozeActivity";


    //These tags are for identifying which fragments are running.
    //It also allows us to keep a reference of the fragments that we start.
    private static final String DIALOG_FRAGMENT_TAG = "dialogFragmentTag";
    public static final String RING_FRAGMENT_TAG = "ringFragment";
    public static final String DOZE_HOME_FRAG_TAG = "dozeHomeFragTag";

    //Used to go back twice when 2 fragments are present
    private boolean shouldGoBack = false;

    //Allows us to reverse the starting of our ring screen.
    public static final String RING_SCREEN_BACK_STACK= "ringScreenBackStack";

    //Used to format the time for the main clock and the alarm times
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm aa");
    public  final SimpleDateFormat mDateRangeFormatter = new SimpleDateFormat("MM/dd/yy");
    public  SimpleDateFormat mDataDateFormatter = new SimpleDateFormat("MM/dd");

    //These are keys for storing values in the phone's local storage.
    private static final String PREF_ALARM_RUNNING="alarmRunning";
    private static final String PREF_SNOOZE_MINUTES = "prefSnoozeMinutes";
    private static final String PREF_FALL_ASLEEP_MINUTES = "prefFallAsleepMinutes";
    private static final String PREF_ALARM_TONE = "alarmToneKey";
    private static final String PREF_RING_MODE_SELECTION = "ringModeSelection";
    private static final String PREF_IS_FIRST_DAY = "isFirstDay";
    private static final String PREF_SLEEP_START = "prefSleepStart";
    private static final String PREF_RUNNING_ALARM_TIME = "prefRunningAlarmTime";

    //Used to go back twice when 2 fragments are present
    public void goBackHome(){
        shouldGoBack = true;
        onBackPressed();
    }

    //Starts sleep mode.  Putting this in DozeActivity allows us to call it from any fragment on top of DozeActivity
    public void startSleepMode(Calendar whenToRing){

        //Store the moment the alarm is set
        Calendar startCalendar = Calendar.getInstance();
        setSleepStart(startCalendar.getTimeInMillis());

        //Start sleep mode fragment and let it know the alarm time
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        //Store the alarm time as a string.  This string displays on start of sleep mode.
        //Storing in SharedPreferences because it needs to be recovered when app is closed and opened but still in sleep mode
        setRunningAlarmTime(mTimeFormat.format(whenToRing.getTimeInMillis()));
        SleepModeFragment sleepFragment = SleepModeFragment.newInstance(getRunningAlarmTime());

        //Start the service and SleepModeFragment
        ft.add(android.R.id.content, sleepFragment, WakeViewFragment.SLEEP_MODE_TAG);
        ft.addToBackStack(WakeViewFragment.SLEEP_MODE_BACK_STACK_NAME);
        Intent i = AlarmService.newIntent(this, whenToRing);
        this.startService(i);
        ft.commit();
    }


    //Starting point of entire app
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        Log.i(TAG, getIntent().getAction());


        //Start DozeHomeFragment
        DozeHomeFragment homeFragment = DozeHomeFragment.newInstance();
        fm.beginTransaction().add(android.R.id.content, homeFragment, DOZE_HOME_FRAG_TAG).commit();



        //Check if application was started from alarm going off or from notification press.
        if (getIntent().getAction()!=null){
            if (getIntent().getAction().equals(AlarmService.RING_ACTION)){
                //If started from a ring action, start RingFragment

                FragmentTransaction ft = fm.beginTransaction();
                RingFragment ringFragment = RingFragment.newInstance();
                ft.replace(android.R.id.content, ringFragment, RING_FRAGMENT_TAG);
                ft.addToBackStack(RING_SCREEN_BACK_STACK);
                ft.commit();
            }else if ((getIntent().getAction().equals(AlarmService.NOTIFICATION_CLICK_ACTION)&&getAlarmRunning())||
                    PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ALARM_RUNNING, false)){
                //If started from notification, start SleepModeFragment
                FragmentTransaction ft = fm.beginTransaction();
                SleepModeFragment sleepModeFragment = SleepModeFragment.newInstance(getRunningAlarmTime());
                ft.replace(android.R.id.content, sleepModeFragment, WakeViewFragment.SLEEP_MODE_TAG);
                ft.addToBackStack(WakeViewFragment.SLEEP_MODE_BACK_STACK_NAME);
                ft.commit();
            }
        }
    }

    //onBackPressed() is accessed from multiple fragments within Doze.
    //This method closes fragments differently depending on which ones are open.
    @Override
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();

        //Used to go back twice
        if (shouldGoBack){
            shouldGoBack = false;
            super.onBackPressed();

        }
        //If SleepModeFragment is active, add OnBackPressedDialogFragment instead of closing the fragment
        else if (fm.getBackStackEntryCount()==1&&fm.getBackStackEntryAt(0).getName().equals(WakeViewFragment.SLEEP_MODE_BACK_STACK_NAME)){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            OnBackPressedDialogFragment dialogFragment = OnBackPressedDialogFragment.newInstance();
            ft.add(dialogFragment, DIALOG_FRAGMENT_TAG);
            ft.commit();
        }
        //No special case.  Default.
        else{
            super.onBackPressed();
        }
    }

    //Activates SleepReviewDialogFragment.
    //This can be called from any fragment over DozeActivity
    public void reviewSleep(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SleepReviewDialogFragment dialogFragment = SleepReviewDialogFragment.newInstance(getSleepStart());
        ft.add(dialogFragment, DIALOG_FRAGMENT_TAG);
        ft.commit();
    }

    /*
    Variables that are stored in the phone's local storage are accessed through these methods.
    These methods read and write values from Doze's SharedPreferences
     */


    //Read and write path of sound file to play when alarm rings
    public void setToneURI(String toneURI){
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PREF_ALARM_TONE, toneURI)
                .commit();
    }
    public String getToneURI(){
        return PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_ALARM_TONE, null);
    }




    //Check if alarm is running
    public boolean getAlarmRunning(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_ALARM_RUNNING, false);
    }
    //Stores if the alarm is running.  Making this static allows it to be called from AlarmService, which is independent from DozeActivity
    public static void setAlarmRunning(Context context, boolean isAlarmSet){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_ALARM_RUNNING, isAlarmSet)
                .apply();
    }

    //Read and write the alarm time currently set
    public void setRunningAlarmTime(String runningAlarmTimeInMillis){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PREF_RUNNING_ALARM_TIME, runningAlarmTimeInMillis)
                .apply();
    }
    public String getRunningAlarmTime(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_RUNNING_ALARM_TIME, null);
    }

    //Read and write the moment the alarm was started
    public void setSleepStart(long sleepStart){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(PREF_SLEEP_START, sleepStart)
                .apply();
    }

    public long getSleepStart(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(PREF_SLEEP_START, 0);
    }


    //Read and write the day the user first opened Doze
    public void setIsFirstDay(boolean isFirstDay){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(PREF_IS_FIRST_DAY, isFirstDay)
                .apply();
    }

    public boolean getIsFirstDay(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_IS_FIRST_DAY, true);
    }


    //Read and write snooze duration
    public void setSnoozeMinutes(int minutes){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(PREF_SNOOZE_MINUTES, minutes)
                .apply();
    }

    public int getSnoozeMinutes(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(PREF_SNOOZE_MINUTES, -1);
    }


    //Read and write time it takes for user to fall asleep
    public void setFallAsleepMinutes(int minutes){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(PREF_FALL_ASLEEP_MINUTES, minutes)
                .apply();
    }

    public int getFallAsleepMinutes(){
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(PREF_FALL_ASLEEP_MINUTES, -1);
    }


    //Read and write whether to ring sound, vibrate, or both when alarm goes off
    public void setRingModeSelection(int selection){
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(PREF_RING_MODE_SELECTION, selection)
                .apply();
    }

    public int getRingModeSelection(){
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(PREF_RING_MODE_SELECTION, 0);
    }




}
