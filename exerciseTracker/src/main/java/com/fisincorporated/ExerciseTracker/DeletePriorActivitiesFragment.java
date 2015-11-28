package com.fisincorporated.ExerciseTracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.fisincorporated.database.GPSLogDAO;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.TrackerDatabase.Exercise;
import com.fisincorporated.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;

// cribbed some code from http://stackoverflow.com/questions/15221320/listview-delete-button-on-checkbox-checked
// and http://stackoverflow.com/questions/18162931/android-get-selected-item-using-checkbox-in-listview-when-i-click-a-button

public class DeletePriorActivitiesFragment extends ActivityListFragment
		implements LoaderCallbacks<Cursor> {
	private Button btnCancel = null;
	private Button btnDelete = null;

	private LocationExerciseDAO leDAO = null;
	private GPSLogDAO gpslrDAO = null;
	private static final int TYPE_OF_DELETE_REQUESTCODE = 1;
	private static final int CONFIRM_DELETE_REQUESTCODE = 2;
	private int deleteDetailType = 0;

	// Called (after onCreate) when the Fragment is attached to its parent
	// Activity.
	// Create, or inflate the Fragment's UI, and return it.
	// Wait till this point if fragment needs to interact with UI of parent
	// Activity
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get views from fragment layout
		// Bind data to views (array adapters etc
		// Create/Assign listeners
		// Create services/timers
		// If this Fragment has no UI then return null
		// setHasOptionsMenu(true);
		restoreSortFilter(getArguments());

		View view = inflater.inflate(R.layout.prior_activity_delete_list,
				container, false);
		activityListView = (ListView) view.findViewById(android.R.id.list);
		// activityListView
		// .setEmptyView(view.findViewById(R.id.empty_activity_list));
		btnCancel = (Button) view
				.findViewById(R.id.prior_activity_delete_list_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("DefaultLocale")
			public void onClick(View v) {
				getFragmentManager().popBackStack();

			}
		});

		btnDelete = (Button) view
				.findViewById(R.id.prior_activity_delete_list_delete);
		btnDelete.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("DefaultLocale")
			public void onClick(View v) {
				showDeleteDialog1();
			}

		});

		registerForContextMenu((View) view
				.findViewById(R.id.prior_activity_delete_columns));
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// getLoaderManager().restartLoader(0, null, this);
		setDeleteCountOnButtion();

	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox cb = (CheckBox) v
				.findViewById(R.id.prior_activity_row_delete_checkbox);
		if (cb.isChecked()) {
			cb.setChecked(false);
			deleteList.remove(id);
			setDeleteCountOnButtion();

		} else {
			cb.setChecked(true);
			if (!deleteList.containsKey(id)) {
				deleteList.put(id, id);
			}
			setDeleteCountOnButtion();
		}

	}

	void showDeleteDialog1() {
		ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
				R.string.what_do_you_want_to_delete,
				R.string.delete_complete_activity, R.string.delete_only_gps_detail,
				R.string.cancel);
		dialog.setTargetFragment(DeletePriorActivitiesFragment.this,
				TYPE_OF_DELETE_REQUESTCODE);
		dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
	}

	void showDeleteDialog2() {
		ActivityDialogFragment dialog = ActivityDialogFragment
				.newInstance(
						-1,
						(deleteDetailType == 1) ? R.string.press_yes_to_delete_selected_activities_or_no_to_cancel
								: R.string.press_yes_to_delete_GPS_delail_or_no_to_cancel,
						R.string.yes, R.string.no, -1);
		dialog.setTargetFragment(DeletePriorActivitiesFragment.this,
				CONFIRM_DELETE_REQUESTCODE);
		dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
	}

	private void setDeleteCountOnButtion() {
		if (deleteList.size() > 0) {
			btnDelete.setText(getResources().getText(R.string.delete) + "("
					+ deleteList.size() + ")");
			btnDelete.setEnabled(true);
		} else {
			btnDelete.setText(getResources().getText(R.string.delete));
			btnDelete.setEnabled(false);

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == TYPE_OF_DELETE_REQUESTCODE) {
			int buttonPressed = intent.getIntExtra(
					ActivityDialogFragment.DIALOG_RESPONSE, -1);
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				deleteDetailType = 1;
				showDeleteDialog2();
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				deleteDetailType = 2;
				showDeleteDialog2();
			}
		} else if (requestCode == CONFIRM_DELETE_REQUESTCODE) {
			int buttonPressed = intent.getIntExtra(
					ActivityDialogFragment.DIALOG_RESPONSE, -1);
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				doPositiveClick();
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				// cancel - do nothing
			}
		}
	}

	// Confirmed delete so just do it.
	public void doPositiveClick() {
		// delete the location exercise records and GPSLog records (if any)
		deletePriorActivities();
		doRefresh = true;
		getLoaderManager().restartLoader(
				GlobalValues.DELETE_ACTIVITY_LIST_LOADER, null, this);
	}

	private void deletePriorActivities() {
		if (leDAO == null) {
			leDAO = new LocationExerciseDAO(databaseHelper);
		}
		if (gpslrDAO == null) {
			gpslrDAO = new GPSLogDAO(databaseHelper);
		}
		database.beginTransaction();
		try {
			for (Long key : deleteList.keySet()) {
				gpslrDAO.deleteGPSLogbyLerRowId(key);
				if (deleteDetailType == 1) {
					leDAO.deleteLocationExercise(key);
				}
				deleteList.remove(0);
			}
			deleteList.clear();
			setDeleteCountOnButtion();
			database.setTransactionSuccessful();
		} finally {
			if (database.inTransaction()) {
				database.endTransaction();
			}
		}

	}

	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// super.onCreateOptionsMenu(menu, inflater);
	// menu.findItem(R.id.activities_list_options_delete).setEnabled(false).setVisible(false);
	// }

	// LoaderCallBacks interface methods
	// #1
	// @Override
	// public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	// we only use one cursor so int id can = 0
	// getDatabaseSetup();
	// return new ListCursorLoader(getActivity(), this);
	// }

	// #2
	@SuppressWarnings("deprecation")
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.prior_activity_delete_row, csrUtility, new String[] {
						Exercise.EXERCISE, ExrcsLocation.LOCATION,
						GlobalValues.START_DATE, LocationExercise.DESCRIPTION,
						LocationExercise.EXERCISE_ID }, new int[] {
						R.id.prior_activity_row_activity,
						R.id.prior_activity_row_location,
						R.id.prior_activity_row_date,
						R.id.prior_activity_row_description,
						R.id.prior_activity_row_delete_checkbox }, 0);

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				long rowId = -1;
				// We don't care about duplicate exercise column in adapter above,
				// we just include that column in query
				// so that this gets called for the checkbox view and we can set the
				// checkbox value
				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						GlobalValues.START_DATE)) {
					String activityDate = cursor.getString(cursor
							.getColumnIndex(GlobalValues.START_DATE));
					((TextView) view).setText(Utility.formatDate(dateFormat,
							activityDate));
					return true;
				}
				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.EXERCISE_ID)) {
					rowId = cursor.getLong(cursor
							.getColumnIndex(LocationExercise._ID));
					// CheckBox cb = (CheckBox)
					// view.findViewById(R.id.prior_activity_row_delete_checkbox);
					if (view instanceof CheckBox) {
						if (deleteList.containsKey(rowId))
							((CheckBox) view).setChecked(true);
						else
							((CheckBox) view).setChecked(false);
						return true;
					}
				}
				return false;
			};
		});

		setListAdapter(adapter);

	}

	// // #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		// setting to null gives error stating fragment is not in the fragment
		// manager
		// setListAdapter(null);
		((SimpleCursorAdapter) this.getListAdapter()).changeCursor(null);
	}

}
