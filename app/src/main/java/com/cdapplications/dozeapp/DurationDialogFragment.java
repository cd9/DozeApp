package com.cdapplications.dozeapp;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
/**
 * Created by Colin on 2015-09-04.
 */

/*
One of many custom DialogFragments.  DurationDialogFragment allows a custom minute value to be set and returned to the fragment that started it.
DialogFragment currently can be accessed through the snooze duration setting or the fall asleep time setting, so there are methods specific to either setting
 */
public class DurationDialogFragment extends DialogFragment {

    private String TAG = "DurationDialogFragmment";

    //Used later to set the initial minute value displayed
    private int mMinutes;

    //One of these identifiers is sent to this DialogFragment when starting it.  Depending on which one is sent, different values will be displayed
    public static final String SNOOZE_FRAG_IDENTIFIER = "com.cdapplications.SNOOZE_FRAG_IDENTIFIER";
    public static final String FALL_ASLEEP_FRAG_IDENTIFIER = "com.cdapplications.FALL_ASLEEP_FRAG_IDENTIFIER";
    private static final String FRAGMENT_IDENTIFIER_TAG = "com.cdapplications.FRAGMENT_IDENTIFIER";

    //Used to store the title of the Dialog sent from the fragment
    private static final String TITLE_KEY = "com.cdapplciations.TITLE_KEY";

    //Store DozeActivity so don't need to cast later
    private DozeActivity mCurrentActivity;

    //Result codes are sent back to the fragment on termination of the dialog
    public static final int SNOOZE_RESULT_CODE = 0;
    public static final int FALL_ASLEEP_RESULT_CODE = 1;


    //Used to store the fragment identifiers
    private String mFragment;

    //A custom title and a fragment identifier is set when starting DurationDialogFragment
    public static DurationDialogFragment newInstance(String title, String fragmentIdentifier){
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(FRAGMENT_IDENTIFIER_TAG, fragmentIdentifier);
        DurationDialogFragment frag = new DurationDialogFragment();
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstancState){
        mCurrentActivity = (DozeActivity) getActivity();
        super.onCreate(savedInstancState);

        //Getting fragment identifier
        mFragment = getArguments().getString(FRAGMENT_IDENTIFIER_TAG, null);

        //Set the value of mMinutes depending on what fragment started this Dialog
        //mMinutes is used to set the initial minute value displayed
        switch (mFragment) {
            case SNOOZE_FRAG_IDENTIFIER:
                mMinutes = mCurrentActivity.getSnoozeMinutes();
                break;
            case FALL_ASLEEP_FRAG_IDENTIFIER:
                mMinutes = mCurrentActivity.getFallAsleepMinutes();
                break;
            default:
                Log.i(TAG, "no fragment found by tag");
                break;
        }

        //If mMinutes ends up being -1, then there was no value found, so mMinutes is set to a default of 5 minutes
        if (mMinutes==-1){
            mMinutes = 5;
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.time_pick_dialog, container, false);
        TextView subtitleView = (TextView) v.findViewById(R.id.subtitle_view);
        Dialog dialog = getDialog();

        //Set the title from the title sent earlier
        dialog.setTitle(getArguments().getString(TITLE_KEY));
        dialog.setCanceledOnTouchOutside(false);

        //Set up a NumberPicker for the minute values
        final NumberPicker mNumberPicker = (NumberPicker) v.findViewById(R.id.minute_picker);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(60);

        //Set the initial value for minutes using mMinutes
        mNumberPicker.setValue(mMinutes);
        mNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        Button mSetButton = (Button) v.findViewById(R.id.set_button);

        //If the snooze setting started this dialog, the button is set to store the snooze value in the phone's memory
        if (mFragment.equals(SNOOZE_FRAG_IDENTIFIER)){
            mSetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mins = mNumberPicker.getValue();
                    //Storing value
                    mCurrentActivity.setSnoozeMinutes(mins);
                    //Closing dialog
                    getActivity().onBackPressed();
                }
            });

            //If the fall asleep setting started this dialog, the button is set to store the fall asleep value in the phone's memory
        }else if (mFragment.equals(FALL_ASLEEP_FRAG_IDENTIFIER)){
            subtitleView.setVisibility(View.VISIBLE);
            mSetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mins = mNumberPicker.getValue();
                    //Storing value
                    mCurrentActivity.setFallAsleepMinutes(mins);
                    //Closing dialog
                    getActivity().onBackPressed();
                }
            });
        }
        return v;
    }

    //When the dialog is closed, either send a code to update the snooze setting or send a code to update the fall asleep setting
    @Override
    public void onDestroy(){
        SettingsFragment targetFragment = (SettingsFragment) getTargetFragment();
        Log.i(TAG, mFragment);
        if (mFragment.equals(SNOOZE_FRAG_IDENTIFIER)){
            targetFragment.refreshIndicators(SNOOZE_RESULT_CODE);
        }else if (mFragment.equals(FALL_ASLEEP_FRAG_IDENTIFIER)){
            targetFragment.refreshIndicators(FALL_ASLEEP_RESULT_CODE);
        }
        super.onDestroy();
    }

}
