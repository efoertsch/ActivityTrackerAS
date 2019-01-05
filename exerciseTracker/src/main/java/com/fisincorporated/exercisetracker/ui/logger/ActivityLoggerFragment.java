package com.fisincorporated.exercisetracker.ui.logger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import com.fisincorporated.exercisetracker.BuildConfig;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.application.AppPreferences;
import com.fisincorporated.exercisetracker.backupandrestore.BackupScheduler;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.stats.StatsArrayAdapter;
import com.fisincorporated.exercisetracker.utility.StatsUtil;

import java.util.ArrayList;

import javax.inject.Inject;

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

    protected LocationExerciseRecord ler;
    private Bundle bundle;

    @Inject
    StatsUtil statsUtil;

    @Inject
    AppPreferences appPreferences;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private LocationUpdateReceiver locationUpdateReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService service = null;

    // Tracks the bound state of the service.
    private boolean bound = false;

    // Used to be able to call service methods
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) iBinder;
            service = binder.getService();
            service.requestLocationUpdates();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            service = null;
            bound = false;
            setStopContinueButton(false);
        }
    };

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class LocationUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ler = intent.getParcelableExtra(TrackerDatabase.LocationExercise.LOCATION_EXERCISE_TABLE);
            displayActivityStats();
        }
    }

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
        locationUpdateReceiver = new LocationUpdateReceiver();
        bundle = lookForArguments(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        if (!haveLocationPermission()) {
            requestLocationPermission();
        }
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
        if (bundle != null) {
            ler = bundle.getParcelable(TrackerDatabase.LocationExercise.LOCATION_EXERCISE_TABLE);
            // exerciseRowId = ler.get_id();
            exercise = bundle.getString(TrackerDatabase.Exercise.EXERCISE);
            // locationRowid = ler.getLocationId();
            exrcsLocation = bundle.getString(TrackerDatabase.ExrcsLocation.LOCATION);
            description = bundle.getString(TrackerDatabase.LocationExercise.DESCRIPTION);
            if (description == null)
                description = "";
        }
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
    public void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        if (shouldBeTrackingLocation()) {
            startLocationUpdateService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ler != null) {
            // may be coming back from showing map so make sure ler is most current
            getCurrentLer();
            displayActivityStats();
        }
        if (shouldBeTrackingLocation()) {
            registerLocationUpdatesServiceReceiver();
        }
    }



    @Override
    public void onPause() {
        unregisterLocationUpdatesServiceReceiver();
        super.onPause();
    }

    @Override
    public void onStop() {
        // Service not stopped unless/until user hits stop button
        if (bound) {
            // Still tracking
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
    }

    private void registerLocationUpdatesServiceReceiver() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationUpdateReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    private void unregisterLocationUpdatesServiceReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(locationUpdateReceiver);
    }

    private void startLocationUpdateService() {
        // use this to start and trigger a service
        Intent intent = new Intent(getActivity(), LocationUpdatesService.class);
        intent.putExtra(GlobalValues.BUNDLE, bundle);
        boolean goodRequest = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "Good bind request:" + goodRequest);
        setStopContinueButton(true);

    }

    private void getCurrentLer() {
        LocationExerciseDAO leDAO = trackerDatabaseHelper.getLocationExerciseDAO();
        ler = leDAO.loadLocationExerciseRecordById(ler.get_id());
    }

    private void getReferencedViews(View view) {
        TextView tvExerciseLocation = view
                .findViewById(R.id.activity_detail_tvExerciseLocation);
        tvExerciseLocation.setText(getString(R.string.exercise_at_location_plus_description, exercise, exrcsLocation, description));
        View buttonView = view.findViewById(R.id.activity_detail_buttonfooter);
        buttonView.setVisibility(View.VISIBLE);
        statsArrayAdapter = new StatsArrayAdapter(getActivity(),
                stats.toArray(new String[][]{}));
        statsList = view.findViewById(R.id.activity_detail_list);
        statsList.setAdapter(statsArrayAdapter);

        btnStopRestart = view
                .findViewById(R.id.activity_stats_stop_restart);
        btnStopRestart.setOnClickListener(v -> {
            if (shouldBeTrackingLocation()) {
                stopService();
                Snackbar.make(layoutView, R.string.stopping_gps_logging,
                        Snackbar.LENGTH_SHORT).show();
                startBackups();
            } else {
                checkLocationPermission();
            }
        });
    }

    /**
     * Use to indicate if app should be tracking (button text = Stop) location
     * or if tracking stopped (button text = Continue)
     * @return
     */
    private boolean shouldBeTrackingLocation() {
        return btnStopRestart.getText().equals(getResources().getString(R.string.stop));
    }

    /**
     * Call when user clicks on stop button to stop logging
     * Stop the location service
     */
    private void stopService() {
        setStopContinueButton(false);
        service.removeLocationUpdates();
    }

    private void startBackups() {
        if (appPreferences.doBackToDrive()) {
            BackupScheduler.scheduleBackupJob(getActivity().getApplicationContext(), GlobalValues.BACKUP_TO_DRIVE);
        }
        if (appPreferences.doLocalBackup()) {
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
                if (ler != null && ler.getStartLatitude() != null) {
                    args.putLong(TrackerDatabase.LocationExercise._ID, ler.get_id());
                    args.putString(GlobalValues.TITLE, getString(R.string.exercise_at_location, exercise, exrcsLocation));
                    args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, description);
                    args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
                    Snackbar.make(layoutView, getString(R.string.displaying_the_map_may_take_a_moment)
                            , Snackbar.LENGTH_SHORT)
                            .show();
                    callBacks.onSelectedAction(args);
                } else {
                    Snackbar.make(layoutView,
                            R.string.gps_has_not_provided_a_location_yet,
                            Snackbar.LENGTH_LONG).show();
                }

                return true;
            case R.id.activity_detail_showChart:
                args.putLong(TrackerDatabase.LocationExercise._ID, ler.get_id());
                args.putString(GlobalValues.TITLE, getString(R.string.exercise_at_location, exercise, exrcsLocation));
                args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, description);
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

    public void displayActivityStats() {
        if (ler != null && ler.getStartAltitude() != null) {
            statsArrayAdapter.clear();
            formatLerStarts(ler);
            statsArrayAdapter.resetValues(stats.toArray(new String[][]{}));
            statsList.postInvalidate();
            statsList.refreshDrawableState();
        } else {
            Snackbar.make(layoutView,
                    R.string.location_not_yet_available, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void formatLerStarts(LocationExerciseRecord ler) {
        statsUtil.formatActivityStats(stats, ler, true, false);
    }

    private void setStopContinueButton(boolean isStop) {
        if (isStop) {
            btnStopRestart.setText(getResources()
                    .getString(R.string.stop));
            displayCameraMenuIcon(true);
        } else {
            // set to continue
            displayCameraMenuIcon(false);
            btnStopRestart.setText(R.string.continuex);
        }
    }

    private void displayCameraMenuIcon(boolean visible) {
        if (cameraMenuItem != null) {
            cameraMenuItem.setVisible(visible);
        }
    }



    /**
     * Returns the current state of the permissions needed.
     */
    private boolean haveLocationPermission() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void checkLocationPermission() {
        // Check if the Location permission has been granted
        if (haveLocationPermission()) {
            if (bound) {
                service.requestLocationUpdates();
                setStopContinueButton(true);
            } else {
                startLocationUpdateService();
            }
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    private void checkCameraPermission() {
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
            Snackbar.make(layoutView, R.string.location_permission_is_required,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                // Request the permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }).show();

        } else {
            Snackbar.make(layoutView,
                    R.string.location_permission_not_available,
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(layoutView, R.string.camera_permission_is_required,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                // Request the permission
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);
            }).show();
        } else {
            Snackbar.make(layoutView,
                    R.string.camera_permission_is_not_available,
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdateService();
            } else {
                // Permission request was denied.
//                Snackbar.make(layoutView, "Location permission request was denied.",
//                        Snackbar.LENGTH_SHORT).show();
                Snackbar.make(
                        layoutView,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraApp();
            } else {
                // Permission request was denied.
                Snackbar.make(layoutView, R.string.camera_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
