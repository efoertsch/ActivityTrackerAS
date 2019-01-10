package com.fisincorporated.exercisetracker.ui.maps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;


//TODO replace CursorLoader
public class ActivityMapFragment extends ExerciseDaggerFragment implements LoaderCallbacks<Cursor> {
    private static final String TAG = ActivityMapFragment.class.getSimpleName();

    public static final String USE_CURRENT_LOCATION_LABEL = "ActivityMapFragment.CURRENT_LOCATION_LABEL";
    private static final int PERMISSION_REQUEST_STORAGE = 2121;

    private LocationExerciseRecord ler = null;
    private long locationExerciseId;
    private String activityTitle = GlobalValues.UNDEFINED;
    private String title = "";
    private String description = "";

    private boolean useCurrentLocationLabel = false;

    private static final int FOR_MAP_PLOT = 1;
    private static final int FOR_KML_FILE = 2;
    private int logicPath;

    private View layoutView;
    private TextView tvInfo;
    private SupportMapFragment supportMapFragment = null;
    private MapRoute mapRoute;

    // maptype must be GoogleMap.MAP_TYPE_HYBRID, _SATELLITE, ...
    private int mapType;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Cursor cursor;

    @Inject
    DisplayUnits displayUnits;

    @Inject
    PhotoUtils photoUtils;

    @Inject
    StatsUtil statsUtil;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;
    private boolean displayPhotoPoints = true;


    public static ActivityMapFragment newInstance(Bundle bundle) {
        ActivityMapFragment fragment = new ActivityMapFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        getArgumentBundle();
        getDatabaseSetup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        layoutView = inflater.inflate(R.layout.map_layout_wo_map_fragment,
                container, false);
        // Can only have one instance of SupportMapFragment so make sure
        // singleton (which newInstance is doing)
        supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.mapFragmentContainer, supportMapFragment);
        fragmentTransaction.commit();
        setHasOptionsMenu(true);
        getReferencedViews(layoutView);
        logicPath = FOR_MAP_PLOT;
        checkStoragePermission();
        return layoutView;
    }

    // TODO reduce info passed in bundle
    private void getArgumentBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
            title = bundle.getString(GlobalValues.TITLE);
            description = bundle.getString(LocationExercise.DESCRIPTION);
            activityTitle = title + "  " + description;
            useCurrentLocationLabel = bundle.getBoolean(USE_CURRENT_LOCATION_LABEL, false);
        }
    }

    public void createMap(){
        checkForGooglePlayServices();
        if (ler == null) {
            loadLocationExerciseRecord(locationExerciseId);
        } else {
            restartCursorLoader();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        compositeDisposable.dispose();
    }

    @Override
    public void onDestroy(){
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
        if (mapRoute != null) {
            mapRoute.onTerminate();
            mapRoute = null;
        }
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_map_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_map_type_satellite_plus_streets:
                if (mapRoute != null)
                    mapRoute.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.activity_map_type_satellite:
                if (mapRoute != null)
                    mapRoute.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.activity_map_type_normal:
                if (mapRoute != null)
                    mapRoute.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.activity_map_type_terrain:
                if (mapRoute != null)
                    mapRoute.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.activity_map_email:
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.email_being_created),
                        Toast.LENGTH_SHORT).show();
                logicPath = FOR_KML_FILE;
                restartCursorLoader();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkForGooglePlayServices() {
        int GooglePlayAvailableCode;
        GooglePlayAvailableCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS != GooglePlayAvailableCode) {
            Toast.makeText(getActivity(), "GooglePlayServices not available",
                    Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }
    }

    private void loadLocationExerciseRecord(long locationExerciseId){
        compositeDisposable = new CompositeDisposable();
        Single<LocationExerciseRecord> lerSingleObservable = trackerDatabaseHelper.getLerSingleObservable(locationExerciseId);
        compositeDisposable.add(lerSingleObservable.subscribeWith(new DisposableSingleObserver<LocationExerciseRecord>() {
            @Override
            public void onSuccess(LocationExerciseRecord ler) {
                // get related GPS points for map or email
                ActivityMapFragment.this.ler = ler;
                getLoaderManager().initLoader(GlobalValues.MAP_LOADER, null, ActivityMapFragment.this);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getActivity(),
                        getString(R.string.valid_activity_record_not_found_with_id, locationExerciseId),
                        Toast.LENGTH_LONG).show();
                getFragmentManager().popBackStack();
            }
        }));
    }

    private void getReferencedViews(View view) {
        tvInfo = (TextView) view.findViewById(R.id.map_activity_tvInfo);
        tvInfo.setText(activityTitle);
    }

    private void restartCursorLoader() {
        getLoaderManager().restartLoader(GlobalValues.MAP_LOADER, null, this);
    }

    private static class GPSPointsCursorLoader extends SQLiteCursorLoader {
        ActivityMapFragment am;

        public GPSPointsCursorLoader(Context context, ActivityMapFragment am) {
            super(context);
            this.am = am;
        }

        @Override
        protected Cursor loadCursor() {
            return am.getGPSPointsCursor();
        }
    }

    protected Cursor getGPSPointsCursor() {
        if (!database.isOpen()) {
            Log.e(GlobalValues.LOG_TAG,
                    "ActivityMapFragment.getGPSPointsCursor:com.fisincorporated.exercisetracker.database is not open attempting reopen");
        }
        database = databaseHelper.getWritableDatabase();
        if (!database.isOpen()) {
            Log.e(GlobalValues.LOG_TAG,
                    "ActivityMapFragment.getGPSPointsCursor:com.fisincorporated.exercisetracker.database is not open. Big Trouble in Little China");
        }
        csrUtility = database.query(GPSLog.GPSLOG_TABLE, new String[]{
                        GPSLog.LATITUDE, GPSLog.LONGITUDE, GPSLog.ELEVATION, GPSLog.TIMESTAMP},
                GPSLog.LOCATION_EXERCISE_ID + " = ?",
                new String[]{locationExerciseId + ""}, null, null,
               GPSLog.TIMESTAMP + " ASC");
        return csrUtility;
    }

    private void plotRoute(Cursor csr) throws IOException {
        switch (logicPath) {
            case FOR_MAP_PLOT:
                // create MapRouteBuilder and pass cursor
                plotRouteForMap(csr);
                break;
            case FOR_KML_FILE:
                createKmlEmail(csr);
                break;
        }
    }

    private void plotRouteForMap(Cursor csr){
        mapRoute = new MapRoute(displayUnits, photoUtils, statsUtil, trackerDatabaseHelper);
        mapRoute.setSupportMapFragment(supportMapFragment)
                .setLocationExerciseRecord(ler)
                .setMapType(mapType)
                .setUseCurrentLocationLabel(useCurrentLocationLabel)
                .setCursor(csr)
                .setTitle(activityTitle);
        mapRoute.displayPhotoPoints(displayPhotoPoints);
        mapRoute.plotGpsRoute();

    }

    private void createKmlEmail(Cursor cursor){
        KmlWriter kmlWriter = new KmlWriter(statsUtil);
        kmlWriter.setContext(getContext())
                .setLocationExerciseRecord(ler)
                .setTitle(title)
                .setDescription(description)
                .setCursor(cursor);
        kmlWriter.createKmlFileForEmailing();
    }

    // LoaderCallBacks interface methods
    // #1
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // we only ever load the runs, so assume this is the case
        return new GPSPointsCursorLoader(getActivity(), this);
    }

    // #2
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        try {
            plotRoute(cursor);
            this.cursor = cursor;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // #3
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // not sure what is needed here
        // cursor should be handled/closed by LoadManager
    }


    private void checkStoragePermission() {
        // Check if the Location permission has been granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // TODO OK to display photos
            createMap();
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(layoutView, R.string.storage_permission_is_required,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                // Request the permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
            }).addCallback(new Snackbar.Callback(){
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                        displayPhotoPoints = false;
                        createMap();
                    }
                }
            }).show();
        } else {
            Snackbar.make(layoutView,
                    R.string.storage_permission_is_not_available,
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
           requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for storage permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayPhotoPoints = true;
            } else {
                // Permission request was denied.
                Snackbar.make(layoutView, R.string.storage_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
                displayPhotoPoints = false;
            }
            createMap();
        }
    }
}
