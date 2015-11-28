package com.fisincorporated.ExerciseTracker;

import java.util.ArrayList;

import com.fisincorporated.database.TrackerDatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ArrayAdapter;

public abstract class FilterDialogFragment extends DialogFragment {

	protected TrackerDatabaseHelper databaseHelper;
	protected SQLiteDatabase database;

	public static final String EXTRA_SELECTIONS = "com.fisincorporated.ExerciseTracker.checkedSelections";
	public static final String EXTRA_TITLE = "com.fisincorporated.ExerciseTracker.title";

	private ArrayList<String> selections;
	private ArrayList<String> originalSelections;
	private ArrayList<String> checkedSelections;
	private boolean[] checkedItems;

	public FilterDialogFragment() {
		// TODO Auto-generated constructor stub
	}

	// public static FilterDialogFragment
	// newInstance(ArrayList<String>originalSelections, String title){
	// Bundle args = new Bundle();
	// args.putStringArrayList(EXTRA_EXERCISE_SELECTIONS, originalSelections);
	// args.putString(EXTRA_TITLE,title);
	// FilterDialogFragment fragment = new FilterDialogFragment();
	// fragment.setArguments(args);
	// return fragment;
	// }

	// use by implementing class
	protected void getDatabaseSetup() {
		if (databaseHelper == null)
			databaseHelper = TrackerDatabaseHelper
					.getTrackerDatabaseHelper(getActivity().getApplicationContext());
		if (database == null) {
			Log.i(GlobalValues.LOG_TAG,
					"FilterDialog.getDatabaseSetup com.fisincorporated.database null, calling getWritable");
			database = databaseHelper.getWritableDatabase();
		}
		if (!database.isOpen()) {
			Log.i(GlobalValues.LOG_TAG,
					"FilterDialog.getDatabaseSetup com.fisincorporated.database not null but not open, calling getWritable");
			database = databaseHelper.getWritableDatabase();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDatabaseSetup();

	}

	@SuppressWarnings("unchecked")
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString(EXTRA_TITLE);
		originalSelections = (ArrayList<String>) getArguments()
				.getStringArrayList(EXTRA_SELECTIONS);
		checkedSelections = (ArrayList<String>) originalSelections.clone();
		selections = loadSelectionArray();
		setCheckedItems();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_dropdown_item,
				selections);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		builder.setTitle(title);
		// .setView(view)
		builder.setMultiChoiceItems(selections.toArray(new String[0]),
				checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						// Log.i(GlobalValues.LOG_TAG, "Item " + which
						// + (isChecked ? " is checked" : " is not checked"));
						checkedItems[which] = isChecked;
					}

				});

		// The OK button returns a (possibly) updated filter list)
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						createNewFilterList();
						sendResult(Activity.RESULT_OK);
					}
				});
		// Negative button is Cancel so return the original filter list
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Log.i(GlobalValues.LOG_TAG, "NegativeButton Clicked");
						checkedSelections = originalSelections;
						sendResult(Activity.RESULT_OK);
					}
				});
		// set neutral is to clear filter (show all)
		builder.setNeutralButton(R.string.clear_filter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Log.i(GlobalValues.LOG_TAG, "Neutral Clicked");
						checkedSelections = new ArrayList<String>();
						sendResult(Activity.RESULT_OK);
					}
				});
		return builder.create();
	}

	// Load list of strings into selections ArrayList
	protected abstract ArrayList<String> loadSelectionArray();

	// protected ArrayList<String> loadSelectionArray() {
	// selections = new ArrayList<String>();
	// databaseHelper = TrackerDatabaseHelper
	// .getTrackerDatabaseHelper(getActivity());
	// com.fisincorporated.database = databaseHelper.getWritableDatabase();
	// Cursor csr = com.fisincorporated.database.query(Exercise.EXERCISE_TABLE,
	// new String[] { Exercise.EXERCISE }, null, null, null, null,
	// Exercise.DEFAULT_SORT_ORDER);
	// if (csr.getCount() != 0){
	// csr.moveToFirst();
	// while (!csr.isAfterLast()) {
	// selections.add(csr.getString(csr
	// .getColumnIndex(Exercise.EXERCISE)));
	// csr.moveToNext();
	// }
	// }
	// csr.close();
	// return selections;
	// }

	private void setCheckedItems() {
		checkedItems = new boolean[selections.size()];
		for (int i = 0; i < checkedSelections.size(); ++i) {
			for (int j = 0; j < selections.size(); ++j) {
				if (selections.get(j).equals(checkedSelections.get(i))) {
					checkedItems[j] = true;
					break;
				}
			}
		}
	}

	private void createNewFilterList() {
		checkedSelections = new ArrayList<String>();
		for (int i = 0; i < selections.size(); ++i) {
			if (checkedItems[i] == true)
				checkedSelections.add(selections.get(i));
		}
	}

	private void sendResult(int resultCode) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putStringArrayListExtra(FilterDialogFragment.EXTRA_SELECTIONS,
				checkedSelections);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

	public void onDestroy() {
		if (database != null) {
			Log.i(GlobalValues.LOG_TAG, "FilterDialog.onDestroy com.fisincorporated.database not null");
			if (database.isOpen()) {
				Log.i(GlobalValues.LOG_TAG,
						"FilterDialog.onDestroy com.fisincorporated.database is open so calling close ");
				database.close();
			}
			database = null;
		}
		super.onDestroy();
	}

	@Override
	public void finalize() {
		if (database != null) {
			Log.i(GlobalValues.LOG_TAG, "FilterDialog.finalize com.fisincorporated.database not null");
			if (database.isOpen())
				Log.i(GlobalValues.LOG_TAG,
						"FilterDialog.finalize com.fisincorporated.database is open so calling close");
			database.close();
			database = null;
		}
	}
}