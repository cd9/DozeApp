package com.cdapplications.dozeapp;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Colin on 2015-09-01.
 */
/*
RingFragment is started when the alarm is set off.
It allows the user to either snooze, which sets another alarm, or dismiss.
 */

public class RingFragment extends Fragment {

    //Time clock shown when alarm goes off
    private TextView mCurrentTime;

    //For formatting current time
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm aa");

    //Storing DozeActivity so we don't have to cast it every time
    private DozeActivity mCurrentActivity;

    //For custom ringtone and causing phone to vibrate
    private Ringtone r;
    private Vibrator vibrator;

    public static RingFragment newInstance(){
        return new RingFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();
        //onCreate, stop the AlarmService
        mCurrentActivity.stopService(new Intent(mCurrentActivity, AlarmService.class));
        DozeActivity.setAlarmRunning(mCurrentActivity, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.ring_view_v2, container, false);

        //Getting URI of alarm ring tone
        Uri alarmRing = Uri.parse(mCurrentActivity.getToneURI());
        r = RingtoneManager.getRingtone(mCurrentActivity, alarmRing);

        //Set up vibration pattern
        vibrator = (Vibrator) mCurrentActivity.getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibrationpattern = {0, 500, 1000};

        //When Ring Fragment starts, turn on the screen, bypass the lockscreen (for Doze only), and keep the screen on
        mCurrentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Depending on the setting the user has chosen, a ringtone, a vibration pattern, or both with start.
        switch(mCurrentActivity.getRingModeSelection()){
            case 0:
                r.play();
                break;
            case 1:
                vibrator.vibrate(vibrationpattern, 0);
                break;
            case 2:
                r.play();
                vibrator.vibrate(vibrationpattern, 0);


        }

        mCurrentTime = (TextView) v.findViewById(R.id.current_time);
        //Set the clock to display the current time
        updateTime();


        CardView mSnoozeCard = (CardView) v.findViewById(R.id.snooze_card);
        CardView mDismissCard = (CardView) v.findViewById(R.id.dismiss_card);

        //When the dismiss button is pressed, start a SleepReviewDialogFragment
        mDismissCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentActivity.reviewSleep();
            }
        });

        //When the snooze button is pressed, start another alarm for the current time + snooze duration
        mSnoozeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentActivity.onBackPressed();

                //Get user defined snooze duration
                int snoozeMinutes = mCurrentActivity.getSnoozeMinutes();

                //Make a calendar for the current time + snooze minutes
                Calendar snoozeCalendar = Calendar.getInstance();
                snoozeCalendar.add(Calendar.MINUTE, snoozeMinutes);

                //Start the AlarmService again for a new alarm time
                Intent i = AlarmService.newIntent(mCurrentActivity, snoozeCalendar);
                mCurrentActivity.startService(i);
                mCurrentActivity.startSleepMode(snoozeCalendar);
            }
        });
        return v;
    }


    //When RingFragment is closed, the ringtone and vibration pattern will stop playing
    @Override
    public void onDestroy(){
        super.onDestroy();
        r.stop();
        vibrator.cancel();
    }

    //This method is public because WakeViewFragment will call it to update the clock.
    //This avoids having to make another BroadcastReceiver for RingFragment
    public void updateTime(){
        mCurrentTime.setText(mTimeFormat.format(new Date()));
    }
}
