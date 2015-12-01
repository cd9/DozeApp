package com.cdapplications.dozeapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cdapplications.dozeapp.database.SleepRecordDbHelper;

import java.util.Calendar;

/**
 * Created by Colin on 2015-09-02.
 */

/*

SleepReviewDialogFragment is a DialogFragment that confirms the user's intent to confirm entry of a sleep record.

 */
public class SleepReviewDialogFragment extends DialogFragment {
    private final String TAG = "SlpReviewDialogFragment";

    //Used to read the moment the alarm was started
    private static final String START_TIME_KEY = "startTimeKey";

    //Stored so don't need to cast every time
    private DozeActivity mCurrentActivity;

    // A start time is set when starting a new SleepReviewDialogFragment
    public static SleepReviewDialogFragment newInstance(long startTime){
        Bundle args = new Bundle();
        args.putLong(START_TIME_KEY, startTime);
        SleepReviewDialogFragment fragment = new SleepReviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.sleep_re_view, container, false);
        Dialog dialog = getDialog();

        //Set title and disable dismissing from touching outside dialog
        dialog.setTitle(R.string.sleep_review_title);
        dialog.setCanceledOnTouchOutside(false);

        final RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating_bar);
        final EditText commentBox = (EditText) v.findViewById(R.id.comment_box);

        //Cancel button simply closes the dialog
        Button cancelButton = (Button) v.findViewById(R.id.cancel_review);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        //Submit button adds hours to the present day's sleep record, stops the alarm, and updates the DataView
        Button submitButton = (Button) v.findViewById(R.id.submit_review);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepRecordDbHelper dbHelper = new SleepRecordDbHelper(mCurrentActivity);

                //Calculate the number of hours to add to the Sleep Record
                Calendar endCalendar = Calendar.getInstance();
                long endTime = endCalendar.getTimeInMillis();
                long startTime = getArguments().getLong(START_TIME_KEY);

                //Get the comment from the EditText
                String comment = commentBox.getText().toString();

                //Get the rating from the RatingBar
                double rating = ratingBar.getRating();

                //Get the present day's Sleep Record
                SleepRecord record = dbHelper.getSleepRecordByDate(Calendar.getInstance().getTimeInMillis());

                //Add and update the present day's Sleep Record
                record.addHoursOfSleep(endTime - startTime);
                record.setComment(comment);
                record.setSleepRating(rating);
                dbHelper.updateSleepRecord(record);

                //Stop the alarm
                DozeActivity.setAlarmRunning(mCurrentActivity, false);
                mCurrentActivity.stopService(new Intent(mCurrentActivity, AlarmService.class));
                getDialog().dismiss();

                DozeHomeFragment dozeHomeFragment = (DozeHomeFragment) mCurrentActivity.getSupportFragmentManager().findFragmentByTag(DozeActivity.DOZE_HOME_FRAG_TAG);


                dozeHomeFragment.updateDataViewFragment();
                mCurrentActivity.goBackHome();
            }
        });

        return v;
    }
}
