package com.fisincorporated.ExerciseTracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import com.fisincorporated.broadcastreceiver.UpdateLerReceiver;
import com.fisincorporated.database.ExerciseDAO;
import com.fisincorporated.database.GPSLogDAO;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.LocationExerciseRecord;
import com.fisincorporated.database.TrackerDatabase.Exercise;
import com.fisincorporated.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;

import java.util.ArrayList;

//Removed service logic and updated to location logic from Android Programming - The Big Nerd Ranch Guide

public class ActivityLoggerFragment extends ExerciseMasterFragment {

    private static final int CANCEL_REQUESTCODE = 0;
    private TextView tvExerciseLocation = null;
    ListView statsList = null;
    StatsArrayAdapter statsArrayAdapter = null;

    private Button btnStopRestart = null;
    //private Button btnShowMap = null;
    private Button btnCancel = null;

    private String mExrcsLocation;
    private String mExercise;
    private String mDescription;

    private ArrayList<String[]> mStats = new ArrayList<String[]>();

    protected LocationExerciseRecord mLer = null;
    private LocationExerciseDAO mLeDAO = null;
    private ExerciseDAO mExerciseDAO = null;

    private GPSLocationManager mGpsLocationManager = null;
    private UpdateLerReceiver mUpdateLerReceiver = new UpdateLerReceiver() {
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
        mGpsLocationManager = GPSLocationManager.get(getActivity());
        // Created 2nd call for notification as passing in the get() method above would cause
        // problem in LocationReceiver.onLocationReceived() with passes just context
        mGpsLocationManager.setNotification(getActivity(), mExercise, mExrcsLocation);
        GPSLocationManager.setActivityDetails(bundle);
        findDisplayUnits();
        mGpsLocationManager.startNewLer(mLer);
    }

    private Bundle lookForArguments(Bundle savedInstanceState) {
        Bundle bundle = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(GlobalValues.BUNDLE)) {
                bundle = getArguments().getBundle(GlobalValues.BUNDLE);
            }
            ;
        } else if (savedInstanceState != null) {
            bundle = savedInstanceState;
        }
        mLer = (LocationExerciseRecord) bundle
                .getParcelable(LocationExercise.LOCATION_EXERCISE_TABLE);
        // exerciseRowId = ler.get_id();
        mExercise = bundle.getString(Exercise.EXERCISE);
        // locationRowid = ler.getLocationId();
        mExrcsLocation = bundle.getString(ExrcsLocation.LOCATION);
        mDescription = bundle.getString(LocationExercise.DESCRIPTION);
        if (mDescription == null)
            mDescription = "";
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
        if (mLer.get_id() != -1) {
            // may be coming back from showing map so make sure ler is most current
            getCurrentLer();
            displayActivityStats(mLer);
        }

    }

    private void getCurrentLer() {
        mLeDAO = new LocationExerciseDAO(databaseHelper);
        mLer = mLeDAO.loadLocationExerciseRecordById(mLer.get_id());
    }

    private void getReferencedViews(View view) {
        tvExerciseLocation = (TextView) view
                .findViewById(R.id.activity_detail_tvExerciseLocation);
        tvExerciseLocation.setText(mExercise + "@" + mExrcsLocation + " "
                + mDescription);

        View buttonView = (View) view.findViewById(R.id.buttonfooter);
        buttonView.setVisibility(View.VISIBLE);
        // mStats.add(new String[] {"xxxx", "yyyy"});
        statsArrayAdapter = new StatsArrayAdapter(getActivity(),
                mStats.toArray(new String[][]{}));
        statsList = (ListView) view.findViewById(R.id.activity_detail_list);
        statsList.setAdapter(statsArrayAdapter);

        btnStopRestart = (Button) view
                .findViewById(R.id.activity_stats_stop_restart);
        btnStopRestart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btnStopRestart.getText().equals(
                        getResources().getString(R.string.stop))) {

                    // mGpsLocationManager.stopLocationUpdates();
                    mGpsLocationManager.stopTrackingLer();

                    Toast.makeText(getActivity(), "Stopping GPS logging",
                            Toast.LENGTH_SHORT).show();
                    checkStopRestartButton();
                } else {
                    // mGpsLocationManager.startLocationUpdates();
                    mGpsLocationManager.startTrackingLer(mLer);
                    Toast.makeText(getActivity(), "Continuing GPS logging",
                            Toast.LENGTH_SHORT).show();
                    checkStopRestartButton();
                }
            }
        });
        btnCancel = (Button) view.findViewById(R.id.activity_stats_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                        R.string.confirm_cancel, R.string.yes, R.string.no, -1);
                dialog.setTargetFragment(ActivityLoggerFragment.this,
                        CANCEL_REQUESTCODE);
                dialog.show(getActivity().getSupportFragmentManager(),
                        "confirmDialog");
            }

        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // 11 = Honeycomb and using this to assume tablet
        if (android.os.Build.VERSION.SDK_INT < 11) {
            inflater.inflate(R.menu.activity_detail, menu);
        } else {
            inflater.inflate(R.menu.activity_detail_for_tablet, menu);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        ActivityDialogFragment dialog;
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case R.id.activity_detail_showMap:
                if (mLer.getStartLatitude() != null) {
                    args.putLong(LocationExercise._ID, mLer.get_id());
                    args.putString(GlobalValues.TITLE, mExercise + "@" + mExrcsLocation);
                    args.putString(LocationExercise.DESCRIPTION, mDescription);
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
                args.putLong(LocationExercise._ID, mLer.get_id());
                args.putString(GlobalValues.TITLE, mExercise + "@" + mExrcsLocation);
                args.putString(LocationExercise.DESCRIPTION, mDescription);
                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_CHART);
                args.putInt(GlobalValues.BAR_CHART_TYPE, GlobalValues.DISTANCE_VS_ELEVATION);
                callBacks.onSelectedAction(args);
                return true;
            default:
                return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == CANCEL_REQUESTCODE) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                mGpsLocationManager.stopTrackingLer();
                GPSLogDAO gpslogDAO = new GPSLogDAO(databaseHelper);
                database.beginTransaction();
                try {
                    gpslogDAO.deleteGPSLogbyLerRowId(mLer.get_id());
                    mLeDAO = new LocationExerciseDAO(databaseHelper);
                    mLeDAO.deleteLocationExercise(mLer);
                    mExerciseDAO = new ExerciseDAO(databaseHelper);
                    mExerciseDAO.updateTimesUsed(mLer.get_id(), -1);
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
                getActivity().finish();
            } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                return;
            }
        }
    }

    public void displayActivityStats(LocationExerciseRecord ler) {
        // UI updates must be on UI thread of course
        // update the instance copy
        mLer = ler;
        if (ler != null && ler.getStartAltitude() != null) {
            statsArrayAdapter.clear();
            formatLerStarts(ler);
            statsArrayAdapter.resetValues(mStats.toArray(new String[][]{}));
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
        Utility.formatActivityStats(getActivity(), mStats, ler, imperialMetric,
                imperial, feetMeters, milesKm, mphKph);
    }

    private void checkStopRestartButton() {
        if (mGpsLocationManager == null)
            return;
        boolean started = mGpsLocationManager.isTrackingLer();
        if (started) {
            btnStopRestart.setText(getResources()
                    .getString(R.string.stop));
        } else {
            // why does 'continue' for string resource name give an error?
            btnStopRestart.setText(getResources().getString(
                    R.string.continuex));
        }
    }

    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mUpdateLerReceiver,
                new IntentFilter(GPSLocationManager.LER_UPDATE));
    }

    public void onStop() {
        getActivity().unregisterReceiver(mUpdateLerReceiver);
        super.onStop();
    }




}
