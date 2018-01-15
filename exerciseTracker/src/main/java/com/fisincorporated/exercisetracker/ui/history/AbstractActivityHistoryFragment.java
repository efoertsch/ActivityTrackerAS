package com.fisincorporated.exercisetracker.ui.history;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.ui.filters.ExerciseFilterDialog;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.ui.filters.LocationFilterDialog;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.master.IHandleSelectedAction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractActivityHistoryFragment  extends ExerciseMasterFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    protected static final String EXERCISE_FILTER_DIALOG = "exercise_filter_dialog";
    protected static final String LOCATION_FILTER_DIALOG = "location_filter_dialog";
    protected static final int EXERCISE_REQUESTCODE = 0;
    protected static final int LOCATION_REQUESTCODE = 1;
    protected static int sortOrder = 6;
    private static int defaultSortOrder = sortOrder;
    protected ArrayList<String> exerciseSelections = new ArrayList<>();
    protected ArrayList<String> locationSelections = new ArrayList<>();
    protected static boolean doRefresh = false;

    protected Cursor cursor = null;

    //dateFormat used in subclasses
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    protected HashMap<Long, Long> deleteList = new HashMap<Long, Long>();

    protected IHandleSelectedAction callBacks;

    protected RecyclerView recyclerView;

    private ActivityHistoryItemAdapter activityHistoryItemAdapter;

    // TODO - create super class
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //TODO check/throw exception
        callBacks = (IHandleSelectedAction) activity;

    }

    public void onDetach() {
        super.onDetach();
        callBacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy(){
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
        super.onDestroy();
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
        ArrayList<Long> deleteKeyList = new ArrayList<Long>(deleteList.values());
        for (int i = 0; i < deleteKeys.length; ++i) {
            deleteKeys[i] = deleteKeyList.get(i);
        }
        return deleteKeys;
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
        dialog.setTargetFragment(AbstractActivityHistoryFragment.this, EXERCISE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(),
                EXERCISE_FILTER_DIALOG);
    }

    public void showLocationFilter() {
        LocationFilterDialog locDialog = LocationFilterDialog.newInstance(
                locationSelections, getString(R.string.filter_by_location));
        locDialog.setTargetFragment(AbstractActivityHistoryFragment.this,
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
        AbstractActivityHistoryFragment alf;

        public ListCursorLoader(Context context, AbstractActivityHistoryFragment alf) {
            super(context);
            this.alf = alf;
        }

        @Override
        protected Cursor loadCursor() {
            return alf.getActivitiesListCursor();
        }
    }

    protected Cursor getActivitiesListCursor() {
        StringBuffer query = new StringBuffer();
        getDatabaseSetup();
        databaseHelper.createActivitySQL(query, exerciseSelections, locationSelections, sortOrder);
        cursor = database.rawQuery(query.toString(), null);
        if (cursor.getCount() == 0)
            return null;
        return cursor;
    }


    // Note that sortOrder must match
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
            // getActivitiesListCursor(sortOrder);
            getLoaderManager().restartLoader(0, null, this);
        return result;
    }

    // LoaderCallBacks interface methods
    // #1
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // we only ever load the runs, so assume this is the case
        getDatabaseSetup();
        return new ListCursorLoader(getContext(), this);
    }

    // #2
    @Override
    public abstract void onLoadFinished(Loader<Cursor> loader, Cursor cursor) ;

    // #3
    @Override
    public abstract void onLoaderReset(Loader<Cursor> loader) ;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == EXERCISE_REQUESTCODE) {
            exerciseSelections = intent
                    .getStringArrayListExtra(ExerciseFilterDialog.EXTRA_SELECTIONS);

        }
        if (requestCode == LOCATION_REQUESTCODE) {
            locationSelections = intent
                    .getStringArrayListExtra(LocationFilterDialog.EXTRA_SELECTIONS);
        }
        getLoaderManager().restartLoader(0, null, this);
        // getActivitiesListCursor(sortOrder);

    }

    public int getSortOrder() {
        return sortOrder;
    }

    public ArrayList<String> getExerciseSelections() {
        return exerciseSelections;
    }

    public ArrayList<String> getLocationSelections() {
        return locationSelections;
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


    //IHistoryCallbacks

    public void displayMap(ActivityHistorySummary activityHistorySummary){
        Toast.makeText(getContext(), getContext().getResources().getString(R.string.displaying_the_map_may_take_a_moment),
                Toast.LENGTH_SHORT).show();
        Bundle args = new Bundle();
        args.putLong(TrackerDatabase.LocationExercise._ID, activityHistorySummary.getRow_id());
        args.putString(GlobalValues.TITLE, activityHistorySummary.createActivityTitle());
        args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
        args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, activityHistorySummary.getDescription());
        callBacks.onSelectedAction(args);
    };

    public void displayStats(ActivityHistorySummary activityHistorySummary, int position){
        Bundle args = new Bundle();
        args.putLong(TrackerDatabase.LocationExercise._ID, activityHistorySummary.getRow_id());
        args.putString(GlobalValues.TITLE, activityHistorySummary.createActivityTitle());
        args.putString(TrackerDatabase.LocationExercise.DESCRIPTION, activityHistorySummary.getDescription()) ;
        args.putInt(GlobalValues.SORT_ORDER, sortOrder);
        args.putStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE, exerciseSelections);
        args.putStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE, locationSelections);
        args.putInt(GlobalValues.CURSOR_POSITION, position);
        args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_STATS);
        callBacks.onSelectedAction(args);

    };

}