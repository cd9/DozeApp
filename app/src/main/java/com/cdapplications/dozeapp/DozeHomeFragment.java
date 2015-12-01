package com.cdapplications.dozeapp;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/*
DozeHomeFragment is the fragment that holds the ViewPager that allows navigation through pages in Doze.
 */

public class DozeHomeFragment extends Fragment {

    //Number of pages to swipe through
    private static final int PAGES_AMOUNT = 4;
    private ViewPager mPager;

    //Icons for navigation bar
    private ImageView mChartIcon;
    private ImageView mMoonIcon;
    private ImageView mSettingsIcon;
    private ImageView mStarIcon;

    //Color of selected/unselected icons
    private int selectedColor;
    private int unselectedColor;

    //Storing instances of fragments in ViewPager.
    //Fragments started in a ViewPager cannot be accessed through tags.
    private WakeViewFragment mWakeViewFragment;
    private DataViewFragment mDataViewFragment;


    public static DozeHomeFragment newInstance(){
        return new DozeHomeFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.doze_home_v2, container, false);

        //Getting a reference to the ViewPager and setting an adapter
        mPager = (ViewPager) v.findViewById(R.id.main_pager);

        PagerAdapter pagerAdapter = new MainPagerAdapter(getChildFragmentManager());

        //Getting colors from resources
        selectedColor = getResources().getColor(R.color.light_color);
        unselectedColor = getResources().getColor(R.color.top_not_selected_color);

        //Setting icons
        mChartIcon = (ImageView) v.findViewById(R.id.chart_icon);
        mMoonIcon = (ImageView) v.findViewById(R.id.moon_icon);
        mSettingsIcon = (ImageView) v.findViewById(R.id.settings_icon);
        mStarIcon = (ImageView) v.findViewById(R.id.star_icon);

        //Allows navigation to page by pressing icon
        mStarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(0);
            }
        });
        mChartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(1);
            }
        });
        mMoonIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(2);
            }
        });
        mSettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(3);
            }
        });


        //Initializes the colours of the icons.
        //By default the 2nd icon is selected
        mSettingsIcon.setColorFilter(unselectedColor);
        mChartIcon.setColorFilter(unselectedColor);
        mMoonIcon.setColorFilter(selectedColor);
        mStarIcon.setColorFilter(unselectedColor);


        mPager.setAdapter(pagerAdapter);
        //On start the ViewPager is set to page 1, the middle page
        mPager.setCurrentItem(2);
        mPager.setOffscreenPageLimit(1);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //Depending on what page the user is on, a different icon will be selected,
                //so the colours much change

                Log.i("TAG,", position+"");
                switch (position) {
                    case 0:
                        mChartIcon.setColorFilter(unselectedColor);
                        mMoonIcon.setColorFilter(unselectedColor);
                        mStarIcon.setColorFilter(selectedColor);
                        mSettingsIcon.setColorFilter(unselectedColor);
                        break;
                    case 1:
                        mChartIcon.setColorFilter(selectedColor);
                        mMoonIcon.setColorFilter(unselectedColor);
                        mStarIcon.setColorFilter(unselectedColor);
                        mSettingsIcon.setColorFilter(unselectedColor);
                        break;
                    case 2:
                        mChartIcon.setColorFilter(unselectedColor);
                        mMoonIcon.setColorFilter(selectedColor);
                        mStarIcon.setColorFilter(unselectedColor);
                        mSettingsIcon.setColorFilter(unselectedColor);
                        break;
                    case 3:
                        mChartIcon.setColorFilter(unselectedColor);
                        mMoonIcon.setColorFilter(unselectedColor);
                        mStarIcon.setColorFilter(unselectedColor);
                        mSettingsIcon.setColorFilter(selectedColor);
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return v;
    }



    //Custom ViewPager adapter
    private class MainPagerAdapter extends FragmentStatePagerAdapter{

        public MainPagerAdapter(FragmentManager fm){
            super(fm);
        }


        //A different Fragment will be loaded depending on page number
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0: return RatingViewFragment.newInstance();
                case 1: return DataViewFragment.newInstance();
                case 2: return WakeViewFragment.newInstance();
                case 3: return SettingsFragment.newInstance();
                default: return DataViewFragment.newInstance();
            }
        }


        //Save references to the fragments.  These references are saved so the data can be updated without them being on screen
        //Since these fragments were created in a ViewPager, they cannot be referenced to with a tag.
        @Override
        public Object instantiateItem(ViewGroup container, int position){
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            switch(position){
                case 1:
                    mDataViewFragment = (DataViewFragment) createdFragment;
                    break;
                case 2:
                    mWakeViewFragment = (WakeViewFragment) createdFragment;
            }
            return createdFragment;
        }

        //The number of pages to display
        @Override
        public int getCount() {
            return PAGES_AMOUNT;
        }
    }

    //Updating WakeViewFragment through DozeHomeFragment because DozeHomeFragment holds the reference to WakeViewFragment
    public void updateWakeViewFragment(){
        if (mWakeViewFragment!= null) {
            mWakeViewFragment.updateWakeTimes();
        }
    }

    //Updating DataViewFragment through DozeHomeFragment because DozeHomeFragment holds the reference to DataViewFragment
    public void updateDataViewFragment(){
        if (mDataViewFragment!=null){
            mDataViewFragment.updateGraphs();
        }
    }

}
