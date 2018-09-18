package com.fisincorporated.exercisetracker.ui.startup;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.about.AboutActivityTracker;
import com.fisincorporated.exercisetracker.ui.backuprestore.RestoreActivity;
import com.fisincorporated.exercisetracker.ui.history.ActivityHistory;
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseMaintenanceListActivity;
import com.fisincorporated.exercisetracker.ui.settings.SettingsActivity;
import com.fisincorporated.exercisetracker.ui.startactivity.StartExerciseActivity;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class ExerciseDrawerActivity  extends AppCompatActivity  implements EasyPermissions.PermissionCallbacks {

    private static final int RC_LOCATION_CAMERA = 123;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private  NavigationView navigationView;
    private int appVersion = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_nav_drawer);
        drawerLayout = findViewById(R.id.app_drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = setupDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView = findViewById(R.id.app_navigation_drawer);
        if (appVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkRequiredPermissions();
        } else {
            continueWithStartup();
        }


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



    private void checkRequiredPermissions() {
        String[] perms = {Manifest.permission.CAMERA
                , Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            continueWithStartup();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, RC_LOCATION_CAMERA, perms)
                    .setRationale(R.string.camera_and_location_rationale)
                    .setPositiveButtonText(R.string.rationale_ask_ok)
                    .setNegativeButtonText(R.string.quit_app)
                    .build());
            ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
       if (permissionInList(list,Manifest.permission.ACCESS_FINE_LOCATION )){
           continueWithStartup();
       }
    }

    private void displayCanNotProceed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.can_not_use_app_without_location_permission)
                .setPositiveButton(R.string.ok, (dialog, id) -> checkRequiredPermissions())
                .setNegativeButton(R.string.cancel, (dialog, id) -> finish());
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (permissionInList(list,Manifest.permission.ACCESS_FINE_LOCATION )){
            displayCanNotProceed();
        }
    }

    private boolean permissionInList(List<String> permissions, String permission){
        for (int i = 0; i < permissions.size(); i++){
            if (permissions.get(i).equals(permission)){
                return true;
            }
        }
        return false;
    }


    @AfterPermissionGranted(RC_LOCATION_CAMERA)
    private void continueWithStartup() {
        setupDrawerContent(navigationView);
        displayPhotoFragment();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
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
            case R.id.nav_menu_restore:
                displayRestoreActivity();
                break;
            case R.id.nav_menu_about:
                displayAbout();
                break;
        }

        drawerLayout.closeDrawers();
    }

    private void displayPhotoFragment() {
        Fragment fragment = new StartupPhotoFragment();
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
        Intent intent = new Intent(this, StartExerciseActivity.class);
        startActivity(intent);
    }

    private void displayHistory() {
        Intent intent = new Intent(this, ActivityHistory.class);
        startActivity(intent);
    }

    private void displaySettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void displayExerciseSetup() {
        Intent intent = new Intent(this, ExerciseMaintenanceListActivity.class);
        startActivity(intent);
    }

    private void displayRestoreActivity() {
        Intent intent = new Intent(this, RestoreActivity.class);
        startActivity(intent);
    }

    private void displayAbout() {
        Intent intent = new Intent(this, AboutActivityTracker.class);
        startActivity(intent);
    }


}