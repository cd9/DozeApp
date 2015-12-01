package com.cdapplications.dozeapp;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.cdapplications.dozeapp.database.SleepRecordDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by Colin on 2015-08-29.
 */

/*
DataViewFragment is our screen that displays the user's sleep data.
 */
public class DataViewFragment extends Fragment {

    SleepRecordDbHelper mDbHelper;
    private static String TAG = "DataViewFragment";

    //Using this list to store DB locally
    private List<SleepRecord> mSleepRecords;

    //Using this list to store which sleep records to display based on selected date range
    private List<SleepRecord> mSleepRecordsToDisplay;

    //For storing strings on the spinner
    private List<String> mDateRangeStringList;

    private GraphAdapter mAdapter;

    //Stored so we don't need to cast every time
    private DozeActivity mCurrentActivity;

    //Store the current spinner selection.  Used to decide what range of data to display
    private int mSpinnerSelection;

    public static DataViewFragment newInstance(){
        return new DataViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Store instance of DozeActivity so we don't need to cast it every time
        mCurrentActivity = (DozeActivity) getActivity();
        //Accessing our SleepRecord database
        mDbHelper = new SleepRecordDbHelper(mCurrentActivity);
        //Used later to store strings for the spinner
        mDateRangeStringList = new ArrayList<>();
        //Used later to store actual values beyond the strings in the spinner

        //-----------------------------------------------------------------------------

        Calendar today = Calendar.getInstance();

        //Check if it's the first time opening this app.
        //If it is, we store the current day in a new sleep record.
        if (mCurrentActivity.getIsFirstDay()) {
            mCurrentActivity.setIsFirstDay(false);
            mDbHelper.addSleepRecord(new SleepRecord(UUID.randomUUID().toString(), today.getTimeInMillis(), 0, 0, null));
        }
        //If it's not the first time, then we need to check if we've already created an empty sleep record for the current date.
        //If we haven't then we add sleep records until we hit the current day
        else if (mDbHelper.getSleepRecordByDate(today.getTimeInMillis())==null){
            Calendar lastestCalendar = Calendar.getInstance();

            lastestCalendar.setTimeInMillis(mDbHelper.getAllSleepRecords().get(mDbHelper.getDbSize()-1).getDate().getTimeInMillis());

            long todayRecord = Calendar.getInstance().getTimeInMillis();

            //To find out how many days we need to add, we subtract the current day minus the latest day we have in our database.
            int days = Math.round((todayRecord-lastestCalendar.getTimeInMillis())/1000/60/60/24);

            //Adding sleep records
            for (int i = 0; i<days;i++){
                lastestCalendar.add(Calendar.DAY_OF_YEAR, 1);
                mDbHelper.addSleepRecord(new SleepRecord(null,lastestCalendar.getTimeInMillis(), 0, 0, null));
            }
        }

        //--------------------------------------------------------------------------------

        int dbSize = mDbHelper.getDbSize();


        //The graph displays 14 days at once.
        //This line calculates how many days we need to add to make the amount of sleep records a multiple of 14
        int sleepRecordsToAdd = dbSize%14==0 ? 0 : 14-(dbSize%14);

        Calendar c = Calendar.getInstance();

        //Getting the latest day in our database.
        c.setTimeInMillis(mDbHelper.getAllSleepRecords().get(mDbHelper.getDbSize()-1).getDate().getTimeInMillis());

        //Adding the required number of sleep records.
        for (int i = 0; i<sleepRecordsToAdd;i++){
            c.add(Calendar.DAY_OF_YEAR, 1);
            mDbHelper.addSleepRecord(new SleepRecord(null,c.getTimeInMillis(), 0, 0, null));
        }

        //Once database has been managed, we store it's contents in an array list.
        mSleepRecords = mDbHelper.getAllSleepRecords();

        //We don't need our database helper anymore.
        mDbHelper.close();
        mAdapter = new GraphAdapter();


        //-------------------------------------------------------------------------

        Calendar dayTracker = Calendar.getInstance();
        dayTracker.setTimeInMillis(mSleepRecords.get(0).getDate().getTimeInMillis());

        mSleepRecordsToDisplay = new ArrayList<>();
        for (int i = 0; i<13; i++){
            mSleepRecordsToDisplay.add(mSleepRecords.get(i));
        }
                //Here we are setting the strings for the spinner
        //Each string will correspond to a selection of 13 days"

        String tempString;
        for (int i = 0;i<mSleepRecords.size();i+=14){
            tempString = mCurrentActivity.mDateRangeFormatter.format(dayTracker.getTime());
            tempString += " to ";
            dayTracker.add(Calendar.DAY_OF_YEAR, 13);
            tempString += mCurrentActivity.mDateRangeFormatter.format(dayTracker.getTime());
            mDateRangeStringList.add(tempString);
            dayTracker.add(Calendar.DAY_OF_YEAR, 1);
        }

        //Default selected spinner value
        mSpinnerSelection = 0;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.data_view_page, container, false);

        //Important stuff to initialize the RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.bar_graph_view);
        //Horizontal LinearLayoutManager for horizontal scrolling data
        LinearLayoutManager layoutManager = new LinearLayoutManager(mCurrentActivity, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        //Setting the adapter of the RecyclerView to our custom adapter.
        mRecyclerView.setAdapter(mAdapter);

        //Setting out spinner and spinner colours
        Spinner mDateSpinner = (Spinner) v.findViewById(R.id.date_spinner);
        //Set spinner colours
        mDateSpinner.getBackground().setColorFilter(getResources().getColor(R.color.light_color), PorterDuff.Mode.SRC_ATOP);
        //Format of items to display in the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mCurrentActivity, R.layout.date_spinner_item, mDateRangeStringList);
        mDateSpinner.setAdapter(spinnerAdapter);

        //When a spinner item is selected, we want to update the range displayed in our graph
        mDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Storing position of spinner
                mSpinnerSelection = position;

                //Updates the data that is shown on the graph
                updateGraphs();

                //Forces the RecyclerView to redraw it's contents
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Not needed
            }
        });

        return v;
    }

    //Custom adapter for our RecyclerView.  Adapter handles the view and behavior of the ViewHolders.
    //RecyclerView is solely responsible for recycling/re-using the ViewHolders when done with them.
    private class GraphAdapter extends RecyclerView.Adapter<GraphHolder>{

        @Override
        public GraphHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mCurrentActivity);
            //Set the view of a ViewHolder
            View v = inflater.inflate(R.layout.bar, parent, false);
            return new GraphHolder(v);
        }


        //Called to initialize a single bar on a graph
        @Override
        public void onBindViewHolder(GraphHolder holder, int position) {
            //Get the corresponding sleep record from our list
            SleepRecord mCurrentSleepRecord = mSleepRecordsToDisplay.get(position);

            //Convert the hours of sleep from milliseconds to hours and store it
            double mSleepHours = mCurrentSleepRecord.getMillisOfSleep() / (1000*60*60);

            //Store the date of the sleep record
            double mRecordDateInMillis = mCurrentSleepRecord.getDate().getTimeInMillis();

            //Store a reference to the bar view on the screen.
            View bar = holder.mBar;

            //Set day of week label
            holder.mDayOfWeek.setText(mCurrentSleepRecord.getDayOfWeekAsString());
            holder.mDayOfWeek.setTextColor(getResources().getColor(R.color.light_color));

            //Set hours of sleep label
            holder.mSleepHourIndicator.setText(String.format("%.1f", mSleepHours));
            //Set day and month label
            holder.mDayAndMonth.setText(mCurrentActivity.mDataDateFormatter.format(mRecordDateInMillis));
            holder.mSleepHourIndicator.setTextColor(getResources().getColor(R.color.light_color));

            //Create parameters from the parameters of the bar view
            ViewGroup.LayoutParams barParams = holder.mBar.getLayoutParams();

            //Calculate the height of the bar in pixels.  If 15 of above just display a height of 13.5
            float barheight = mSleepHours<15 ? ((float) (mSleepHours * 15)) : 13.5f*15;

            //Convert pixels to dpi and set the height of the parameters
            barParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, barheight, getResources().getDisplayMetrics());

            //Apply parameters to the bar view
            bar.setLayoutParams(barParams);

            //Set bar color depending on hours of sleep
            setBarColor(bar, mSleepHours);

        }


        //Sets the amount of ViewHolders / bars to display
        //We want to display however many bars are in the list
        @Override
        public int getItemCount() {
            return mSleepRecordsToDisplay.size();
        }
    }


    //ViewHolder that provides access to our subtitles and views
    private class GraphHolder extends RecyclerView.ViewHolder{
        View mBar;
        TextView mDayOfWeek;
        TextView mSleepHourIndicator;
        TextView mDayAndMonth;
        public GraphHolder(View itemView) {
            super(itemView);
            //Binding out variables to the views
            mBar = itemView.findViewById(R.id.sleep_bar);
            mDayOfWeek = (TextView) itemView.findViewById(R.id.day_of_week);
            mSleepHourIndicator = (TextView) itemView.findViewById(R.id.sleep_value);
            mDayAndMonth = (TextView) itemView.findViewById(R.id.day_and_month);
        }
    }

    public void setBarColor(View bar, double value){
        //Get a reference to the background of the bar
        GradientDrawable drawable = (GradientDrawable) bar.getBackground();
        //Change the color of the bar depending on the hours of sleep
        switch((int)Math.round(value)){
            case 1: drawable.setColorFilter(getResources().getColor(R.color.bar_1), PorterDuff.Mode.MULTIPLY);
                break;
            case 2: drawable.setColorFilter(getResources().getColor(R.color.bar_2), PorterDuff.Mode.MULTIPLY);
                break;
            case 3: drawable.setColorFilter(getResources().getColor(R.color.bar_3), PorterDuff.Mode.MULTIPLY);
                break;
            case 4: drawable.setColorFilter(getResources().getColor(R.color.bar_4), PorterDuff.Mode.MULTIPLY);
                break;
            case 5: drawable.setColorFilter(getResources().getColor(R.color.bar_5), PorterDuff.Mode.MULTIPLY);
                break;
            case 6: drawable.setColorFilter(getResources().getColor(R.color.bar_6), PorterDuff.Mode.MULTIPLY);
                break;
            case 7: drawable.setColorFilter(getResources().getColor(R.color.bar_7), PorterDuff.Mode.MULTIPLY);
                break;
            case 8: drawable.setColorFilter(getResources().getColor(R.color.bar_8), PorterDuff.Mode.MULTIPLY);
                break;
            case 9: drawable.setColorFilter(getResources().getColor(R.color.bar_9), PorterDuff.Mode.MULTIPLY);
                break;
            case 10:  drawable.setColorFilter(getResources().getColor(R.color.bar_10), PorterDuff.Mode.MULTIPLY);
                break;
            case 11: drawable.setColorFilter(getResources().getColor(R.color.bar_11), PorterDuff.Mode.MULTIPLY);
                break;
            case 12: drawable.setColorFilter(getResources().getColor(R.color.bar_12), PorterDuff.Mode.MULTIPLY);
                break;
            case 13: drawable.setColorFilter(getResources().getColor(R.color.bar_13), PorterDuff.Mode.MULTIPLY);
                break;
            default: drawable.setColorFilter(getResources().getColor(R.color.bar_13), PorterDuff.Mode.MULTIPLY);
                break;
        }
    }

    public void updateGraphs(){
        //Sync our local list with the database
        mSleepRecords = mDbHelper.getAllSleepRecords();

        //Replacing mSleepRecordsToDisplay with the correct sleep records depending on what spinner position is selected
        mSleepRecordsToDisplay.clear();
        int startingPoint = mSpinnerSelection*14;
        for (int i = startingPoint;i<startingPoint+13;i++){
            mSleepRecordsToDisplay.add(mSleepRecords.get(i));
        }

        //Force the adapter to redraw everything
        mAdapter.notifyDataSetChanged();
    }
}
