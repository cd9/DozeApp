package com.cdapplications.dozeapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Colin on 2015-09-15.
 */

/*
CustomAlarmPickerDialog allows the user to set a specific alarm besides the presets.
It bypasses the enter sleep mode button and goes straight to sleep mode
 */

public class CustomAlarmPickerDialogFragment extends DialogFragment {
    public static CustomAlarmPickerDialogFragment newInstance(){
        return new CustomAlarmPickerDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.custom_alarm_picker, container, false);
        final DozeActivity mCurrentActivity = (DozeActivity) getActivity();
        final TimePicker timePicker = (TimePicker) v.findViewById(R.id.time_picker);  //TimePicker for a custom alarm time
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //No title needed for this
        dialog.setCanceledOnTouchOutside(false); //Only allow user to back out with virtual button
        dialog.setCancelable(false); //Disable the default back button method call to set our own code to run when back button is pressed
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //When back button is pressed, run DozeActivity's onBackPressed
                    mCurrentActivity.onBackPressed();
                    return true;
                }
                return false;
            }
        });


        //set the custom alarm
        Button sleepModeButton = (Button) v.findViewById(R.id.dialog_enter_sleep_mode);
        sleepModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back through DozeActivity
                mCurrentActivity.onBackPressed();
                Calendar c = Calendar.getInstance();
                //Get date and time through our time picker
                c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                c.set(Calendar.MINUTE, timePicker.getCurrentMinute());

                //Setting hour/minute values less than the current hour/minute will cause the Calendar to be set to those hour/minute values in the current day.
                //This will set the calendar in the past, so the alarm will ring instantaneously.
                //To fix this, we check if the Calendar time is in the past and if it is, we add a full day.
                if (c.getTimeInMillis()<Calendar.getInstance().getTimeInMillis()) c.add(Calendar.DAY_OF_YEAR, 1);

                //Start the sleep mode with our custom alarm
                mCurrentActivity.startSleepMode(c);
            }
        });
        return v;
    }
}
