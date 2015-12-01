package com.cdapplications.dozeapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Colin on 2015-08-30.
 */

/*
SleepModeFragment is called when alarm is started.  It disables the rest of the application while an alarm is running
 */


public class SleepModeFragment extends Fragment{

    private static final String TAG = "SleepModeFragment";

    private DozeActivity mCurrentActivity;

    //TextView for clock
    private TextView mCurrentTime;

    //Formatter for clock
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm aa");

    //Key for restoring the alarm time after closed app
    private static final String ALARM_TIME_KEY = "com.cdapplications.doze.ALARM_TIME";

    //Fading TextView for go to sleep animation
    private TextView mSleepMessage;

    //Colors for go to sleep animation
    private int mSleepMessageColorStart;
    private int mSleepMessageColorEnd;

    //Timer to handle animation
    private Timer mTimer = new Timer();

    //Task to run after timer ends
    private TimerTask mTimerTask = new TimerTask() {
        Runnable fadeMessade = new Runnable() {
            @Override
            public void run() {
                //Animate the go to sleep TextView to turn from white to black in 2000ms
                ObjectAnimator messageAnimator = ObjectAnimator
                        .ofInt(mSleepMessage, "textColor", mSleepMessageColorStart, mSleepMessageColorEnd)
                        .setDuration(2000);
                messageAnimator.setEvaluator(new ArgbEvaluator());
                messageAnimator.start();
            }
        };
        @Override
        public void run() {

            //Runs the animation on UI thread
            mCurrentActivity.runOnUiThread(fadeMessade);

        }
    };

    public static SleepModeFragment newInstance(String alarmString){
        Bundle args = new Bundle();
        args.putString(ALARM_TIME_KEY, alarmString);
        SleepModeFragment smf = new SleepModeFragment();
        smf.setArguments(args);
        return smf;
    }

    //Public so that other Fragments can notify SleepModeFragment when to update it's time
    public void updateTime() {
        mCurrentTime.setText(mTimeFormat.format(new Date()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.sleep_mode_view, container, false);

        //Colors are set
        mSleepMessageColorStart = getResources().getColor(R.color.light_color);
        mSleepMessageColorEnd = getResources().getColor(R.color.go_to_sleep_color_fade);

        //TextViews are set
        mCurrentTime = (TextView) v.findViewById(R.id.current_time);
        mCurrentTime.setText(mTimeFormat.format(new Date()));
        mSleepMessage = (TextView) v.findViewById(R.id.go_to_sleep);
        TextView alarmTime = (TextView) v.findViewById(R.id.alarm_time);

        //Alarm time is set depending on what string was sent to this SleepModeFragment in newInstance()
        alarmTime.setText(getArguments().getString(ALARM_TIME_KEY));

        CardView wakeUpEarlyButton = (CardView) v.findViewById(R.id.wake_up_early_card);
        wakeUpEarlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Wake up early button pops up a SleepReviewDialogFragment through activity
                DozeActivity activity = (DozeActivity) getActivity();
                activity.reviewSleep();
            }
        });
        CardView cancelSleepButton = (CardView) v.findViewById(R.id.cancel_sleep_button);
        cancelSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cancel sleep button pops up an OnBackPressedDialogFragment through activity
                getActivity().onBackPressed();
            }
        });

        //Animation is scheduled
        mTimer.schedule(mTimerTask, 1000);
        return v;
    }

}
