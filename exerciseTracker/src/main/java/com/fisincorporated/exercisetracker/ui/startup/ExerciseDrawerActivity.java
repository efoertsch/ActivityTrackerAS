package com.fisincorporated.exercisetracker.ui.startup;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.about.AboutActivityTracker;
import com.fisincorporated.exercisetracker.ui.backuprestore.BackupRestoreActivity;
import com.fisincorporated.exercisetracker.ui.history.ActivityList;
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseListActivity;
import com.fisincorporated.exercisetracker.ui.preferences.SettingsActivity;
import com.fisincorporated.exercisetracker.ui.startactivity.StartExercise;

public class ExerciseDrawerActivity  extends AppCompatActivity  {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_nav_drawer);

        drawerLayout = (DrawerLayout) findViewById(R.id.app_drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = setupDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        navigationView = (NavigationView) findViewById(R.id.app_weather_drawer);
        setupDrawerContent(navigationView);
        displayPhotoFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_menu_start_activity:
                displayStartActivity();
                break;
            case R.id.nav_menu_history:
                displayHistory();
                break;
            case R.id.nav_menu_exercise_setup:
                displayExerciseSetup();
                break;
            case R.id.nav_menu_settings:
                displaySettings();
                break;
            case R.id.nav_menu_backup_restore:
                displayBackupRestore();
                break;
            case R.id.nav_menu_about:
                displayAbout();
                break;

        }

        drawerLayout.closeDrawers();

    }


    private void displayPhotoFragment() {
        Fragment fragment = new PhotoFragment();
        displayFragment(fragment);

    }

    private void displayFragment(Fragment fragment) {
        // Replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().
                replace(R.id.app_frame_layout, fragment).
                commit();
    }

    private void displayStartActivity() {
        Intent intent = new Intent(this, StartExercise.class);
        startActivity(intent);
    }

    private void displayHistory() {
        Intent intent = new Intent(this, ActivityList.class);
        startActivity(intent);
    }

    private void displaySettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void displayExerciseSetup() {
        Intent intent = new Intent(this, ExerciseListActivity.class);
        startActivity(intent);
    }

    private void displayBackupRestore() {
        Intent intent = new Intent(this, BackupRestoreActivity.class);
        startActivity(intent);
    }

    private void displayAbout() {
        Intent intent = new Intent(this, AboutActivityTracker.class);
        startActivity(intent);
    }


}