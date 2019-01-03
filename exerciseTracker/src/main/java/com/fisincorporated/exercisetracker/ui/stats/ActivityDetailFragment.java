package com.fisincorporated.exercisetracker.ui.stats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.media.slideshow.FullscreenPhotoPagerActivity;
import com.fisincorporated.exercisetracker.ui.media.MediaDetail;
import com.fisincorporated.exercisetracker.ui.media.MediaPoint;
import com.fisincorporated.exercisetracker.ui.media.mediagrid.MediaGridPagerActivity;
import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class ActivityDetailFragment extends ExerciseDaggerFragment {
    private static final int DELETE_REQUESTCODE = 1;
    private TextView tvExerciseLocation = null;
    private LocationExerciseRecord ler = null;
    private LocationExerciseDAO leDAO = null;
    private long locationExerciseId;
    private String title;
    private ArrayList<String[]> stats = new ArrayList<String[]>();
    private int deleteDetailType;
    private String description;

    private ArrayList<MediaDetail> mediaDetails = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FloatingActionButton mapFab;
    private FloatingActionButton photosFab;

    @Inject
    PhotoUtils photoUtils;

    @Inject
    StatsUtil statsUtil;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    /**
     * Pass in the arguments needed by this fragment
     * The bundle must have
     * locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
     * title = bundle.getString(GlobalValues.TITLE);
     * and the title is composed of "Exercise@Location Date"
     *
     * @param bundle
     * @return
     */
    public static ActivityDetailFragment newInstance(Bundle bundle) {
        ActivityDetailFragment fragment = new ActivityDetailFragment();
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
        formatActivityStats();
        StatsArrayAdapter statsArrayAdapter = new StatsArrayAdapter(
                getActivity(), stats.toArray(new String[][]{}));
        ListView statsList = (ListView) view
                .findViewById(R.id.activity_detail_list);
        statsList.setAdapter(statsArrayAdapter);
        getPhotosTakenFromStartToFinish();
        return view;
    }

    private void lookForArguments(Bundle savedInstanceState) {
        Bundle bundle = null;
        if (getArguments() != null) {
            bundle = getArguments();
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(LocationExercise._ID)) {
            bundle = savedInstanceState;
        }
        if (bundle != null) {
            locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
            title = bundle.getString(GlobalValues.TITLE);
            description = bundle.getString(LocationExercise.DESCRIPTION);
            mediaDetails = bundle.getParcelableArrayList(GlobalValues.PHOTO_DETAIL_LIST);
        }

    }

    // save the sort order so if you change orientation you will keep sort order
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(LocationExercise._ID, locationExerciseId);
        savedInstanceState.putString(GlobalValues.TITLE, title);
        savedInstanceState.putString(LocationExercise.DESCRIPTION, description);
        savedInstanceState.putParcelableArrayList(GlobalValues.PHOTO_DETAIL_LIST, mediaDetails);
        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    // above to stop error syntax display on mapFab.setVisibility()
    private void getReferencedViews(View view) {
        tvExerciseLocation =  view
                .findViewById(R.id.activity_detail_tvExerciseLocation);
        mapFab = view.findViewById(R.id.activity_detail_map_fab);
        mapFab.setVisibility(View.VISIBLE);
        mapFab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(LocationExercise._ID, locationExerciseId);
            args.putString(GlobalValues.TITLE, title);
            args.putString(LocationExercise.DESCRIPTION, description);
            args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
            Toast.makeText(getActivity().getBaseContext(),
                    getActivity().getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
                    .show();
            callBacks.onSelectedAction(args);
        });
        photosFab = view.findViewById(R.id.activity_detail_photos_fab);
        photosFab.setOnClickListener(v -> {
            Intent intent;
            if (mediaDetails.size() == 1) {
                FullscreenPhotoPagerActivity.IntentBuilder intentBuilder = FullscreenPhotoPagerActivity.IntentBuilder.getBuilder(getContext());
                intentBuilder.setPhotoDetails(mediaDetails);
                intent = intentBuilder.build();
            } else {
                MediaPoint mediaPoint = MediaPoint.getInstance(0, null);
                mediaPoint.setMediaDetails(mediaDetails);
                ArrayList<MediaPoint> mediaPoints = new ArrayList<>();
                mediaPoints.add(mediaPoint);
                MediaGridPagerActivity.IntentBuilder intentBuilder = MediaGridPagerActivity.IntentBuilder.getBuilder(getContext());
                intentBuilder.setPhotoPoints(mediaPoints).setTitle(title).setPhotoPointPosition(0);
                intent = intentBuilder.build();
            }
            startActivity(intent);
        });
    }

    @Override
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
        if (showMap != null) {
            showMap.setVisible(false);
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        ActivityDialogFragment dialog;
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case R.id.activity_detail_showChart:
                args.putLong(LocationExercise._ID, locationExerciseId);
                args.putString(GlobalValues.TITLE, title);
                args.putString(LocationExercise.DESCRIPTION, description);
                args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_CHART);
                args.putInt(GlobalValues.BAR_CHART_TYPE, GlobalValues.DISTANCE_VS_ELEVATION);
                callBacks.onSelectedAction(args);
                return true;
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
        GPSLogDAO gpslogDAO = trackerDatabaseHelper.getGPSLogDAO();
        gpslogDAO.deleteGPSLogbyLerRowId(ler.get_id());
        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.gps_log_detail_deleted),
                Toast.LENGTH_SHORT).show();
        if (deleteDetailType == 1) {
            leDAO = trackerDatabaseHelper.getLocationExerciseDAO();
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
        leDAO =trackerDatabaseHelper.getLocationExerciseDAO();
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
        statsUtil.formatActivityStats(stats, ler,false, false);
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

    @Override
    public void onDestroy() {
        Log.i(GlobalValues.LOG_TAG, "ActivityDetailFragment.onDestroy. Has close error occurred yet?");
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void getPhotosTakenFromStartToFinish() {
        long startTimeLong;
        long endTimeLong;
        startTimeLong = ler.getStartTimestamp().getTime();
        endTimeLong = ler.getEndTimestamp().getTime();
        getPhotosTaken(getContext(), startTimeLong, endTimeLong);
    }

    private void getPhotosTaken(Context context, long startTime, long endTime) {
        compositeDisposable.add(photoUtils.getMediaListObservable(context, startTime, endTime)
                .onErrorReturn(throwable -> {
                            Toast.makeText(context, R.string.error_get_photos_for_activity, Toast.LENGTH_LONG).show();
                            return new ArrayList<>();
                        }
                )
                .subscribe(photoList -> {
                            displayPhotoFab(photoList);
                        },
                        throwable -> {
                            Toast.makeText(context, R.string.error_get_photos_for_activity, Toast.LENGTH_LONG).show();
                        }));
    }

    @SuppressLint("RestrictedApi")
    // https://stackoverflow.com/questions/50343634/android-p-visibilityawareimagebutton-setvisibility-can-only-be-called-from-the-s
    private void displayPhotoFab(ArrayList<MediaDetail> photoList) {
        mediaDetails = photoList;
        if (mediaDetails != null && mediaDetails.size() > 0) {
            photosFab.setVisibility(View.VISIBLE);
        }

    }

}
