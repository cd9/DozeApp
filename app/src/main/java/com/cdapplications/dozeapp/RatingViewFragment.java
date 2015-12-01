package com.cdapplications.dozeapp;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

/**
 * Created by Colin on 2015-10-29.
 */

/*
RatingViewFragment displays the sleep comments and ratings given by the user
 */

//RatingViewFragment is very similar to DataViewFragment.  A lot of code is repeated, which is bad.
//A better approach instead of repetition is to implement DataViewFragment and RatingViewFragment from an abstract class.
//This will optimized in a future update

public class RatingViewFragment extends Fragment {

    private DozeActivity mCurrentActivity;
    private SleepRecordDbHelper mDbHelper;
    private List<SleepRecord> mSleepRecords;
    private List<SleepRecord> mSleepRecordsToDisplay;
    private List<String> mDateRangeStringList;
    private final SimpleDateFormat mDataDateFormatter = new SimpleDateFormat("MM/dd");
    private int mSpinnerSelection;
    RatingListAdapter mAdapter = new RatingListAdapter();

    public static RatingViewFragment newInstance(){
        return new RatingViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Grabbing out activity for later
        mCurrentActivity = (DozeActivity) getActivity();

        mDbHelper = new SleepRecordDbHelper(mCurrentActivity);

        //Setting up all our lists
        mSleepRecords = new ArrayList<>();
        mSleepRecordsToDisplay = new ArrayList<>();
        mDateRangeStringList = new ArrayList<>();

        //Our database will already be setup because of the work done in DataViewFragment.
        //All we have to do now is add the right SleepRecords to the RecyclerView
        mSleepRecords = mDbHelper.getAllSleepRecords();
        mSleepRecordsToDisplay = new ArrayList<>();
        for (int i = 0; i<13; i++){
            //Adding the first 13 records
            mSleepRecordsToDisplay.add(mSleepRecords.get(i));
        }

        Calendar dayTracker = Calendar.getInstance();
        dayTracker.setTimeInMillis(mSleepRecords.get(0).getDate().getTimeInMillis());


        //Building the list of strings for the spinner.  Same as in DataVieewFragment
        String tempString;
        for (int i = 0;i<mSleepRecords.size();i+=14){
            tempString = mCurrentActivity.mDateRangeFormatter.format(dayTracker.getTime());
            tempString += " to ";
            dayTracker.add(Calendar.DAY_OF_YEAR, 13);
            tempString += mCurrentActivity.mDateRangeFormatter.format(dayTracker.getTime());
            mDateRangeStringList.add(tempString);
            dayTracker.add(Calendar.DAY_OF_YEAR, 1);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.rating_view, container, false);

        //Important RecyclerView Setup
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.rating_list_view);
        //LinearLayoutManager is vertical here instead of horizontal in DataViewFragment
        LinearLayoutManager layoutManager = new LinearLayoutManager(mCurrentActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //Setup the colors of our spinner
        Spinner mDateSpinner = (Spinner) v.findViewById(R.id.rating_spinner);
        mDateSpinner.getBackground().setColorFilter(getResources().getColor(R.color.light_color), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mCurrentActivity, R.layout.date_spinner_item, mDateRangeStringList);
        mDateSpinner.setAdapter(spinnerAdapter);
        mDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //When the spinner is selected, update the list of ratings to reflect the range selected
                mSpinnerSelection = position;
                updateRatingList();
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return v;
    }


    //RatingListAdapter is simpler than GraphAdapter.  Little conversion is needed from the raw Sleep Record values.
    private class RatingListAdapter extends RecyclerView.Adapter<GraphHolder>{

        @Override
        public GraphHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mCurrentActivity);
            //rating_segment is for an individual rating on our list
            View v = inflater.inflate(R.layout.rating_segment, parent, false);
            return new GraphHolder(v);
        }


        @Override
        public void onBindViewHolder(GraphHolder holder, int position) {

            SleepRecord mCurrentSleepRecord = mSleepRecordsToDisplay.get(position);

            //Setting the rating number
            holder.mRating.setText(Double.toString(mCurrentSleepRecord.getSleepRating()));

            //Calculating the hours of sleep
            double mSleepHours = mCurrentSleepRecord.getMillisOfSleep() / (1000*60*60);

            //Setting the day and the hours of sleep
            holder.mDayHour.setText(mDataDateFormatter.format(mCurrentSleepRecord.getDate().getTime())+
                    " - "+Math.round(mSleepHours)+"h");

            //Set the comment.  We add quotes around the comment if it's not empty.
            String comment = mCurrentSleepRecord.getComment();

            if (comment.equals("")) holder.mComment.setText("");
            else holder.mComment.setText("\""+comment+"\"");

        }

        @Override
        public int getItemCount() {
            return mSleepRecordsToDisplay.size();
        }
    }


    //ViewHolder that provides access to our TextViews
    private class GraphHolder extends RecyclerView.ViewHolder{
        TextView mRating;
        TextView mDayHour;
        TextView mComment;
        public GraphHolder(View itemView) {
            super(itemView);
            //Binding variables
            mRating = (TextView) itemView.findViewById(R.id.rating_number);
            mDayHour = (TextView) itemView.findViewById(R.id.day_hour);
            mComment = (TextView) itemView.findViewById(R.id.comment);
        }
    }

    //Updates the list of ratings.
    public void updateRatingList(){
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

