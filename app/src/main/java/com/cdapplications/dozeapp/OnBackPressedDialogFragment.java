package com.cdapplications.dozeapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
/**
 * Created by Colin on 2015-08-31.
 */

/*
One of many custom DialogFragments.  OnBackPressedDialogFragment confirms a user wants to cancel a sleep session.
 */
public class OnBackPressedDialogFragment extends DialogFragment {
    public static OnBackPressedDialogFragment newInstance(){
        return new OnBackPressedDialogFragment();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        //Create an AlertDialog with an AlertDialog.builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_on_back_pressed)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DozeActivity.setAlarmRunning(getActivity(), false);
                        getActivity().stopService(new Intent(getActivity(), AlarmService.class));
                        ((DozeActivity) getActivity()).goBackHome();
                        //When yes is pressed, fragments are closed and the AlarmService is canceled
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing.  Remain in sleep mode.
                    }
                });
        Dialog dialog = builder.create();
        //Don't allow dialog to be closed from touches outside dialog.  Just a preference.
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
