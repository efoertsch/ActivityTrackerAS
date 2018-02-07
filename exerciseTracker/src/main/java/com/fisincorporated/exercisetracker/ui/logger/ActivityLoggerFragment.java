package com.fisincorporated.exercisetracker.ui.logger;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.backupandrestore.BackupScheduler;
import com.fisincorporated.exercisetracker.broadcastreceiver.UpdateLerReceiver;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.stats.StatsArrayAdapter;
import com.fisincorporated.exercisetracker.utility.Utility;

import java.util.ArrayList;

import javax.inject.Inject;

//Removed service logic and updated to location logic from Android Programming - The Big Nerd Ranch Guide

public class ActivityLoggerFragment extends ExerciseMasterFragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    ListView statsList = null;
    StatsArrayAdapter statsArrayAdapter = null;

    private Button btnStopRestart = null;

    private String exrcsLocation;
    private String exercise;
    private String description;

    private ArrayList<String[]> stats = new ArrayList<>();

    protected LocationExerciseRecord ler = null;

    private MenuItem cameraMenuItem;

    private GPSLocationManager gpsLocationManager = null;

    @Inject
    Utility utility;

    private UpdateLerReceiver updateLerReceiver = new UpdateLerReceiver() {
        @Override
        protected void onLerUpdate(LocationExerciseRecord ler) {
            if (isVisible())
                displayActivityStats(ler);
        }

    };

    public static ActivityLoggerFragment newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        args.putBundle(GlobalValues.BUNDLE, bundle);
        ActivityLoggerFragment fragment = new ActivityLoggerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = lookForArguments(savedInstanceState);
        gpsLocationManager = GPSLocationManager.get(getActivity());
        // Created 2nd call for notification as passing in the get() method above would cause
        // problem in LocationReceiver.onLocationReceived() with passes just context
        gpsLocationManager.setNotification(getActivity(), exercise, exrcsLocation);
        GPSLocationManager.setActivityDetails(bundle);
        gpsLocationManager.startNewLer(ler);
        setHasOptionsMenu(true);
    }

    private Bundle lookForArguments(Bundle savedInstanceState) {
        Bundle bundle = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(GlobalValues.BUNDLE)) {
                bundle = getArguments().getBundle(GlobalValues.BUNDLE);
            }
        } else if (savedInstanceState != null) {
            bundle = savedInstanceState;
        }
        ler = bundle.getParcelable(LocationExercise.LOCATION_EXERCISE_TABLE);
        // exerciseRowId = ler.get_id();
        exercise = bundle.getString(Exercise.EXERCISE);
        // locationRowid = ler.getLocationId();
        exrcsLocation = bundle.getString(ExrcsLocation.LOCATION);
        description = bundle.getString(LocationExercise.DESCRIPTION);
        if (description == null)
            description = "";
        return bundle;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_stats, container, false);
        getReferencedViews(view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ler.get_id() != -1) {
            // may be coming back from showing map so make sure ler is most current
            getCurrentLer();
            displayActivityStats(ler);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(updateLerReceiver,
                new IntentFilter(GPSLocationManager.LER_UPDATE));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(updateLerReceiver);
        super.onStop();
    }


    private void getCurrentLer() {
        LocationExerciseDAO leDAO = new LocationExerciseDAO();
        ler = leDAO.loadLocationExerciseRecordById(ler.get_id());
    }

    private void getReferencedViews(View view) {
        TextView tvExerciseLocation = (TextView) view
                .findViewById(R.id.activity_detail_tvExerciseLocation);
        tvExerciseLocation.setText(getString(R.string.exercise_at_location_plus_description, exercise, exrcsLocation, description));
        View buttonView = view.findViewById(R.id.activity_detail_buttonfooter);
        buttonView.setVisibility(View.VISIBLE);
        // stats.add(new String[] {"xxxx", "yyyy"});
        statsArrayAdapter = new StatsArrayAdapter(getActivity(),
                stats.toArray(new String[][]{}));
        statsList = (ListView) view.findViewById(R.id.activity_detail_list);
        statsList.setAdapter(statsArrayAdapter);

        btnStopRestart = (Button) view
                .findViewById(R.id.activity_stats_stop_restart);
        btnStopRestart.setOnClickListener(v -> {
            if (btnStopRestart.getText().equals(
                    getResources().getString(R.string.stop))) {

                // gpsLocationManager.stopLocationUpdates();
                gpsLocationManager.stopTrackingLer();

                Toast.makeText(getActivity(), "Stopping GPS logging",
                        Toast.LENGTH_SHORT).show();
                checkStopRestartButton();
                startBackups();
            } else {
                // gpsLocationManager.startLocationUpdates();
                gpsLocationManager.startTrackingLer(ler);
                Toast.makeText(getActivity(), "Continuing GPS logging",
                        Toast.LENGTH_SHORT).show();
                checkStopRestartButton();
            }
        });
    }

    //TODO come up with better backup strategy
    private void startBackups() {
        BackupScheduler.scheduleBackupJob(getActivity().getApplicationContext(), GlobalValues.BACKUP_TO_DRIVE);
        BackupScheduler.scheduleBackupJob(getActivity().getApplicationContext(), GlobalValues.BACKUP_TO_LOCAL);
    }

    // Note this is called after onResume() (Seems odd time to call it)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_logger_camera, menu);
        inflater.inflate(R.menu.activity_detail_for_tablet, menu);
        cameraMenuItem = menu.findItem(R.id.activity_logger_camera);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case R.id.activity_detail_showMap:
                if (ler.getStartLatitude() != null) {
                    args.putLong(LocationExercise._ID, ler.get_id());
                    args.putString(GlobalValues.TITLE, getString(R.string.exercise_at_location, exercise, exrcsLocation));
                    args.putString(LocationExercise.DESCRIPTION, description);
                    args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
                    Toast.makeText(getActivity().getBaseContext(),
                            getActivity().getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
                            .show();
                    callBacks.onSelectedAction(args);
                } else {
                    Toast.makeText(
                            getActivity(),
                            "The GPS hasn't provided a location yet. Please wait a couple more seconds",
                            Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.activity_detail_showChart:
                args.putLong(LocationExercise._ID, ler.get_id());
                args.putString(GlobalValues.TITLE, getString(R.string.exercise_at_location, exercise, exrcsLocation));
                args.putString(LocationExercise.DESCRIPTION, description);
                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_CHART);
                args.putInt(GlobalValues.BAR_CHART_TYPE, GlobalValues.DISTANCE_VS_ELEVATION);
                callBacks.onSelectedAction(args);
                return true;

            case R.id.activity_logger_camera:

                startCameraApp();
                return true;

            default:
                return false;
        }
    }

    private void startCameraApp() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void displayActivityStats(LocationExerciseRecord ler) {
        this.ler = ler;
        if (ler != null && ler.getStartAltitude() != null) {
            statsArrayAdapter.clear();
            formatLerStarts(ler);
            statsArrayAdapter.resetValues(stats.toArray(new String[][]{}));
            statsList.postInvalidate();
            statsList.refreshDrawableState();
        } else {
            Toast.makeText(getActivity(),
                    "Location not yet available. Please wait.", Toast.LENGTH_LONG)
                    .show();
        }
        checkStopRestartButton();
    }

    private void formatLerStarts(LocationExerciseRecord ler) {
        utility.formatActivityStats(getActivity(), stats, ler);
    }

    private void checkStopRestartButton() {
        if (gpsLocationManager == null)
            return;
        boolean started = gpsLocationManager.isTrackingLer();
        if (started) {
            btnStopRestart.setText(getResources()
                    .getString(R.string.stop));
            displayCameraMenuIcon(true);
        } else {
            // why does 'continue' for string resource name give an error?
            btnStopRestart.setText(getResources().getString(
                    R.string.continuex));
            displayCameraMenuIcon(false);
        }
    }

    private void displayCameraMenuIcon(boolean visible) {
        if (cameraMenuItem != null) {
            cameraMenuItem.setVisible(visible);
        }
    }

}
