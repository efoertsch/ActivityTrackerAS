package com.fisincorporated.exercisetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.widget.ListView;

import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.interfaces.IHandleSelectedAction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ActivityListFragment extends AbstractListFragment implements
		LoaderCallbacks<Cursor> {
	protected static final String EXERCISE_FILTER_DIALOG = "exercise_filter_dialog";
	protected static final String LOCATION_FILTER_DIALOG = "location_filter_dialog";
	protected static final int EXERCISE_REQUESTCODE = 0;
	protected static final int LOCATION_REQUESTCODE = 1;
	protected ListView activityListView = null;
	protected static int sortOrder = 6;
	private static int defaultSortOrder = sortOrder;
	protected ArrayList<String> exerciseSelections = new ArrayList<String>();
	protected ArrayList<String> locationSelections = new ArrayList<String>();
	protected static boolean doRefresh = false;
	
	 
	protected Cursor csrUtility = null;
	//dateFormat used in subclasses
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	protected HashMap<Long, Long> deleteList = new HashMap<Long, Long>();
	
	protected IHandleSelectedAction callBacks;
	
	 

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		callBacks = (IHandleSelectedAction) activity;
	}
	public void onDetach(){
		super.onDetach();
		callBacks = null;
	}

	// Initialize the Fragment.
	// Called (after onAttach) when the Fragment is attached to its parent
	// Activity.
	// Create any class scoped objects here
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	private long[] createDeleteKeysArray(HashMap<Long, Long> deleteList) {
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
      getLoaderManager().initLoader(0, null, this);

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
		dialog.setTargetFragment(ActivityListFragment.this, EXERCISE_REQUESTCODE);
		dialog.show(getActivity().getSupportFragmentManager(),
				EXERCISE_FILTER_DIALOG);
	}

	public void showLocationFilter() {
		LocationFilterDialog locDialog = LocationFilterDialog.newInstance(
				locationSelections, getString(R.string.filter_by_location));
		locDialog.setTargetFragment(ActivityListFragment.this,
				LOCATION_REQUESTCODE);
		locDialog.show(getActivity().getSupportFragmentManager(),
				LOCATION_FILTER_DIALOG);
	}

	public void clearFilters() {
		exerciseSelections = new ArrayList<String>();
		locationSelections = new ArrayList<String>();
		getLoaderManager().restartLoader(0, null, this);
		// getActivitiesListCursor(sortOrder);
	}

	public static class ListCursorLoader extends SQLiteCursorLoader {
		// private DeletePriorActivitiesFragment palf;
		ActivityListFragment alf;

		// public ListCursorLoader(Context context,DeletePriorActivitiesFragment
		// palf) {
		public ListCursorLoader(Context context, ActivityListFragment alf) {
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
	 	databaseHelper.createActivitySQL(query, exerciseSelections, locationSelections, sortOrder  );
		csrUtility = database.rawQuery(query.toString(), null);
		if (csrUtility.getCount() == 0)
			return null;
		return csrUtility;
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
		return new ListCursorLoader(getActivity(), this);
	}

	// #2
	@Override
	public abstract void onLoadFinished(Loader<Cursor> loader, Cursor cursor);

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		setListAdapter(null);
	}

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



}
