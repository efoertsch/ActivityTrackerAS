package com.fisincorporated.exercisetracker.ui.maps;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.ExrcsLocationDAO;
import com.fisincorporated.exercisetracker.database.ExrcsLocationRecord;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.media.MediaDetail;
import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;


//TODO replace CursorLoader
public class ActivityMapFragment extends ExerciseMasterFragment implements LoaderCallbacks<Cursor>,
        MapRoute.ActivityPhotosCallback {
    public static final String USE_CURRENT_LOCATION_LABEL = "ActivityMapFragment.CURRENT_LOCATION_LABEL";
    private static final int DELETE_REQUESTCODE = 1;
    private LocationExerciseRecord ler = null;
    private LocationExerciseDAO leDAO = null;
    private ExerciseRecord er = null;
    private ExerciseDAO eDAO = null;
    private ExrcsLocationRecord elr = null;
    private ExrcsLocationDAO elDAO = null;
    private long locationExerciseId;
    private String activityTitle = GlobalValues.UNDEFINED;
    private String title = "";
    private String description = "";

    private boolean useCurrentLocationLabel = false;

    private int deleteDetailType;
    private static final int FOR_MAP_PLOT = 1;
    private static final int FOR_KML_FILE = 2;
    private int logicPath;

    private TextView tvInfo;
    private GoogleMap map;
    //private MapFragment mapFragment = null;
    private SupportMapFragment supportMapFragment = null;
    private MapRoute mapRoute;
    private ArrayList<MediaDetail> mediaDetails;
    // maptype must be GoogleMap.MAP_TYPE_HYBRID, _SATELLITE, ...
    private int mapType;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Cursor cursor;

    @Inject
    MapRoute.Builder mapRouteBuilder;

    @Inject
    KmlWriter.Builder kmlWriterBuilder;


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
        View view = inflater.inflate(R.layout.map_layout_wo_map_fragment,
                container, false);
        // Can only have one instance of SupportMapFragment so make sure
        // singleton (which newInstance is doing)
        supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.mapFragmentContainer, supportMapFragment);
        fragmentTransaction.commit();
        setHasOptionsMenu(true);
        getReferencedViews(view);
        logicPath = FOR_MAP_PLOT;
        createMap();
        return view;
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
    public void onResume() {
        super.onResume();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == DELETE_REQUESTCODE) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                doPositiveCancelClick();
            }
        }
    }

    public void doPositiveCancelClick() {
        GPSLogDAO gpslogDAO = new GPSLogDAO();
        gpslogDAO.deleteGPSLogbyLerRowId(ler.get_id());
        Toast.makeText(getActivity(), "GPS log detail deleted.",
                Toast.LENGTH_SHORT).show();
        if (deleteDetailType == 1) {
            leDAO = new LocationExerciseDAO();
            leDAO.deleteLocationExercise(ler);
            Toast.makeText(getActivity(), "Activity deleted.", Toast.LENGTH_SHORT)
                    .show();
            getFragmentManager().popBackStack();
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
        Single<LocationExerciseRecord> lerSingleObservable = TrackerDatabaseHelper.getLerSingleObservable(locationExerciseId);
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

    private void loadExerciseInfo() {
        leDAO = new LocationExerciseDAO();
        ler = leDAO.loadLocationExerciseRecordById(locationExerciseId);
        if (ler.get_id() > 0) {
            // Get detail on the type of exercise
            eDAO = new ExerciseDAO();
            er = eDAO.loadExerciseRecordById(ler.getExerciseId());
            // Get the location of the exercise
            elDAO = new ExrcsLocationDAO();
            elr = elDAO.loadExrcsLocationRecordById(ler.getLocationId());
        } else {
            Toast.makeText(
                    getActivity(),
                    "Valid activity record not found with id =" + locationExerciseId,
                    Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }

        if (ler.getStartTimestamp() == null) {
            Toast.makeText(getActivity(),
                    "Invalid activity record. Insufficient information to map.",
                    Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }
    }

    private void getReferencedViews(View view) {
        tvInfo = (TextView) view.findViewById(R.id.map_activity_tvInfo);
        tvInfo.setText(activityTitle);
    }

    private void restartCursorLoader() {
        getLoaderManager().restartLoader(GlobalValues.MAP_LOADER, null, this);
    }

    // MapRoute.ActivityPhotosCallback
    @Override
    public void photoList(ArrayList<MediaDetail> mediaDetails) {
        if (mediaDetails != null && mediaDetails.size() > 0) {
            this.mediaDetails = mediaDetails;
        }
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
        MapRoute.Builder builder = new MapRoute.Builder();
        builder.setSupportMapFragment(supportMapFragment)
                .setLocationExerciseRecord(ler)
                .setMapType(mapType)
                .setUseCurrentLocationLabel(useCurrentLocationLabel)
                .setCursor(csr)
                .setTitle(activityTitle)
                .setActivityPhotosCallback(this);
        mapRoute = builder.build();
        mapRoute.plotGpsRoute();
    }

    private void createKmlEmail(Cursor cursor){
        KmlWriter.Builder builder = new KmlWriter.Builder();
        builder.setContext(getContext())
                .setLocationExerciseRecord(ler)
                .setTitle(title)
                .setDescription(description)
                .setCursor(cursor);
        KmlWriter kmlWriter = builder.build();
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
}
