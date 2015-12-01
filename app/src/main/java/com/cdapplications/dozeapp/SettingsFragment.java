package com.cdapplications.dozeapp;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Colin on 2015-09-03.
 */

/*
SettingsFragment gives the user access to a variety of different settings.
Most of these settings start a dialog that changes a value stored in the phone's local storage

 */

public class SettingsFragment extends Fragment {

    private String TAG = "SettingsFragment";

    //These tags are used to reference the DialogFragments after they have been started
    public static final String SNOOZE_DIALOG_TAG = "durationDialogTag";
    public static final String FALL_ASLEEP_DIALOG_TAG = "durationDialogTag";
    public static final String RING_MODE_DIALOG_TAG = "ringModeTag";

    //These backstack entry names allow the reversal of the starting of a DialogFragment.
    private static final String DURATION_DIALOG_BACK_STACK_NAME = "durationDialogBackStackName";
    private static final String RING_MODE_DIALOG_BACK_STACK_NAME = "ringModeDialogBackStackName";

    //These ints are used to allow communication between SettingsFragment and the DialogFragments
    private final int SNOOZE_DIALOG_REQUEST_CODE = 0;
    private final int FALL_ASLEEP_DIALOG_REQUEST_CODE = 1;
    private final int RING_MODE_DIALOG_REQUEST_CODE = 2;
    public static final int ALARM_TONE_REQUEST_CODE = 3;

    //Setting TextViews
    private TextView mSnoozeIndicator;
    private TextView mFallAsleepIndicator;
    private TextView mToneIndicator;
    private TextView mRingModeIndicator;

    //Store DozeActivity so don't need to cast every time
    DozeActivity mCurrentActivity;

    public static SettingsFragment newInstance(){
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.settings_view_page, container, false);

        //Set onClickListener for snooze setting
        RelativeLayout mSnoozeSetting = (RelativeLayout) v.findViewById(R.id.snooze_setting);
        mSnoozeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = mCurrentActivity.getSupportFragmentManager();
                //Make new DurationDialogFragment for snooze setting
                DurationDialogFragment fragment = DurationDialogFragment.newInstance(getResources().getString(R.string.snooze_dialog_title), DurationDialogFragment.SNOOZE_FRAG_IDENTIFIER);
                //Establish a connection between SettingsFragment and the DurationDialogFragment
                fragment.setTargetFragment(SettingsFragment.this, SNOOZE_DIALOG_REQUEST_CODE);
                //Start the DurationDialogFragment
                fm.beginTransaction()
                        .add(fragment, SNOOZE_DIALOG_TAG)
                        .addToBackStack(DURATION_DIALOG_BACK_STACK_NAME)
                        .commit();
            }
        });

        //Set onClickListener for fall asleep setting
        RelativeLayout mSleepSetting = (RelativeLayout) v.findViewById(R.id.sleep_setting);
        mSleepSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = mCurrentActivity.getSupportFragmentManager();
                //Make new DurationDialogFragment for fall asleep setting
                DurationDialogFragment fragment = DurationDialogFragment.newInstance(getResources().getString(R.string.fall_asleep_title), DurationDialogFragment.FALL_ASLEEP_FRAG_IDENTIFIER);
                //Establish a connection between SettingsFragment and the DurationDialogFragment
                fragment.setTargetFragment(SettingsFragment.this, FALL_ASLEEP_DIALOG_REQUEST_CODE);
                //Start the DurationDialogFragment
                fm.beginTransaction()
                        .add(fragment, FALL_ASLEEP_DIALOG_TAG)
                        .addToBackStack(DURATION_DIALOG_BACK_STACK_NAME)
                        .commit();

            }
        });

        //Set onClickListener for alarm tone setting
        RelativeLayout mToneSetting = (RelativeLayout) v.findViewById(R.id.tone_setting);
        mToneSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Make a new intent that starts the system's RingtoneManager
                Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getResources().getString(R.string.alarm_tone_title));
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);

                //Current selected ring tone is saved to set as initial selection
                String ToneURIString = mCurrentActivity.getToneURI();
                if (ToneURIString!=null){
                    i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(mCurrentActivity.getToneURI()));
                }

                //Start the RingtoneManager's picker
                startActivityForResult(i, ALARM_TONE_REQUEST_CODE);

            }
        });

        //Set onClickListener for ring mode setting
        RelativeLayout mRingModeSetting = (RelativeLayout) v.findViewById(R.id.ring_mode_setting);
        mRingModeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = mCurrentActivity.getSupportFragmentManager();
                //Make new RingModeDialogFragment
                RingModeDialogFragment fragment = RingModeDialogFragment.newInstance();
                //Establish connection
                fragment.setTargetFragment(SettingsFragment.this, RING_MODE_DIALOG_REQUEST_CODE);
                //Start RingModeDialogFragment
                fm.beginTransaction()
                        .add(fragment, RING_MODE_DIALOG_TAG)
                        .addToBackStack(RING_MODE_DIALOG_BACK_STACK_NAME)
                        .commit();
            }
        });
        mSnoozeIndicator = (TextView) v.findViewById(R.id.snooze_indicator);
        mFallAsleepIndicator = (TextView) v.findViewById(R.id.fall_asleep_indicator);
        mToneIndicator = (TextView) v.findViewById(R.id.tone_indicator);
        mToneIndicator.setText(getToneTitle(mCurrentActivity.getToneURI()));
        mRingModeIndicator = (TextView) v.findViewById(R.id.ring_mode_indicator);


        //Getting the stored values for the settings to display.
        //If the values = -1, they haven't been set, so set them to a default value
        int minutes = mCurrentActivity.getSnoozeMinutes();
        if (minutes==-1){
            mCurrentActivity.setSnoozeMinutes(5);
        }
        minutes = mCurrentActivity.getFallAsleepMinutes();
        if (minutes==-1){
            mCurrentActivity.setFallAsleepMinutes(5);
        }
        //Sending -1 refreshes all the settings
        refreshIndicators(-1);
        return v;
    }


    //Called when a DialogFragment is closed
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode==Activity.RESULT_OK){
            switch(requestCode){
                case ALARM_TONE_REQUEST_CODE:
                    //If alarm tone dialog was just closed, set the user selection in the phone's local storage
                    if (intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)!=null){
                        String toneURIString = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString();
                        mCurrentActivity.setToneURI(toneURIString);
                        mToneIndicator.setText(getToneTitle(toneURIString));
                    }
            }
        }
    }


    //Returns the title of the selected tone using the tone's URI.
    //If the URI is null, then set a default tone title
    private String getToneTitle(String toneUri){
        String title;
        if (toneUri!=null){
            //set tone title
            title = RingtoneManager.getRingtone(mCurrentActivity, Uri.parse(toneUri)).getTitle(mCurrentActivity);
        }else{
            //set default tone title
            title =  RingtoneManager.getRingtone(mCurrentActivity, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)).getTitle(mCurrentActivity);
        }

        //If the title is too long, shorten it and add ...
        if (title.length()>=10){
            title = title.substring(0,10);
            title +="...";
        }
        return title;
    }

    //Refreshes indicators based on resultCode sent by dialog.
    public void refreshIndicators(int resultCode) {
        int minutes;
        boolean refreshAll = false;
        switch (resultCode) {
            //When -1 is the result code, all indicators are refreshed
            case -1:
                refreshAll = true;
            case DurationDialogFragment.SNOOZE_RESULT_CODE:
                minutes = mCurrentActivity.getSnoozeMinutes();
                mSnoozeIndicator.setText(minutes + "m");
                if (!refreshAll) return;
            case DurationDialogFragment.FALL_ASLEEP_RESULT_CODE:
                minutes = mCurrentActivity.getFallAsleepMinutes();
                mFallAsleepIndicator.setText(minutes + "m");

                //If fall asleep time was changed, we need to update WakeViewFragment
                ((DozeHomeFragment)mCurrentActivity.getSupportFragmentManager().findFragmentByTag(DozeActivity.DOZE_HOME_FRAG_TAG)).updateWakeViewFragment();
                if (!refreshAll) return;
            case RingModeDialogFragment.RING_MODE_RESULT_CODE:
                mRingModeIndicator.setText(getRingModeName());
        }
    }


    //Returns the name of the ring mode selection
    //Used to preview the ring mode
    public String getRingModeName(){
        switch(mCurrentActivity.getRingModeSelection()){
            case 0: return "Ring";
            case 1: return "Vibrate";
            case 2: return "Ring+Vibrate";
            default: return null;
        }
    }
}
