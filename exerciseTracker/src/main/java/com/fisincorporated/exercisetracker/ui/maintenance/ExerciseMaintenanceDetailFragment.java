package com.fisincorporated.exercisetracker.ui.maintenance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.ActivityDialogFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.utility.InputFilterMinMax;
import com.fisincorporated.utility.Utility;

import java.text.DecimalFormat;
import java.util.Locale;

public class ExerciseMaintenanceDetailFragment extends ExerciseMasterFragment {

	private long exerciseRowId = -1;
	private long exerciseLocationRowId = -1;
	private AutoCompleteTextView actvExercise;
	// Always log detail
	//private CheckBox chkbxLogDetail;
	private EditText etLogInterval;
	private EditText etDefaultLogInterval;
	private EditText etMinDistToTravel;
	private CheckBox chkbxElevationInCalcs;
	private TextView tvUnits;

	static final int DELETE_REQUESTCODE = 1;
	private Button btnSave;
	private Button btnDelete;
	private long origExerciseId;
	private ExerciseRecord exerciseRecord = null;

	public ExerciseMaintenanceDetailFragment() {
	}

	public static ExerciseMaintenanceDetailFragment newInstance(Bundle bundle) {
		ExerciseMaintenanceDetailFragment fragment = new ExerciseMaintenanceDetailFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view;
		view = inflater.inflate(R.layout.exercise_maintanence, container, false);
		lookForArguments(savedInstanceState);
		findDisplayUnits();
		getReferencedViews(view);
		return view;
	}

	private void lookForArguments(Bundle savedInstanceState) {
		Bundle bundle;
		if (getArguments() != null) {
			bundle = getArguments();
			exerciseRecord = (ExerciseRecord) bundle
					.getParcelable(Exercise.EXERCISE_TABLE);
			origExerciseId = exerciseRecord.get_id();
		}
		if (savedInstanceState != null) {
			exerciseRecord = savedInstanceState
					.getParcelable(Exercise.EXERCISE_TABLE);
			origExerciseId = exerciseRecord.get_id();
		}
	}

	// save the current record so if orientation change you can display same one
	public void onSaveInstanceState(Bundle savedInstanceState) {
		updateExerciseRecordFromScreen();
		savedInstanceState.putParcelable(Exercise.EXERCISE_TABLE, exerciseRecord);
		super.onSaveInstanceState(savedInstanceState);

	}

	// if rowid passed then you can either update the log interval
	// or delete the exercise
	// if no rowid passed then you are entering new exercise
	// make sure appropriate field access set
	@Override
	public void onResume() {
		super.onResume();
		showExercise();
		setFieldAccess();
	}
		

	private void getReferencedViews(View view) {
		actvExercise = (AutoCompleteTextView) view
				.findViewById(R.id.exercise_maintenance_actvExercise);
//		chkbxLogDetail = (CheckBox) view
//				.findViewById(R.id.exercise_maintenance_chkbxLog_Detail);
		etLogInterval = (EditText) view
				.findViewById(R.id.exercise_maintenance_etLogInterval);
		etDefaultLogInterval = (EditText) view
				.findViewById(R.id.exercise_maintenance_DefaultLogInterval);
		etMinDistToTravel = (EditText) view
				.findViewById(R.id.exercise_maintenance_etMinDistToTravel);
		chkbxElevationInCalcs = (CheckBox) view
				.findViewById(R.id.exercise_maintenance_chkbxElevationInCalcs);
		tvUnits = (TextView) view.findViewById(R.id.exercise_maintenance_tvUnits);
		tvUnits.setText(feetMeters);
		etLogInterval.setFilters(new InputFilter[] { new InputFilterMinMax("1",
				"600") });
		etDefaultLogInterval
				.setFilters(new InputFilter[] { new InputFilterMinMax("1", "600") });
		etMinDistToTravel.setFilters(new InputFilter[] { new InputFilterMinMax(
				"1", "600") });

		// Handle Save Button
		btnSave = (Button) view.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("DefaultLocale")
			public void onClick(View v) {
				updateExercise();
				goToExeciseList();
			}
		});

		// Handle Delete Button
		// See if any logging done using this exercise. If so you can't delete it
		btnDelete = (Button) view.findViewById(R.id.btnDelete);
		btnDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ActivityDialogFragment dialog;
				dialog = ActivityDialogFragment.newInstance(-1,
						R.string.confirm_delete_exercise, R.string.delete,
						R.string.cancel, -1);
				dialog.setTargetFragment(ExerciseMaintenanceDetailFragment.this,
						DELETE_REQUESTCODE);
				dialog.show(getActivity().getSupportFragmentManager(),
						"confirmDialog");
			}
		});
	}

	private void setFieldAccess() {
		if (origExerciseId != -1) {
			// since exercise rowid passed in then you can update or delete record
			exerciseRowId = origExerciseId;
			actvExercise.setEnabled(false);
			etDefaultLogInterval.setEnabled(false);
			etLogInterval.requestFocus();
			if (isDefaultExercise(exerciseRecord.getExercise())) {
				// Toast.makeText(getActivity(),
				// getString(R.string.default_exercise_can_not_be_deleted),
				// Toast.LENGTH_LONG).show();
				btnDelete.setVisibility(View.INVISIBLE);
			} else {
				btnDelete.setVisibility(View.VISIBLE);
			}
		} else {
			// exercise rowid not passed in so can add
			actvExercise.setEnabled(true);
			etDefaultLogInterval.setEnabled(true);
			//chkbxLogDetail.setEnabled(true);
			actvExercise.requestFocus();
			btnDelete.setVisibility(View.INVISIBLE);
		}
	}
	
	private void updateExercise() {
		boolean exerciseExists;
		if (actvExercise.getText().toString().trim().isEmpty()) {
			Toast.makeText(
					getActivity(),
					getResources().getString(
							R.string.exercise_not_defined_nothing_to_save),
					Toast.LENGTH_LONG).show();
			return;
		}
		// if update inside a transaction get (5) com.fisincorporated.exercisetracker.database is locked error.
		// Why is that?
		database.beginTransaction();
		try {
			// if new Exercise save it, else update existing Exercise
			if (!(exerciseExists = doesExerciseExist())) {
				// save new Exercise
				updateExerciseRecordFromScreen();
				// new
				// ExerciseDAO(getTrackerDataseHelper()).createExerciseRecord(exerciseRecord);
				insertNewExcersiseRecord(exerciseRecord);
				Toast.makeText(
						getActivity(),
						actvExercise.getText().toString().trim() + " "
								+ getResources().getString(R.string.saved),
						Toast.LENGTH_LONG).show();
				database.setTransactionSuccessful();
			} else if (exerciseExists && origExerciseId == -1) {
				Toast.makeText(
						getActivity(),
						getResources()
								.getString(
										R.string.an_exercise_already_exists_with_this_name_exercise_not_added),
						Toast.LENGTH_LONG).show();
			} else {
				// update existing Exercise
				updateExerciseRecordFromScreen();
				// new
				// ExerciseDAO(getTrackerDataseHelper()).updateExercise(exerciseRecord);
				updateExistingExerciseRecord(exerciseRecord);
				Toast.makeText(
						getActivity(),
						actvExercise.getText().toString().trim() + " "
								+ getResources().getString(R.string.updated),
						Toast.LENGTH_LONG).show();
				database.setTransactionSuccessful();
			}
		} catch (SQLiteException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"ExerciseMaintenanceDetailFragment.onClick for update/insert of exercise. SQLiteException: "
							+ sqle.toString());
		} finally {
			if (database.inTransaction())
				database.endTransaction();
		}
	}

	private void updateExerciseRecordFromScreen() {
		exerciseRecord.setExercise(actvExercise.getText() + "");
		//exerciseRecord.setLogDetail(chkbxLogDetail.isChecked() == true ? 1 : 0);
		exerciseRecord.setLogDetail(1);
		try {
			exerciseRecord.setDefaultLogInterval(Integer
					.parseInt(etDefaultLogInterval.getText().toString()));
		} catch (NumberFormatException nfe) {
			exerciseRecord.setLogInterval(ExerciseRecord.DEFAULT_LOG_INTERVAL);
		}
		try {
			exerciseRecord.setLogInterval(Integer.parseInt(etLogInterval.getText()
					.toString()));
		} catch (NumberFormatException nfe) {
			exerciseRecord.setLogInterval(ExerciseRecord.LOG_INTERVAL);
		}
		try {
			if (feetMeters.equalsIgnoreCase("m")) {
				exerciseRecord.setMinDistanceToLog(Float
						.parseFloat(etMinDistToTravel.getText().toString()));
			} else {
				exerciseRecord.setMinDistanceToLog(Utility.feetToMeters(Float
						.parseFloat(etMinDistToTravel.getText().toString())));
				etMinDistToTravel.setText(new DecimalFormat("#####").format(Utility
						.metersToFeet(exerciseRecord.getMinDistanceToLog())));
			}
		} catch (NumberFormatException nfe) {
			exerciseRecord.setMinDistanceToLog(ExerciseRecord.MIN_DISTANCE_TO_LOG);
		}
		exerciseRecord.setElevationInDistCalcs(chkbxElevationInCalcs.isChecked() == true ? 1 : 0);
	}

	private void updateExistingExerciseRecord(ExerciseRecord exerciser) {
		Long rowId = exerciser.get_id();
		ContentValues values = new ContentValues();
		values.put(Exercise._ID, exerciser.get_id());
		values.put(Exercise.EXERCISE, exerciser.getExercise());
		values.put(Exercise.LOG_INTERVAL,
				Integer.toString(exerciser.getLogInterval()));
		values.put(Exercise.DEFAULT_LOG_INTERVAL,
				Integer.toString(exerciser.getDefaultLogInterval()));
		values.put(Exercise.LOG_DETAIL,
				Integer.toString(exerciser.getLogDetail()));
		values.put(Exercise.TIMES_USED,
				Integer.toString(exerciser.getTimesUsed()));
		values.put(Exercise.ELEVATION_IN_DIST_CALCS,
				Integer.toString(exerciser.getElevationInDistCalcs()));
		values.put(Exercise.MIN_DISTANCE_TO_LOG,
				Float.toString(exerciser.getMinDistanceToLog()));

		database.update(Exercise.EXERCISE_TABLE, values, " _id = ?",
				new String[] { rowId.toString() });
	}

	private void insertNewExcersiseRecord(ExerciseRecord exerciser) {
		Long rowId = -1l;
		ContentValues values = new ContentValues();
		// all fields mandatory fields except for _ID
		values.put(Exercise.EXERCISE, exerciser.getExercise());
		values.put(Exercise.LOG_INTERVAL,
				Integer.toString(exerciser.getLogInterval()));
		values.put(Exercise.DEFAULT_LOG_INTERVAL,
				Integer.toString(exerciser.getDefaultLogInterval()));
		values.put(Exercise.LOG_DETAIL,
				Integer.toString(exerciser.getLogDetail()));
		values.put(Exercise.ELEVATION_IN_DIST_CALCS,
				Integer.toString(exerciser.getElevationInDistCalcs()));
		values.put(Exercise.MIN_DISTANCE_TO_LOG,
				Float.toString(exerciser.getMinDistanceToLog()));

		rowId = database.insert(Exercise.EXERCISE_TABLE, null, values);
		exerciser.set_id(rowId);
	}

	private boolean doesExerciseLocationExist() {
		Cursor csr;
		// SQL Query
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(LocationExercise.LOCATION_EXERCISE_TABLE);
			queryBuilder.appendWhere(LocationExercise.EXERCISE_ID + "='"
					+ exerciseRowId + "'");

			// run the query since it's all ready to go
			csr = queryBuilder.query(database, null, null, null, null, null, null);

			if (csr.getCount() == 0) {
				exerciseLocationRowId = -1;
			} else {
				csr.moveToFirst();
				exerciseLocationRowId = csr.getLong(csr
						.getColumnIndex(LocationExercise._ID));
			}
			csr.close();
			//database.setTransactionSuccessful();
		} finally {
			//database.endTransaction();
		}
		if (exerciseLocationRowId > 0)
			return true;
		else
			return false;

	}

	private void goToExeciseList() {
		// This may not be best way to do this.
		// if fragment displayed via ExerciseMaintenanceListActivity on table, prior state will be on
		// backstack
		getActivity().onBackPressed();
		// getFragmentManager().popBackStack();
	}

	private boolean doesExerciseExist() {
		// check if Exercise exists already
		Cursor csr = null;
		try {
			String strExercise = actvExercise.getText().toString()
					.toLowerCase(Locale.getDefault()).trim();
			// SQL Query
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Exercise.EXERCISE_TABLE);
			queryBuilder.appendWhere("lower(" + Exercise.EXERCISE + ")" + "='"
					+ strExercise + "'");

			// run the query since it's all ready to go
			csr = queryBuilder.query(database, null, null, null, null, null, null);

			if (csr.getCount() == 0) {
				exerciseRowId = -1;
			} else {
				csr.moveToFirst();
				exerciseRowId = csr.getLong(csr.getColumnIndex(Exercise._ID));
			}

		} catch (SQLException sqle) {

		} finally {
			if (csr != null) {
				try {
					csr.close();
				} catch (SQLException sqle) {
					;
				}
				;
			}
		}
		if (exerciseRowId > 0)
			return true;
		else
			return false;
	}

	// Show exercise - most likely for update action

	private void showExercise() {
		exerciseRowId = exerciseRecord.get_id();
		actvExercise.setText(exerciseRecord.getExercise());
		//chkbxLogDetail.setChecked(exerciseRecord.getLogDetail() == 1 ? true : false);
		etLogInterval.setText("" + exerciseRecord.getLogInterval());
		etDefaultLogInterval.setText("" + exerciseRecord.getDefaultLogInterval());
		if (feetMeters.equalsIgnoreCase("m")) {
			etMinDistToTravel.setText(new DecimalFormat("#####")
					.format(exerciseRecord.getMinDistanceToLog()));
		} else
			etMinDistToTravel.setText(new DecimalFormat("#####").format(Utility
					.metersToFeet(exerciseRecord.getMinDistanceToLog())));
		chkbxElevationInCalcs
				.setChecked(exerciseRecord.getElevationInDistCalcs() == 1 ? true
						: false);
		tvUnits.setText(feetMeters);
		etLogInterval.requestFocus();
		etLogInterval.setCursorVisible(true);
	}

	public void onPause() {
		super.onPause();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == DELETE_REQUESTCODE) {
			int buttonPressed = intent.getIntExtra(
					ActivityDialogFragment.DIALOG_RESPONSE, -1);
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				deleteExercise();
				goToExeciseList();
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				Toast.makeText(getActivity(),
						getResources().getString(R.string.continuex),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private int deleteExercise(Long id) {
		int numberRowsDeleted = 0;
		numberRowsDeleted = database.delete(Exercise.EXERCISE, Exercise._ID
				+ "=?", new String[] { id.toString() });
		if (numberRowsDeleted == 1) {
			// com.fisincorporated.exercisetracker.database.setTransactionSuccessful();
			Toast.makeText(getActivity(),
					getResources().getString(R.string.the_exercise_was_deleted),
					Toast.LENGTH_SHORT).show();
		}
		return numberRowsDeleted;
	}

	// delete the exercise
	private void deleteExercise() {
		database.beginTransaction();
		try {
			if (doesExerciseExist()) {
				// OK it exists, now see if any exercises done using it
				if (doesExerciseLocationExist()) {
					// can't delete as some log records exist
					// so dialog you can't delete record
					Toast.makeText(
							getActivity(),
							getResources()
									.getString(
											R.string.can_not_delete_as_log_records_exist_for_this_exercise),
							Toast.LENGTH_LONG).show();
				} else {
					// no records so can delete it
					deleteExercise(exerciseRowId);
					database.setTransactionSuccessful();
				}

			} else {
				// exercise doesn't exist in exercise com.fisincorporated.exercisetracker.database
				// so nothing to do
				Toast.makeText(
						getActivity(),
						getResources().getString(
								R.string.can_not_delete_as_exercise_does_not_exist),
						Toast.LENGTH_LONG).show();
			}

		} catch (IllegalStateException ise) {
			Toast.makeText(
					getActivity(),
					getResources()
							.getString(R.string.illegal_state_exception_caught),
					Toast.LENGTH_LONG).show();
		} finally {
			if (database.inTransaction())
				database.endTransaction();
		}
	}

	private boolean isDefaultExercise(String exerciseToDelete) {
		boolean exerciseInList = false;
		Resources resources = getResources();
		TypedArray taExerciseArray = getResources().obtainTypedArray(
				R.array.exerciseList);
		int n = taExerciseArray.length();
		String[][] array = new String[n][];
		for (int i = 0; i < n; ++i) {
			int id = taExerciseArray.getResourceId(i, 0);
			if (id > 0) {
				array[i] = resources.getStringArray(id);
				if (exerciseToDelete.equalsIgnoreCase(array[i][0])) {
					exerciseInList = true;
					break;
				}
			}
		}
		taExerciseArray.recycle(); // Important!
		return exerciseInList;
	}

}
