package com.cdapplications.dozeapp;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Created by Colin on 2015-09-12.
 */

/*
One of many custom DialogFragments.  OnBackPressedDialogFragment confirms a user wants to cancel a sleep session.
 */

public class RingModeDialogFragment extends DialogFragment {

    public static final int RING_MODE_RESULT_CODE = 54;
    private DozeActivity mCurrentActivity;
    private RadioGroup mRadioGroup;

    public static RingModeDialogFragment newInstance(){
        return new RingModeDialogFragment();
    }
    private static String TAG = "RingModeDialogFragment";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.ring_mode_view, container, false);
        Dialog dialog = getDialog();
        dialog.setTitle(getResources().getString(R.string.ring_mode_title));
        dialog.setCanceledOnTouchOutside(false);
        mRadioGroup = (RadioGroup) v.findViewById(R.id.ring_mode_button_group);

        switch(mCurrentActivity.getRingModeSelection()){
            case 0:
                mRadioGroup.check(R.id.ring_only_button);
                break;
            case 1:
                mRadioGroup.check(R.id.vibrate_only_button);
                break;
            case 2:
                mRadioGroup.check(R.id.both_button);
                break;
            default:
                Log.i(TAG, "no selection found");
        }

        Button setButton = (Button) v.findViewById(R.id.set_button);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(mRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.ring_only_button:
                        mCurrentActivity.setRingModeSelection(0);
                        break;
                    case R.id.vibrate_only_button:
                        mCurrentActivity.setRingModeSelection(1);
                        break;
                    case R.id.both_button:
                        mCurrentActivity.setRingModeSelection(2);
                }
                ((SettingsFragment) getTargetFragment()).refreshIndicators(RING_MODE_RESULT_CODE);
                mCurrentActivity.onBackPressed();
            }
        });

        return v;
    }

}
