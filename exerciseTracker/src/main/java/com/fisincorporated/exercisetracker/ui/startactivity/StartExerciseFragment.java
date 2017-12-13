package com.fisincorporated.exercisetracker.ui.startactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Spinner;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.fisincorporated.exercisetracker.ui.logger.ActivityLoggerActivity;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;

import java.util.Locale;

public class StartExerciseFragment extends ExerciseMasterFragment {
    private static final String TAG = "StartExerciseFragment";
    protected Cursor csrLocationAutoComplete;
    protected Cursor csrExerciseAutoComplete;
    private long exerciseRowId = -1;
    private long locationRowId = -1;
    private Spinner spnrExercise;
    private EditText etAdditionalInfo;
    private AutoCompleteTextView actvLocation;
    private Button btnStart;

    private int selectedExercisePosition = -1;
    private static final int DIALOG_SAVE_LOCATION = 20;
    private static final int DIALOG_CONTINUE_ACTIVITY = 10;

    private float minDistanceToLog = 10;
    private int elevationInDistcalcs = 0;
    private LocationExerciseDAO leDAO = null;
    private LocationExerciseRecord ler = null;
    private ExerciseDAO exerciseDAO = null;

    private int logInterval = 0;
    private boolean startPressed = false;
    private ActivityDialogFragment activityDialog = null;
    private OnFocusChangeListener locationOnFocusChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.start_exercise, container, false);
        getReferencedViews(view);
        setUpAutoCompletes();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        actvLocation.setOnFocusChangeListener(locationOnFocusChangeListener);
        if (ler != null && selectedExercisePosition != -1) {
            spnrExercise.setSelection(selectedExercisePosition);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeListeners();
    }

    private void getReferencedViews(View view) {
        spnrExercise = (Spinner) view
                .findViewById(R.id.start_exercise_spnrExercise);

        // get this defined first so you can get colors for actvLocation
        etAdditionalInfo = (EditText) view
                .findViewById(R.id.start_exercise_additional_info);
        actvLocation = (AutoCompleteTextView) view
                .findViewById(R.id.start_exercise_actvLocation);
        // 2.3.4 (and earlier) have problem of white on white or black on black text on
        // autocompletetextview so set colors to the textview colors
        actvLocation.setTextColor(etAdditionalInfo.getTextColors());

        // set spinner listener to display the selected item id
        // The id value is the Exercise._ID value
        spnrExercise.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                exerciseRowId = id;
                getExerciseDetail(exerciseRowId);
                selectedExercisePosition = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle Save Button
        btnStart = (Button) view.findViewById(R.id.start_exercise_btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            public void onClick(View v) {
                if (actvLocation.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter location first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // do whatever needed then start the logging process.
                if (!doesLocationExist()) {
                    startPressed = true;
                    showAddLocationDialog();
                } else {
                    createLer();

                }
            }
        });


        // Add and remove listener in onStop to avoid IllegalStateException: Can not perform this action after onSaveInstanceState
        locationOnFocusChangeListener = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i(TAG, "actvLocation.setOnFocusChangeListener fired");
                if (v.equals(actvLocation) && !hasFocus) {
                    if (!doesLocationExist()) {
                        startPressed = false;
                        showAddLocationDialog();
                    }
                }
            }
        };

    }

    public void showAddLocationDialog() {

        activityDialog = ActivityDialogFragment.newInstance(-1,
                "Do you want to create new location "
                        + actvLocation.getText().toString().trim() + "?",
                R.string.ok, R.string.no, -1);
        activityDialog.setTargetFragment(StartExerciseFragment.this, DIALOG_SAVE_LOCATION);
        activityDialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    public void setUpAutoCompletes() {
        leDAO = new LocationExerciseDAO(databaseHelper);
        fillExerciseAutoComplete();
        fillLocationAutoComplete();
    }

    private void setBtnStartEnabled() {
        if ((locationRowId < 0)
                || (actvLocation.getText().toString().trim().isEmpty())) {
            btnStart.setEnabled(false);
        } else
            btnStart.setEnabled(true);
    }

    private void fillExerciseAutoComplete() {
        csrExerciseAutoComplete = database.query(Exercise.EXERCISE_TABLE,
                new String[]{Exercise._ID, Exercise.EXERCISE}, null, null, null,
                null, Exercise.TIMES_USED + " Desc , " + Exercise.DEFAULT_SORT_ORDER);

        getActivity().startManagingCursor(csrExerciseAutoComplete);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item, csrExerciseAutoComplete,
                new String[]{Exercise.EXERCISE},
                new int[]{android.R.id.text1}, 0);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setCursorToStringConverter(new ExerciseCursorToStringConverter());
        spnrExercise.setAdapter(adapter);
    }

    // This controls what column to place in the edittext when selected. The
    // default textview.tostring, not helpful
    class ExerciseCursorToStringConverter implements SimpleCursorAdapter.CursorToStringConverter {
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(Exercise.EXERCISE));
        }
    }

    private void fillLocationAutoComplete() {
        csrLocationAutoComplete = database.query(ExrcsLocation.LOCATION_TABLE,
                new String[]{ExrcsLocation._ID, ExrcsLocation.LOCATION}, null,
                null, null, null, ExrcsLocation.DEFAULT_SORT_ORDER);
        getActivity().startManagingCursor(csrLocationAutoComplete);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                csrLocationAutoComplete, new String[]{ExrcsLocation.LOCATION},
                new int[]{android.R.id.text1}, 0);

        adapter
                .setCursorToStringConverter(new MyLocationCursorToStringConverter());
        adapter.setFilterQueryProvider(new LocationFilterQueryProvider());
        // actvLocation was defined and assigned in onCreate
        actvLocation.setAdapter(adapter);
    }

    // This controls what column to place in the edittext when selected. The
    // default textview.tostring, not helpful
    class MyLocationCursorToStringConverter implements
            SimpleCursorAdapter.CursorToStringConverter {
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(ExrcsLocation.LOCATION));
        }
    }

    // Used to help auto complete
    // There's a bit of a workaround here, since this function does not handle
    // Cursor refreshing appropriately
    class LocationFilterQueryProvider implements FilterQueryProvider {
        public Cursor runQuery(CharSequence constraint) {
            if ((constraint != null) && (csrLocationAutoComplete != null)) {
                String strWhere = ExrcsLocation.LOCATION + " LIKE ?";
                getActivity().stopManagingCursor(csrLocationAutoComplete);
                csrLocationAutoComplete = database.query(
                        ExrcsLocation.LOCATION_TABLE, new String[]{
                                ExrcsLocation.LOCATION, ExrcsLocation._ID}, strWhere,
                        new String[]{constraint.toString() + "%"}, null, null,
                        ExrcsLocation.DEFAULT_SORT_ORDER);
                getActivity().startManagingCursor(csrLocationAutoComplete);
            }
            return csrLocationAutoComplete;
        }
    }

    private void addLocation() {
        database.beginTransaction();
        try {
            // save new ExrcsLocation and get ExrcsLocation._ID
            ContentValues recordValues = new ContentValues();
            recordValues.put(ExrcsLocation.LOCATION, actvLocation.getText()
                    .toString().trim());

            locationRowId = database.insert(ExrcsLocation.LOCATION_TABLE, null,
                    recordValues);
            database.setTransactionSuccessful();
            Toast.makeText(getActivity(),
                    actvLocation.getText().toString().trim() + " saved.",
                    Toast.LENGTH_LONG).show();
        } finally {
            if (database.inTransaction())
                database.endTransaction();
        }
    }

    private boolean doesLocationExist() {
        // check if ExrcsLocation exists already
        Cursor csr = null;
        try {
            String strLocation = actvLocation.getText().toString().toLowerCase(Locale.getDefault()).trim();
            // SQL Query
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(ExrcsLocation.LOCATION_TABLE);
            queryBuilder.appendWhere("lower(" + ExrcsLocation.LOCATION + ")='"
                    + strLocation + "'");

            // run the query since it's all ready to go
            csr = queryBuilder.query(database, null, null, null, null, null, null);

            if (csr.getCount() == 0) {
                locationRowId = -1;
            } else {
                csr.moveToFirst();
                locationRowId = csr.getLong(csr.getColumnIndex(ExrcsLocation._ID));
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
        if (locationRowId > 0)
            return true;
        else
            return false;
    }

    private void getExerciseDetail(long rowId) {
        Cursor csr = null;
        try {
            csr = database.rawQuery("Select " + Exercise.LOG_DETAIL + ","
                    + Exercise.MIN_DISTANCE_TO_LOG + ","
                    + Exercise.ELEVATION_IN_DIST_CALCS + "," + Exercise.LOG_INTERVAL
                    + " from " + Exercise.EXERCISE_TABLE + " where " + Exercise._ID
                    + " = ? ", new String[]{rowId + ""});

            if (csr.getCount() == 0) {
                minDistanceToLog = 10;
                elevationInDistcalcs = 0;
                logInterval = 60;
            } else {
                csr.moveToFirst();
                minDistanceToLog = csr.getFloat(csr
                        .getColumnIndex(Exercise.MIN_DISTANCE_TO_LOG));
                ;
                elevationInDistcalcs = csr.getInt(csr
                        .getColumnIndex(Exercise.ELEVATION_IN_DIST_CALCS));
                logInterval = csr.getInt(csr.getColumnIndex(Exercise.LOG_INTERVAL));
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == DIALOG_SAVE_LOCATION) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                if (actvLocation.getText() == null || actvLocation.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "A location must be entered/selected before starting.",
                            Toast.LENGTH_LONG).show();
                } else {
                    doAddLocation();
                }
            } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.continuex),
                        Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == DIALOG_CONTINUE_ACTIVITY) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                startActivity();
            } else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
                createNewLer();
                startActivity();
            }
        }
    }

    public void doAddLocation() {
        if (!doesLocationExist()) {
            addLocation();
        }
        if (locationRowId == -1) {
            Toast.makeText(getActivity(),
                    "A location must be entered/selected before starting.",
                    Toast.LENGTH_LONG).show();
        } else {
            if (startPressed) {
                btnStart.performClick();
            }
        }

    }

    private void createLer() {
        // if location/exercise record already exists for today then see if you
        // should continue using it
        if (checkForSameToday()) {
            // show activityDialog and act on response
            Cursor csr = (Cursor) spnrExercise.getSelectedItem();

            ActivityDialogFragment dialog;
            dialog = ActivityDialogFragment.newInstance(-1,
                    "Do you want to continue with prior "
                            + csr.getString(csr.getColumnIndex(Exercise.EXERCISE))
                            + "@" + actvLocation.getText().toString().trim() + "  " + ler.getDescription()
                            + " started earlier today?", R.string.ok, R.string.no, -1);
            dialog.setTargetFragment(StartExerciseFragment.this,
                    DIALOG_CONTINUE_ACTIVITY);
            dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");

        } else {
            // starting off fresh with new record
            createNewLer();
            startActivity();
        }
    }

    private boolean checkForSameToday() {
        ler = new LocationExerciseRecord();
        boolean leFoundForToday = false;
        Cursor csr = null;
        try {
            csr = database.rawQuery("Select " + LocationExercise._ID + " from "
                    + LocationExercise.LOCATION_EXERCISE_TABLE + " where "
                    + LocationExercise.LOCATION_ID + " = ? and "
                    + LocationExercise.EXERCISE_ID + " = ?" + " and DATE("
                    + LocationExercise.START_TIMESTAMP + ") = CURRENT_DATE"
                    + " ORDER BY " + LocationExercise._ID + " DESC", new String[]{
                    locationRowId + "", exerciseRowId + "",});

            if (csr.getCount() == 0) {
                leFoundForToday = false;
            } else {
                csr.moveToFirst();
                // a location/exercise already exists for today, see if we should
                // continue using it
                ler = leDAO.loadLocationExerciseRecordById(csr.getLong(0));
                leFoundForToday = true;
            }

        } catch (SQLException sqle) {
            Log.e(GlobalValues.LOG_TAG,
                    "StartExercise.checkForSameToday:" + sqle.toString());

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
        return leFoundForToday;
    }

    public void createNewLer() {
        ler = new LocationExerciseRecord();
        // create a new record for the activity in the com.fisincorporated.exercisetracker.database
        ler.setExerciseId(exerciseRowId);
        ler.setLocationId(locationRowId);
        ler.setDescription(etAdditionalInfo.getText().toString());
        ler.setLogInterval(logInterval);
        ler.setLogDetail(1);
        database.beginTransaction();
        try {
            leDAO.createLocationExercise(ler);
            exerciseDAO = new ExerciseDAO(databaseHelper);
            exerciseDAO.updateTimesUsed(exerciseRowId, 1);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    private void startActivity() {
        Intent intent = new Intent(getActivity(), ActivityLoggerActivity.class);
        intent.putExtra(LocationExercise.LOCATION_EXERCISE_TABLE, ler);
        // and some general text info not stored on ler
        Cursor csr = (Cursor) spnrExercise.getSelectedItem();
        intent.putExtra(Exercise.EXERCISE,
                csr.getString(csr.getColumnIndex(Exercise.EXERCISE)));
        intent.putExtra(ExrcsLocation.LOCATION, actvLocation.getText().toString()
                .trim());
        intent.putExtra(LocationExercise.DESCRIPTION, etAdditionalInfo.getText().toString());
        intent.putExtra(Exercise.MIN_DISTANCE_TO_LOG, minDistanceToLog);
        intent.putExtra(Exercise.ELEVATION_IN_DIST_CALCS, elevationInDistcalcs);
        startActivity(intent);
    }

    // onBackPressed will trigger focus change on the autocomplete field which
    // then causes IllegalStateException:Can not perform this action after onSaveInstanceState
    //so remove listeners prior to having activities onBackPressed complete
    private void removeListeners() {
        Log.i(TAG, "Setting actvLocation.setOnFocusChangeListener to null");
        actvLocation.setOnFocusChangeListener(null);

    }

}
