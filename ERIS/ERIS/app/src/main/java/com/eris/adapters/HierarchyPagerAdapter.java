package com.eris.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.eris.fragments.IncidentHierarchyFragment;

/**
 * Created by derieux on 11/9/16.
 */

public class HierarchyPagerAdapter extends FragmentPagerAdapter {

    // NOTE: guide for building the custom adapter is found at the following
    // https://guides.codepath.com/android/google-play-style-tabs-using-tablayout

    /*
     * Private elements
     */
    private String[] tabTitles = new String[] {"Team", "Everyone"};
    private Context context;

    /*
     * Constants
     */
    final int PAGE_COUNT = tabTitles.length;


    /**
     * Constructor
     *
     * @param fm
     * @param context
     */
    public HierarchyPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }


    @Override
    public Fragment getItem(int position) {

        // Determine which tab item was clicked.
        switch (tabTitles[position].toLowerCase()) {
            case "team":
                break;
            case "everyone":
                break;
        }
        return new IncidentHierarchyFragment();
    }


    @Override
    public int getCount() {
        return this.PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
