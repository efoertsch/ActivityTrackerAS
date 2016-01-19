package com.fisincorporated.ExerciseTracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.database.GPSLogDAO;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.LocationExerciseRecord;
import com.fisincorporated.database.TrackerDatabase.GPSLog;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ActivityDetailFragment extends ExerciseMasterFragment {
    private static final int DELETE_REQUESTCODE = 1;
    private TextView tvExerciseLocation = null;
    private LocationExerciseRecord ler = null;
    private LocationExerciseDAO leDAO = null;
    private long locationExerciseId;
    private String title;
    private ArrayList<String[]> stats = new ArrayList<String[]>();
    private int deleteDetailType;
    private String description;

    // Map info from https://developers.google.com/maps/documentation/android/
    public ActivityDetailFragment() {
    }

    /**
     * Pass in the arguments needed by this
     * The bundle must have
     * locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
     * title = bundle.getString(GlobalValues.TITLE);
     * and the title is composed of "Exercise@Location Date"
     *
     * @param bundle
     * @return
     */
    public static ActivityDetailFragment newInstance(Bundle bundle) {
//		Bundle args = new Bundle();
//		args.putLong(LocationExercise._ID,
//				bundle.getLong(LocationExercise._ID, -1));
//		args.putString(GlobalValues.TITLE, bundle.getString(GlobalValues.TITLE));
//		args.putString(LocationExercise.DESCRIPTION, bundle.getString(LocationExercise.DESCRIPTION));
        ActivityDetailFragment fragment = new ActivityDetailFragment();
//		fragment.setArguments(args);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ActivityDetailFragment newInstance() {
        ActivityDetailFragment fragment = new ActivityDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable the option menu for the Fragment
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_stats, container, false);
        getReferencedViews(view);
        lookForArguments(savedInstanceState);
        displayTitle();

        loadActivityRecords();
        findDisplayUnits();
        formatActivityStats();
        StatsArrayAdapter statsArrayAdapter = new StatsArrayAdapter(
                getActivity(), stats.toArray(new String[][]{}));
        ListView statsList = (ListView) view
                .findViewById(R.id.activity_detail_list);
        statsList.setAdapter(statsArrayAdapter);

        return view;
    }


    private void lookForArguments(Bundle savedInstanceState) {
        Bundle bundle = null;
        if (getArguments() != null) {
            bundle = getArguments();
        }
// If fragment destroyed as not needed in FragmentStatePagerAdapter
// but then later recreated, the savedInstanceState will hold info 
        if (savedInstanceState != null && savedInstanceState.containsKey(LocationExercise._ID)) {
            bundle = savedInstanceState;
        }
        if (bundle != null) {
            locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
            title = bundle.getString(GlobalValues.TITLE);
            description = bundle.getString(LocationExercise.DESCRIPTION);
        }

    }

    // save the sort order so if you change orientation you will keep sort order
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(LocationExercise._ID, locationExerciseId);
        savedInstanceState.putString(GlobalValues.TITLE, title);
        savedInstanceState.putString(LocationExercise.DESCRIPTION, description);
        super.onSaveInstanceState(savedInstanceState);

    }


    private void getReferencedViews(View view) {
        tvExerciseLocation = (TextView) view
                .findViewById(R.id.activity_detail_tvExerciseLocation);
        FloatingActionButton fabMap = (FloatingActionButton) view.findViewById(R.id.fabMap);
        fabMap.setVisibility(View.VISIBLE);
        fabMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putLong(LocationExercise._ID, locationExerciseId);
                args.putString(GlobalValues.TITLE, title);
                args.putString(LocationExercise.DESCRIPTION, description);
                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
                Toast.makeText(getActivity().getBaseContext(),
                        getActivity().getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
                        .show();
                callBacks.onSelectedAction(args);
            }
        });

    }

    public void onResume() {
        super.onResume();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // 11 = Honeycomb and using this to assume tablet
        if (android.os.Build.VERSION.SDK_INT < 11) {
            inflater.inflate(R.menu.activity_detail, menu);
        } else {
            inflater.inflate(R.menu.activity_detail_for_tablet, menu);
        }
        MenuItem showMap = menu.findItem(R.id.activity_detail_showMap);
        if (showMap != null){
            showMap.setVisible(false);
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {

        ActivityDialogFragment dialog;
        Bundle args = new Bundle();
        switch (item.getItemId()) {
//            case R.id.activity_detail_showMap:
//                args.putLong(LocationExercise._ID, locationExerciseId);
//                args.putString(GlobalValues.TITLE, title);
//                args.putString(LocationExercise.DESCRIPTION, description);
//                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
//                Toast.makeText(getActivity().getBaseContext(),
//                        getActivity().getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
//                        .show();
//                callBacks.onSelectedAction(args);
//                return true;
            case R.id.activity_detail_showChart:
                args.putLong(LocationExercise._ID, locationExerciseId);
                args.putString(GlobalValues.TITLE, title);
                args.putString(LocationExercise.DESCRIPTION, description);
                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_CHART);
                args.putInt(GlobalValues.BAR_CHART_TYPE, GlobalValues.DISTANCE_VS_ELEVATION);
                callBacks.onSelectedAction(args);
                return true;
//		case R.id.post_to_facebook:
//			args.putString(GlobalValues.TITLE,title);
//			args.putString(LocationExercise.DESCRIPTION, description) ;
//			StringBuilder activityStatsSB = new StringBuilder();
//			Utility.formatActivityStatsForFacebook(getActivity(), activityStatsSB,
//					 ler,  imperialMetric, imperial,   feetMeters , milesKm,  mphKph) ;
//			args.putString(GlobalValues.ACTIVITY_STATS, activityStatsSB.toString());
//			args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_FACEBOOK_TO_POST);
//			callBacks.onSelectedAction(args);
//			return true;
            case R.id.activity_delete_activity:
                deleteDetailType = 1;
                dialog = ActivityDialogFragment.newInstance(-1,
                        R.string.delete_confirmation, R.string.yes, R.string.no, -1);
                dialog.setTargetFragment(ActivityDetailFragment.this,
                        DELETE_REQUESTCODE);
                dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
                return true;
            case R.id.activity_delete_trace:
                deleteDetailType = 2;
                dialog = ActivityDialogFragment.newInstance(-1,
                        R.string.delete_detail_confirmation, R.string.yes, R.string.no,
                        -1);
                dialog.setTargetFragment(ActivityDetailFragment.this,
                        DELETE_REQUESTCODE);
                dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == DELETE_REQUESTCODE) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                doPositiveCancelClick();
            } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                doNegativeCancelClick();
            }
        }
    }

    public void doPositiveCancelClick() {
        GPSLogDAO gpslogDAO = new GPSLogDAO(databaseHelper);
        gpslogDAO.deleteGPSLogbyLerRowId(ler.get_id());
        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.gps_log_detail_deleted),
                Toast.LENGTH_SHORT).show();
        if (deleteDetailType == 1) {
            leDAO = new LocationExerciseDAO(databaseHelper);
            leDAO.deleteLocationExercise(ler);
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.activity_deleted), Toast.LENGTH_SHORT)
                    .show();
            getFragmentManager().popBackStack();
        }
    }

    // continue on as before
    public void doNegativeCancelClick() {
        return;
    }

    /**
     * Get the location (eg. 'Home') exercise (eg. 'Walking') exercise location
     * summary
     */
    private void loadActivityRecords() {
        leDAO = new LocationExerciseDAO(databaseHelper);
        ler = leDAO.loadLocationExerciseRecordById(locationExerciseId);
        if (ler.get_id() < 1) {
            Toast.makeText(
                    getActivity(),
                    getActivity().getResources().getString(R.string.valid_activity_record_not_found_with_id) + locationExerciseId,
                    Toast.LENGTH_LONG).show();
            // finish();
        }
    }

    private void displayTitle() {
        tvExerciseLocation.setText(title + "  " + description);
    }

    public void formatActivityStats() {
        Utility.formatActivityStats(getActivity(), stats, ler, imperialMetric, imperial, feetMeters, milesKm, mphKph);

    }

    public float calcMaxSpeedToPoint(Long lerId) {
        float maxSpeedToPoint = 0;
        float calcedMaxSpeedToPoint = 0;
        Timestamp lastTimestamp = new java.sql.Timestamp(0);
        Timestamp currentTimestamp = new java.sql.Timestamp(0);
        float elapsedTime = 0;
        float distance = 0;

        int i = 0;
        Cursor csr = database.query(GPSLog.GPSLOG_TABLE, new String[]{
                        GPSLog._ID, GPSLog.LATITUDE, GPSLog.LONGITUDE, GPSLog.ELEVATION,
                        GPSLog.TIMESTAMP, GPSLog.DISTANCE_FROM_LAST_POINT},
                " location_exercise_id = ? ", new String[]{"" + lerId}, null,
                null, GPSLog.DEFAULT_SORT_ORDER);

        if (csr.getCount() == 0) {
            return maxSpeedToPoint;
        } else {
            csr.moveToFirst();
            while (!csr.isAfterLast()) {
                if (i == 0) {
                    lastTimestamp = Timestamp.valueOf(csr.getString(csr
                            .getColumnIndex(GPSLog.TIMESTAMP)));
                } else {
                    currentTimestamp = Timestamp.valueOf(csr.getString(csr
                            .getColumnIndex(GPSLog.TIMESTAMP)));
                    // making distance in hours (or fraction of hours)
                    elapsedTime = (((currentTimestamp.getTime() - lastTimestamp
                            .getTime())) / GlobalValues.TIME_TO_FRACTION_HOURS);
                    // making distance in kilometers
                    distance = (csr.getInt(csr
                            .getColumnIndex(GPSLog.DISTANCE_FROM_LAST_POINT))) / 1000f;
                    // so speed in kph
                    calcedMaxSpeedToPoint = distance / elapsedTime;
                    lastTimestamp = currentTimestamp;
                }
                if (calcedMaxSpeedToPoint > maxSpeedToPoint) {
                    maxSpeedToPoint = calcedMaxSpeedToPoint;
                }
                ++i;
                csr.moveToNext();
            }
        }
        csr.close();
        return maxSpeedToPoint;
    }

    public void onDestroy() {
        Log.i(GlobalValues.LOG_TAG, "ActivityDetailFragment.onDestroy. Has close error occurred yet?");
        super.onDestroy();
    }

}
