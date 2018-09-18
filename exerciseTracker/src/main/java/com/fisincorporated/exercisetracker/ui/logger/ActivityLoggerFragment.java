package com.fisincorporated.exercisetracker.ui.logger;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
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
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.stats.StatsArrayAdapter;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

//Removed service logic and updated to location logic from Android Programming - The Big Nerd Ranch Guide

public class ActivityLoggerFragment extends ExerciseDaggerFragment {

    private static final String TAG = ActivityLoggerFragment.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 234240;
    private static final int PERMISSION_REQUEST_CAMERA = 785352;

    private View layoutView;
    private ListView statsList;
    private StatsArrayAdapter statsArrayAdapter;
    private MenuItem cameraMenuItem;
    private Button btnStopRestart;

    private String exrcsLocation;
    private String exercise;
    private String description;
    private ArrayList<String[]> stats = new ArrayList<>();

    private Disposable publishRelayDisposable;

    protected LocationExerciseRecord ler;

    @Inject
    StatsUtil statsUtil;

    @Inject
    GPSLocationManager gpsLocationManager;

    @Inject
    PublishRelay<Object> publishRelay;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;


    private Observer<Object> publishRelayObserver = new Observer<Object>() {
        @Override
        public void onSubscribe(Disposable disposable) {
            publishRelayDisposable = disposable;
        }

        @Override
        public void onNext(Object o) {
            if (o instanceof  LocationExerciseRecord) {
                if (isVisible())
                    displayActivityStats((LocationExerciseRecord) o);
            }
            if (o instanceof NeedLocationPermission) {
                checkLocationPermission();
            }
        }

        @Override
        public void onError(Throwable e) {
            // Big Trouble - PublishRelay should never throw
            Log.e(TAG, "PublishRelay throwing error:" + e.toString());
            // TODO Do something more
        }

        @Override
        public void onComplete() {
            // Big Trouble - PublishRelay should never call
            Log.e(TAG, "PublishRelay onComplete Thrown");
            // TODO Do something more
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
        layoutView = inflater.inflate(R.layout.activity_stats, container, false);
        getReferencedViews(layoutView);
        setHasOptionsMenu(true);
        return layoutView;
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
        publishRelay.subscribe(publishRelayObserver);
    }

    @Override
    public void onStop() {
        if (publishRelayDisposable != null) {
            publishRelayDisposable.dispose();
        }
        publishRelayDisposable = null;
        super.onStop();
    }

    private void getCurrentLer() {
        LocationExerciseDAO leDAO = trackerDatabaseHelper.getLocationExerciseDAO();
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
            if (btnStopRestart.getText().equals(getResources().getString(R.string.stop))) {
                gpsLocationManager.stopTrackingLer();
                Toast.makeText(getActivity(), "Stopping GPS logging",
                        Toast.LENGTH_SHORT).show();
                checkStopRestartButton();
                startBackups();
            } else {
                checkLocationPermission();
            }
        });
    }

    private void startBackups() {
        if (sharedPreferences.getBoolean(getString(R.string.drive_backup), false)) {
            BackupScheduler.scheduleBackupJob(getActivity().getApplicationContext(), GlobalValues.BACKUP_TO_DRIVE);
        }
        if (sharedPreferences.getBoolean(getString(R.string.local_backup), false)) {
            BackupScheduler.scheduleBackupJob(getActivity().getApplicationContext(), GlobalValues.BACKUP_TO_LOCAL);
        }
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
               checkCameraPermission();
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
        statsUtil.formatActivityStats(stats, ler, true, false);
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

    private void startTracking() {
        gpsLocationManager.startTrackingLer(ler);
        checkStopRestartButton();
    }

    private void checkLocationPermission() {
        // Check if the Location permission has been granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startTracking();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    private void checkCameraPermission(){
        // Check if the Location permission has been granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraApp();
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission();
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#ACCESS_FINE_LOCATION} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(layoutView, "Location access is required to display to record GPS points.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                // Request the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }).show();

        } else {
            Snackbar.make(layoutView,
                    "Permission is not available. Requesting location permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(layoutView, "Camera permission is required to use the camera.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                        // Request the permission
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSION_REQUEST_CAMERA);
                    }).show();
        } else {
            Snackbar.make(layoutView,
                    "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                // Permission request was denied.
                Snackbar.make(layoutView, "Location permission request was denied.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraApp();
            } else {
                // Permission request was denied.
                Snackbar.make(layoutView, "Camera permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
