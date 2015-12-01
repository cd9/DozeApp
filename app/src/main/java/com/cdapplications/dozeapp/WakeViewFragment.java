package com.cdapplications.dozeapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by Colin on 2015-08-27.
 */

/*

WakeViewFragment is the Fragment that allows the majority of the functionality of Doze.  It allows alarms to be set

 */
public class WakeViewFragment extends Fragment {

    //List of wake times used to display the alarm times
    private List<WakeTime> mWakeTimes;

    //Length of a sleep cycle
    //todo allow customization of sleep cycle length
    private static int SLEEP_CYCLE_LENGTH = 90;

    //Back stack entry names to reverse starting of fragments
    public static final String SLEEP_MODE_BACK_STACK_NAME = "sleepModeBackStack";
    private static final String CUSTOM_ALARM_PICKER_BACK_STACK_NAME = "customAlarmPickerBackStack";

    //Tags to get references to Fragments
    public static final String SLEEP_MODE_TAG = "SleepModeTag";
    private static final String CUSTOM_ALARM_PICKER_TAG = "customAlarmPickerTag";

    //Custom adapter for our RecyclerView
    private WakeAdapter mWakeAdapter;
    //LinearLayoutManager for out RecyclerView
    LinearLayoutManager mLayoutManager;

    //TextView for bottom button
    private TextView mBottomTextView;

    //Icons for bottom button
    ImageView mBottomAlarmIcon1;
    ImageView mBottomAlarmIcon2;

    //Keeps track of if an alarm time is selected
    private boolean mIsTimeSelected = false;
    //Keeps track of which alarm time is selected
    private int mPositionSelected;

    //BroadcastReceiver to handle updating TextViews every time the system clock changes
    BroadcastReceiver mBroadcastReceiver;

    //Formatter for clocks
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm aa");

    //TextView for current alarm time
    private TextView mCurrentTime;
    //Calendar for storing the alarm time that will be sent
    private Calendar mCurrentAlarmTime;


    public static WakeViewFragment newInstance(){
        return new WakeViewFragment();
    }
    private DozeActivity mCurrentActivity;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCurrentActivity = (DozeActivity) getActivity();

        //Initialize the wake times
        initWakeTimes();

    }


    //Called on onResume so BroadcastReceiver stops updating the clocks when in task manager
    @Override
    public void onStart(){
        super.onStart();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK)==0){
                    //Update the wake times
                    updateWakeTimes();

                    if (mIsTimeSelected){
                        //If a time is selected, also update the calendar alarm time stored
                        mCurrentAlarmTime = mWakeTimes.get(mPositionSelected).getAlarmCalendarTime();
                    }

                    //Update the clock
                    mCurrentTime.setText(mTimeFormat.format(new Date()));
                    FragmentManager fm = mCurrentActivity.getSupportFragmentManager();

                    //If our fragment manager returns null, don't update.
                    //Update SleepModeFragment clock
                    SleepModeFragment smf = (SleepModeFragment) fm.findFragmentByTag(SLEEP_MODE_TAG);
                    if (smf!=null) smf.updateTime();

                    //Update RingFragment clock
                    RingFragment rf = (RingFragment) fm.findFragmentByTag(DozeActivity.RING_FRAGMENT_TAG);
                    if (rf!=null) rf.updateTime();
                }
            }
        };

        //Register the BroadcastReceiver to only run on a system time chance
        mCurrentActivity.registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        //Clock is updated
        mCurrentTime.setText(mTimeFormat.format(new Date()));

        //Wake times are updated
        updateWakeTimes();
    }


    //On stop, pause the BroadcastReceiver
    @Override
    public void onStop(){
        super.onStop();
        if (mBroadcastReceiver!=null){
            mCurrentActivity.unregisterReceiver(mBroadcastReceiver);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.wake_recycler_view_page, container, false);

        //Set up RecyclerView and attach LayoutManager and WakeAdapter
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.wake_screen_recycler_view);
        mLayoutManager = new LinearLayoutManager(mCurrentActivity);
        recyclerView.setLayoutManager(mLayoutManager);
        mWakeAdapter = new WakeAdapter();
        recyclerView.setAdapter(mWakeAdapter);

        //Set bottom button text, icons, and colors
        mBottomAlarmIcon1 = (ImageView) v.findViewById(R.id.mini_alarm_icon_bottom_1);
        mBottomAlarmIcon2 = (ImageView) v.findViewById(R.id.mini_alarm_icon_bottom_2);
        mBottomAlarmIcon1.setColorFilter(getResources().getColor(R.color.bottom_icon_color));
        mBottomAlarmIcon2.setColorFilter(getResources().getColor(R.color.bottom_icon_color));
        mBottomTextView = (TextView) v.findViewById(R.id.bottom_text_view);

        //Setup clock
        mCurrentTime = (TextView) v.findViewById(R.id.current_time);
        mCurrentTime.setText(mTimeFormat.format(Calendar.getInstance().getTimeInMillis()));

        //Setup bottom button.
        //onClick, start sleep mode and alarm
        CardView bottomCardView = (CardView) v.findViewById(R.id.bottom_card_view);
        bottomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsTimeSelected) {
                    mCurrentActivity.startSleepMode(mCurrentAlarmTime);
                }
            }
        });
        return v;

    }

    //Creates a list of 10 different WakeTimes, each 90 minutes apart.
    private void initWakeTimes(){
        mWakeTimes = new ArrayList<>();
        for (int i = 0; i<10;i++){
            mWakeTimes.add(new WakeTime());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (SLEEP_CYCLE_LENGTH * (i+1))+mCurrentActivity.getFallAsleepMinutes());
            mWakeTimes.get(i).setAlarmTime(calendar);
        }
    }

    //Resets the alarm times for every WakeTime
    public void updateWakeTimes(){
        for (int i = 0; i<mWakeTimes.size();i++){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (SLEEP_CYCLE_LENGTH * (i+1))+mCurrentActivity.getFallAsleepMinutes());
            mWakeTimes.get(i).setAlarmTime(calendar);
            //Notify the adapter of the changes.
            mWakeAdapter.notifyDataSetChanged();
        }
    }

    //Custom RecyclerView.Adapter for WakeTimes
    private class WakeAdapter extends RecyclerView.Adapter<WakeHolder>{


        //On creation of each WakeHolder, set colours and icons
        @Override
        public WakeHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(mCurrentActivity);
            View view = layoutInflater.inflate(R.layout.card_view, viewGroup, false);
            ImageView alarmIcon = (ImageView) view.findViewById(R.id.mini_alarm_icon);
            alarmIcon.setColorFilter(getResources().getColor(R.color.light_color));
            return new WakeHolder(view);
        }

        //When WakeHolder appears on screen, fill it with the appropriate data.
        @Override
        public void onBindViewHolder (WakeHolder wakeHolder, int i) {
            WakeTime wakeTime = mWakeTimes.get(i);
            if (i==9) {
                //The ninth WakeHolder is really a custom alarm time button.
                //The appearance is set differently than the other Wake Times.
                wakeHolder.mText.setText(getResources().getString(R.string.custom_alarm));
                wakeHolder.mTimeIndicator.setVisibility(View.GONE);
                wakeHolder.mAlarmIcon.setVisibility(View.GONE);
                wakeHolder.mText.setTextSize(35);
            }else{
                wakeHolder.mText.setText(getResources().getString(R.string.set_time_for));
                wakeHolder.mTimeIndicator.setVisibility(View.VISIBLE);
                wakeHolder.mAlarmIcon.setVisibility(View.VISIBLE);
                wakeHolder.mText.setTextSize(20);
                wakeHolder.mTimeIndicator.setText(wakeTime.getAlarmTime());


                //Whether or not to light up the wake time.
                if (wakeTime.isActive()){
                    setWakeTimeColors(wakeHolder, true);
                }else{
                    setWakeTimeColors(wakeHolder, false);
                }
            }
        }

        //Show a WakeHolder for every WakeTime in the list
        @Override
        public int getItemCount() {
            return mWakeTimes.size();
        }


        //Sets the WakeTime colors based on whether or not a WakeTime is selected
        private void setWakeTimeColors(WakeHolder wakeHolder, boolean isSelected){
            if (isSelected){
               wakeHolder.mAlarmIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_on_black_48dp));
                wakeHolder.mAlarmIcon.setColorFilter(getResources().getColor(R.color.mini_selected_color));
                wakeHolder.mTimeIndicator.setTextColor(getResources().getColor(R.color.mini_selected_color));
                wakeHolder.mText.setTextColor(getResources().getColor(R.color.mini_selected_color));

            }else{
                wakeHolder.mAlarmIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_black_48dp));
                wakeHolder. mAlarmIcon.setColorFilter(getResources().getColor(R.color.light_color));
                wakeHolder.mTimeIndicator.setTextColor(getResources().getColor(R.color.light_color));
                wakeHolder.mText.setTextColor(getResources().getColor(R.color.light_color));

            }

        }

    }

    //Setting up a custom ViewHolder
    private class WakeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTimeIndicator;
        private TextView mText;
        private ImageView mAlarmIcon;

        //Constructor for new WakeHolders
        public WakeHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.mini_text);
            mTimeIndicator = (TextView) itemView.findViewById(R.id.mini_time);
            mAlarmIcon = (ImageView) itemView.findViewById(R.id.mini_alarm_icon);
            itemView.setOnClickListener(this);

        }

        //Called when an alarm time is pressed.
        @Override
        public void onClick(View v) {
            //store the position of the alarm time in the list
            int position = getAdapterPosition();
            /*
            The 9th alarm time is the custom alarm time.
            When pressed, it should behave differently than the others
            by initiating a DialogFragment to start sleep mode.
            */
            if (position==9){

                //Reset the alarm times to deselect them all
                initWakeTimes();
                mIsTimeSelected = false;

                FragmentManager fm = mCurrentActivity.getSupportFragmentManager();

                //Create new custom alarm dialog
                CustomAlarmPickerDialogFragment fragment = CustomAlarmPickerDialogFragment.newInstance();

                //Add the fragment to the stack.
                fm.beginTransaction().add(fragment, CUSTOM_ALARM_PICKER_TAG)

                        //Store the backstack and its name
                        //This will allow us to check what fragment is on the stack
                        .addToBackStack(CUSTOM_ALARM_PICKER_BACK_STACK_NAME)
                        .commit();
            }else{

                //Else a normal alarm time is pressed.  Select it and update colors.
                //Get the alarm time from our list
                WakeTime mCurrentWakeTime = mWakeTimes.get(position);

                if (mCurrentWakeTime.isActive()) {

                    //If the alarm time is selected, deselect all alarm times.
                    initWakeTimes();
                    mIsTimeSelected = false;

                } else {
                    //If the alarm time is not selected, select it
                    mIsTimeSelected = true;

                    mPositionSelected = position;

                    //Store the alarm time for later when the user presses enter sleep mode
                    mCurrentAlarmTime = mCurrentWakeTime.getAlarmCalendarTime();

                    //Deselect all other alarm times and select the pressed alarm time
                    initWakeTimes();
                    mWakeTimes.get(position).setIsActive(true);
                }
            }
            //Refresh the view of the adapter
            mWakeAdapter.notifyDataSetChanged();

            //Either enable or disable the enter sleep mode button
            setBottomColors();
        }

        //Set bottom button colors based on whether or not an alarm time is selected
        private void setBottomColors() {
            if (mIsTimeSelected) {
                mBottomTextView.setTextColor(getResources().getColor(R.color.light_color));
                mBottomAlarmIcon1.setColorFilter(getResources().getColor(R.color.light_color));
                mBottomAlarmIcon2.setColorFilter(getResources().getColor(R.color.light_color));
            } else {
                mBottomTextView.setTextColor(getResources().getColor(R.color.bottom_text_color));
                mBottomAlarmIcon1.setColorFilter(getResources().getColor(R.color.bottom_icon_color));
                mBottomAlarmIcon2.setColorFilter(getResources().getColor(R.color.bottom_icon_color));
            }

        }


    }


}
