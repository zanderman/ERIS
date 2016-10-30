//
// Copyright 2016 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.9
//
package com.eris.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.eris.R;
import com.eris.classes.NavigationDrawerMenuItem;
import com.eris.fragments.DemoResponderDatabaseFragment;
import com.eris.fragments.HomeFragment;
import com.eris.fragments.IncidentListFragment;
import com.eris.fragments.SettingsFragment;
import com.eris.navigation.NavigationDrawer;
import com.eris.services.LocationService;
import com.eris.services.DatabaseService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /** Class name for log messages. */
    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    /** Bundle key for saving/restoring the toolbar title. */
    private final static String BUNDLE_KEY_TOOLBAR_TITLE = "title";

    /** The identity manager used to keep track of the current user account. */
    private IdentityManager identityManager;

    /** The toolbar view control. */
    private Toolbar toolbar;

    /** Our navigation drawer class for handling navigation drawer logic. */
    private NavigationDrawer navigationDrawer;

    /** The helper class used to toggle the left navigation drawer open and closed. */
    private ActionBarDrawerToggle drawerToggle;

    public LocationService locationService;
    public DatabaseService databaseService;

    /*
     * Constants
     */
    final private int REQUEST_CODE_ACCESS_FINE_LOCATION = 123;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder instanceof DatabaseService.DatabaseServiceBinder) {
                databaseService = ((DatabaseService.DatabaseServiceBinder)iBinder).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //TODO idk what to do here.  How do I tell which service stopped?
        }
    };




    /**
     * Initializes the Toolbar for use with the activity.
     */
    private void setupToolbar(final Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(com.eris.R.id.toolbar);
        // Set up the activity to use this toolbar. As a side effect this sets the Toolbar's title
        // to the activity's title.
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            // Some IDEs such as Android Studio complain about possible NPE without this check.
            assert getSupportActionBar() != null;

            // Restore the Toolbar's title.
            getSupportActionBar().setTitle(
                savedInstanceState.getCharSequence(BUNDLE_KEY_TOOLBAR_TITLE));
        }
    }


    /**
     * Initializes the navigation drawer menu to allow toggling via the toolbar or swipe from the
     * side of the screen.
     */
    private void setupNavigationMenu(final Bundle savedInstanceState) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerItems = (ListView) findViewById(R.id.nav_drawer_items);

        // Create the navigation drawer.
        navigationDrawer = new NavigationDrawer(this, toolbar, drawerLayout, drawerItems,
            R.id.main_fragment_container);

        // Add navigation drawer menu items.
        NavigationDrawerMenuItem homeItem = new NavigationDrawerMenuItem(
                "Home", R.mipmap.ic_home_black_24dp,
                HomeFragment.class.getName(), HomeFragment.class.getSimpleName()
        );
        NavigationDrawerMenuItem incidentListItem = new NavigationDrawerMenuItem(
                "Incident List", R.mipmap.ic_explore_black_24dp,
                IncidentListFragment.class.getName(), IncidentListFragment.class.getSimpleName()
        );
        NavigationDrawerMenuItem commandStructureItem = new NavigationDrawerMenuItem(
                "Command Structure", R.mipmap.ic_line_style_black_24dp,
                DemoResponderDatabaseFragment.class.getName(), DemoResponderDatabaseFragment.class.getSimpleName()
        );
        NavigationDrawerMenuItem settingsItem = new NavigationDrawerMenuItem(
                "Settings", R.mipmap.ic_settings_black_24dp,
                SettingsFragment.class.getName(), SettingsFragment.class.getSimpleName()
        );
        NavigationDrawerMenuItem loginItem = new NavigationDrawerMenuItem(
                "Login", R.mipmap.ic_assignment_ind_black_24dp,
                DemoResponderDatabaseFragment.class.getName(), DemoResponderDatabaseFragment.class.getSimpleName()
        );

        navigationDrawer.addItemToMenu(homeItem);
        navigationDrawer.addItemToMenu(incidentListItem);
        navigationDrawer.addItemToMenu(commandStructureItem);
        navigationDrawer.addItemToMenu(settingsItem);
        navigationDrawer.addItemToMenu(loginItem);

        if (savedInstanceState == null) {
            // Add the home fragment to be displayed initially.
            navigationDrawer.showHome();  // Change this later if needed 44444444
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain a reference to the mobile client. It is created in the Application class,
        // but in case a custom Application class is not used, we initialize it here if necessary.
        AWSMobileClient.initializeMobileClientIfNecessary(this);
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        // Obtain a reference to the identity manager.
        identityManager = awsMobileClient.getIdentityManager();

        setContentView(R.layout.activity_main);

        setupToolbar(savedInstanceState);

        setupNavigationMenu(savedInstanceState);

        // Run the location and database services.
        runLocationService();
        runDatabaseService();
        bindService(new Intent(this, DatabaseService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();


        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here excluding the home button.

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // Save the title so it will be restored properly to match the view loaded when rotation
        // was changed or in case the activity was destroyed.
        if (toolbar != null) {
            bundle.putCharSequence(BUNDLE_KEY_TOOLBAR_TITLE, toolbar.getTitle());
        }
    }

    @Override
    public void onClick(final View view) {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = this.getSupportFragmentManager();
        
        if (navigationDrawer.isDrawerOpen()) {
            navigationDrawer.closeDrawer();
            return;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            if (fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName()) == null) {
                final Class fragmentClass = HomeFragment.class;
                // if we aren't on the home fragment, navigate home.
                final Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());

                fragmentManager
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment, fragmentClass.getSimpleName())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();

                // Set the title for the fragment.
                setActionBarTitle(getString(R.string.app_name));

                return;
            }
        }
        super.onBackPressed();
    }


    /**
     * This method performs all action needed to start the location background service.
     */
    private void runLocationService() {

        // User has pre-allowed location permissions.
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            startService( new Intent(this, LocationService.class) ); // Start location service.
            return;
        }

        // Need to prompt user to allow location permissions.
        else  {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ACCESS_FINE_LOCATION);
            return;
        }
    }


    /**
     * This method performs all action needed to start the database background service.
     */
    private void runDatabaseService() {
        startService(new Intent(this, DatabaseService.class));
        return;
    }


    /**
     * Required starting in Android SDK 23, developers now need to request permissions at runtime.
     * <p>
     * This method processes all request results.
     *
     * @param requestCode ID of request that was processed.
     * @param permissions Array of string permissions.
     * @param grantResults Array of result codes from processing the different requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService( new Intent(this, LocationService.class) ); // Start location service.
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Sets the title of the ActionBar.
     * <p>
     * This method is necessary for altering the title of the ActionBar from within fragments.
     *
     * @param title New title string.
     */
    public void setActionBarTitle(String title) {
        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}
