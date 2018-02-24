package com.fisincorporated.exercisetracker.ui.history;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.filters.ExerciseFilterDialog;
import com.fisincorporated.exercisetracker.ui.filters.LocationFilterDialog;
import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;
import com.fisincorporated.exercisetracker.ui.master.ChangeToolbarEvent;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.master.IHandleSelectedAction;
import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

//TODO replace CursorLoader
//TODO convert to DataBinding

public class ActivityFragmentHistory extends ExerciseDaggerFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ActivityHistorySummary.class.getSimpleName();

    protected static final String EXERCISE_FILTER_DIALOG = "exercise_filter_dialog";
    protected static final String LOCATION_FILTER_DIALOG = "location_filter_dialog";
    protected static final int EXERCISE_REQUESTCODE = 0;
    protected static final int LOCATION_REQUESTCODE = 1;
    private static final int TYPE_OF_DELETE_REQUESTCODE = 3;
    private static final int CONFIRM_DELETE_REQUESTCODE = 4;
    protected static int sortOrder = 6;
    private static int defaultSortOrder = sortOrder;

    protected ArrayList<String> exerciseSelections = new ArrayList<>();
    protected ArrayList<String> locationSelections = new ArrayList<>();
    protected static boolean doRefresh = false;

    protected RecyclerView recyclerView;
    private ActivityHistoryItemAdapter activityHistoryItemAdapter;

    protected Cursor cursor = null;

    // Delete stuff
    private LocationExerciseDAO leDAO = null;
    private GPSLogDAO gpslrDAO = null;

    private int deleteDetailType = 0;

    // TODO convert callbacks to RxJava or use Bus
    private IHandleSelectedAction handleSelectedActionImpl;

    private HashMap<Long, Long> deleteList = new HashMap<>();
    private HashSet<Long> deleteSet = new HashSet<>();

    private MenuItem trashcan;

    private Disposable publishRelayDisposable;

    @Inject
    GPSLocationManager gpsLocationManager;

    @Inject
    PublishRelay<Object> publishRelay;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    private Observer<Object> publishRelayObserver = new Observer<Object>() {
        @Override
        public void onSubscribe(Disposable disposable) {
            publishRelayDisposable = disposable;
        }

        @Override
        public void onNext(Object o) {
            if (o instanceof ActivityHistorySummary) {
                ActivityHistorySummary activityHistorySummary = (ActivityHistorySummary) o;
                switch (activityHistorySummary.getAction()) {
                    case DISPLAY_MAP:
                        displayMap(activityHistorySummary);
                        break;
                    case DISPLAY_STATS:
                        displayStats(activityHistorySummary);
                        break;
                }
                return;
            }
            if (o instanceof DeleteHistoryListEvent) {
                deleteSet = ((DeleteHistoryListEvent) o).getDeleteSet();
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

    public void setHandleSelectedActionImpl(IHandleSelectedAction handleSelectedActionImpl) {
        this.handleSelectedActionImpl = handleSelectedActionImpl;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handleSelectedActionImpl = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            restoreSortFilter(savedInstanceState);
        }
        // initLoader placed here per example at
        // http://developer.android.com/reference/android/app/LoaderManager.html#initLoader(int,%20android.os.Bundle,%20android.app.LoaderManager.LoaderCallbacks
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getActivity().getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreSortFilter(getArguments());
        View view = inflater.inflate(R.layout.activity_history_list, container,
                false);
        recyclerView = (RecyclerView) view.findViewById(R.id.activity_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        activityHistoryItemAdapter = new ActivityHistoryItemAdapter(getContext(), publishRelay);
        recyclerView.setAdapter(activityHistoryItemAdapter);

        return view;
    }

    // Note this is called after onResume() (Seems odd time to call it)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activities_trashcan, menu);
        trashcan = menu.findItem(R.id.activities_history_trashcan);
    }

    @Override
    public void onStart() {
        super.onStart();
        // a bit of a hack. If you return from the delete history screen with no changes the recycler view is empty,
        // so forcing a redraw
        if (recyclerView != null && activityHistoryItemAdapter != null) {
            recyclerView.setAdapter(activityHistoryItemAdapter);
        }
        publishRelay.subscribe(publishRelayObserver);
    }

    public void onResume() {
        super.onResume();
        displayTrashCanIfAnyToBeDeleted();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onStop() {
        if (publishRelayDisposable != null) {
            publishRelayDisposable.dispose();
        }
        publishRelayDisposable = null;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activities_history_trashcan:
                showTypeOfDeleteDialog();
                return true;
        }
        return false;
    }

    // save the sort order so if you change orientation you will keep sort order
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(GlobalValues.SORT_ORDER, sortOrder);
        savedInstanceState.putStringArrayList(
                GlobalValues.EXERCISE_FILTER_PHRASE, exerciseSelections);
        savedInstanceState.putStringArrayList(
                GlobalValues.LOCATION_FILTER_PHRASE, locationSelections);
        long deleteKeysArray[] = createDeleteKeysArray(deleteList);
        savedInstanceState.putLongArray("DELETE_KEYS", deleteKeysArray);
    }

    protected long[] createDeleteKeysArray(HashMap<Long, Long> deleteList) {
        long deleteKeys[] = new long[deleteList.size()];
        ArrayList<Long> deleteKeyList = new ArrayList<>(deleteList.values());
        for (int i = 0; i < deleteKeys.length; ++i) {
            deleteKeys[i] = deleteKeyList.get(i);
        }
        return deleteKeys;
    }

    public void restoreSortFilter(Bundle bundle) {
        if (bundle == null)
            return;
        if (bundle.containsKey(GlobalValues.SORT_ORDER))
            sortOrder = bundle.getInt(GlobalValues.SORT_ORDER, defaultSortOrder);
        if (bundle.containsKey(GlobalValues.LOCATION_FILTER_PHRASE))
            exerciseSelections = bundle
                    .getStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE);
        if (bundle.containsKey(GlobalValues.LOCATION_FILTER_PHRASE))
            locationSelections = bundle
                    .getStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE);
        if (bundle.containsKey("DELETE_KEYS")) {
            long deleteKeysArray[] = bundle.getLongArray("DELETE_KEYS");
            for (int i = 0; i < deleteKeysArray.length; ++i) {
                deleteList.put(deleteKeysArray[i], deleteKeysArray[i]);
            }
        }
    }

    public void showExerciseFilter() {
        ExerciseFilterDialog dialog = ExerciseFilterDialog.newInstance(
                exerciseSelections, getString(R.string.filter_by_exercise));
        dialog.setTargetFragment(ActivityFragmentHistory.this, EXERCISE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(),
                EXERCISE_FILTER_DIALOG);
    }

    public void showLocationFilter() {
        LocationFilterDialog locDialog = LocationFilterDialog.newInstance(
                locationSelections, getString(R.string.filter_by_location));
        locDialog.setTargetFragment(ActivityFragmentHistory.this,
                LOCATION_REQUESTCODE);
        locDialog.show(getActivity().getSupportFragmentManager(),
                LOCATION_FILTER_DIALOG);
    }

    public void clearFilters() {
        exerciseSelections = new ArrayList<>();
        locationSelections = new ArrayList<>();
        getLoaderManager().restartLoader(0, null, this);
    }

    public static class ListCursorLoader extends SQLiteCursorLoader {
        ActivityFragmentHistory alf;

        public ListCursorLoader(Context context, ActivityFragmentHistory alf) {
            super(context);
            this.alf = alf;
        }

        @Override
        protected Cursor loadCursor() {
            return alf.getActivitiesListCursor();
        }
    }

    // Note that sortOrder must match corresponding column in sql query
    public boolean setSortOrder(MenuItem item) {
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.activities_sort_menu_activity_asc:
                sortOrder = 1;
                result = true;
                break;
            case R.id.activities_sort_menu_activity_desc:
                sortOrder = 2;
                result = true;
                break;
            case R.id.activities_sort_menu_location_asc:
                sortOrder = 3;
                result = true;
                break;
            case R.id.activities_sort_menu_location_desc:
                sortOrder = 4;
                result = true;
                break;
            case R.id.activities_sort_menu_date_asc:
                sortOrder = 5;
                result = true;
                break;
            case R.id.activities_sort_menu_date_desc:
                sortOrder = 6;
                result = true;
                break;
            case R.id.activities_sort_menu_description_asc:
                sortOrder = 7;
                result = true;
                break;
            case R.id.activities_sort_menu_description_desc:
                sortOrder = 8;
                result = true;
                break;
            default:
                sortOrder = 6;
                break;
        }
        if (result)
            getLoaderManager().restartLoader(0, null, this);
        return result;
    }

    private Cursor getActivitiesListCursor() {
        StringBuffer query = new StringBuffer();
        getDatabaseSetup();
        databaseHelper.createActivitySQL(query, exerciseSelections, locationSelections, sortOrder);
        cursor = database.rawQuery(query.toString(), null);
        if (cursor.getCount() == 0) {
            return null;
        }
        return cursor;
    }


    // Delete logic
    private void showTypeOfDeleteDialog() {
        ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                R.string.what_do_you_want_to_delete,
                R.string.delete_complete_activity, R.string.delete_only_gps_detail,
                R.string.cancel);
        dialog.setTargetFragment(ActivityFragmentHistory.this,
                TYPE_OF_DELETE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    private void showDeleteConfirmationDialog() {
        ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                (deleteDetailType == 1) ? R.string.press_yes_to_delete_selected_activities_or_no_to_cancel : R.string.press_yes_to_delete_GPS_delail_or_no_to_cancel,
                R.string.yes, R.string.no, -1);
        dialog.setTargetFragment(ActivityFragmentHistory.this,
                CONFIRM_DELETE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    private void displayTrashCanIfAnyToBeDeleted() {
        if (trashcan == null) {
            return;
        }

        if (deleteSet.size() > 0) {
            trashcan.setVisible(true);
            publishRelay.accept(new ChangeToolbarEvent(ChangeToolbarEvent.EVENT.SET_TOOLBAR_COLOR).setToolbarColor(getResources().getColor(R.color.check_circle_blue_grey)));
            publishRelay.accept(new ChangeToolbarEvent(ChangeToolbarEvent.EVENT.SET_TOOLBAR_TITLE).setToolbarTitle(getString(R.string.delete_activity_n, deleteSet.size())));
        } else {
            trashcan.setVisible(false);
            publishRelay.accept(new ChangeToolbarEvent(ChangeToolbarEvent.EVENT.RESET_TOOLBAR_COLOR_TO_DEFAULT));
            publishRelay.accept(new ChangeToolbarEvent(ChangeToolbarEvent.EVENT.RESET_TOOLBAR_TITLE_TO_DEFAULT));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        int buttonPressed;
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case TYPE_OF_DELETE_REQUESTCODE:
                buttonPressed = intent.getIntExtra(
                        ActivityDialogFragment.DIALOG_RESPONSE, -1);
                if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                    deleteDetailType = 1;
                    showDeleteConfirmationDialog();
                } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                    deleteDetailType = 2;
                    showDeleteConfirmationDialog();
                }
                break;
            case CONFIRM_DELETE_REQUESTCODE:
                buttonPressed = intent.getIntExtra(
                        ActivityDialogFragment.DIALOG_RESPONSE, -1);
                if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                    doDelete();
                } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                    // cancel - do nothing
                }
                break;
            case EXERCISE_REQUESTCODE:
                exerciseSelections = intent
                        .getStringArrayListExtra(ExerciseFilterDialog.EXTRA_SELECTIONS);
                getLoaderManager().restartLoader(0, null, this);
                break;
            case LOCATION_REQUESTCODE:
                locationSelections = intent
                        .getStringArrayListExtra(LocationFilterDialog.EXTRA_SELECTIONS);
                getLoaderManager().restartLoader(0, null, this);
                break;
        }
        //getLoaderManager().restartLoader(0, null, this);
    }

    // Confirmed delete so just do it.
    public void doDelete() {
        // delete the location exercise records and GPSLog records (if any)
        deleteActivitiesFromDatabase();
        doRefresh = true;
        getLoaderManager().restartLoader(
                GlobalValues.DELETE_ACTIVITY_LIST_LOADER, null, this);
    }

    //TODO move sql transaction to database helper
    private void deleteActivitiesFromDatabase() {
        Long key;
        // May want to delete activity that is currently running
        long currentLerId = gpsLocationManager.getCurrentLer();
        if (leDAO == null) {
            leDAO = trackerDatabaseHelper.getLocationExerciseDAO();
        }
        if (gpslrDAO == null) {
            gpslrDAO = trackerDatabaseHelper.getGPSLogDAO();
        }
        database.beginTransaction();
        try {
            Iterator<Long> iter = deleteSet.iterator();
            while (iter.hasNext()) {
                key = iter.next();
                if (key == currentLerId) {
                    gpsLocationManager.stopTrackingLer();
                }
                gpslrDAO.deleteGPSLogbyLerRowId(key);
                if (deleteDetailType == 1) {
                    leDAO.deleteLocationExercise(key);
                }
                deleteSet.remove(0);
            }
            deleteSet.clear();
            displayTrashCanIfAnyToBeDeleted();
            database.setTransactionSuccessful();
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }

    // LoaderCallBacks interface methods
    // #1
    // Note this gets called before onResume
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // we only ever load the runs, so assume this is the case
        getDatabaseSetup();
        return new ActivityFragmentHistory.ListCursorLoader(getContext(), this);
    }

    // #2
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        activityHistoryItemAdapter.swapCursor(cursor);
    }

    // #3
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is reset, we need to clear out the
        //current cursor from the adapter.
        activityHistoryItemAdapter.swapCursor(null);
    }

    public Bundle getSortFilterValues() {
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalValues.SORT_ORDER, sortOrder);
        bundle.putStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE,
                exerciseSelections);
        bundle.putStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE,
                locationSelections);
        long deleteKeysArray[] = createDeleteKeysArray(deleteList);
        bundle.putLongArray("DELETE_KEYS", deleteKeysArray);
        return bundle;
    }

    public void displayMap(ActivityHistorySummary activityHistorySummary) {
        Toast.makeText(getContext(), getContext().getResources().getString(R.string.displaying_the_map_may_take_a_moment),
                Toast.LENGTH_SHORT).show();
        Bundle args = new Bundle();
        args.putLong(TrackerDatabase.LocationExercise._ID, activityHistorySummary.getRow_id());
        args.putString(GlobalValues.TITLE, activityHistorySummary.createActivityTitle());
        args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
        args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, activityHistorySummary.getDescription());
        handleSelectedActionImpl.onSelectedAction(args);
    }

    public void displayStats(ActivityHistorySummary activityHistorySummary) {
        Bundle args = new Bundle();
        args.putLong(TrackerDatabase.LocationExercise._ID, activityHistorySummary.getRow_id());
        args.putString(GlobalValues.TITLE, activityHistorySummary.createActivityTitle());
        args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, activityHistorySummary.getDescription());
        args.putInt(GlobalValues.SORT_ORDER, sortOrder);
        args.putStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE, exerciseSelections);
        args.putStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE, locationSelections);
        args.putInt(GlobalValues.CURSOR_POSITION, activityHistorySummary.getPosition());
        args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_STATS);
        handleSelectedActionImpl.onSelectedAction(args);

    }


}
