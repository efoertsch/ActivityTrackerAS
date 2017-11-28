package com.fisincorporated.exercisetracker.ui.history;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

public class DeleteHistoryFragment extends AbstractActivityHistoryFragment implements IHistoryDeleteCallbacks {

    private Button btnDelete = null;

    private LocationExerciseDAO leDAO = null;
    private GPSLogDAO gpslrDAO = null;
    private static final int TYPE_OF_DELETE_REQUESTCODE = 1;
    private static final int CONFIRM_DELETE_REQUESTCODE = 2;
    private int deleteDetailType = 0;

    protected RecyclerView recyclerView;

    private ActivityHistoryDeleteAdapter activityHistoryDeleteAdapter;

    protected HashSet<Long> deleteSet = new HashSet<Long>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreSortFilter(getArguments());

        View view = inflater.inflate(R.layout.activity_history_delete_list,
                container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.activity_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        btnDelete = (Button) view
                .findViewById(R.id.activity_history_delete_list_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            public void onClick(View v) {
                showDeleteDialog1();
            }

        });

        registerForContextMenu(view
                .findViewById(R.id.activity_history_delete_columns));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDeleteCountOnButtion();

    }

    private void showDeleteDialog1() {
        ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                R.string.what_do_you_want_to_delete,
                R.string.delete_complete_activity, R.string.delete_only_gps_detail,
                R.string.cancel);
        dialog.setTargetFragment(DeleteHistoryFragment.this,
                TYPE_OF_DELETE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    private void showDeleteDialog2() {
        ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                (deleteDetailType == 1) ? R.string.press_yes_to_delete_selected_activities_or_no_to_cancel : R.string.press_yes_to_delete_GPS_delail_or_no_to_cancel,
                        R.string.yes, R.string.no, -1);
        dialog.setTargetFragment(DeleteHistoryFragment.this,
                CONFIRM_DELETE_REQUESTCODE);
        dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    private void setDeleteCountOnButtion() {
        if (deleteSet.size() > 0) {
            btnDelete.setText(getResources().getText(R.string.delete) + "("
                    + deleteSet.size() + ")");
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
        Long key;
        // May want to delete activity that is currently running
        GPSLocationManager gpsLocationManager = GPSLocationManager.get(getActivity());
        long currentLerId = gpsLocationManager.getCurrentLer();
        if (leDAO == null) {
            leDAO = new LocationExerciseDAO(databaseHelper);
        }
        if (gpslrDAO == null) {
            gpslrDAO = new GPSLogDAO(databaseHelper);
        }
        database.beginTransaction();
        try {
            Iterator<Long> iter = deleteSet.iterator();
            while (iter.hasNext()) {
                key = iter.next();
                if (key == currentLerId){
                    gpsLocationManager.stopTrackingLer();
                }
                gpslrDAO.deleteGPSLogbyLerRowId(key);
                if (deleteDetailType == 1) {
                    leDAO.deleteLocationExercise(key);
                }
                deleteSet.remove(0);
            }
            deleteSet.clear();
            setDeleteCountOnButtion();
            database.setTransactionSuccessful();
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }

    }

    // LoaderCallBacks interface methods
    // #2
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        if (activityHistoryDeleteAdapter == null) {
            activityHistoryDeleteAdapter = new ActivityHistoryDeleteAdapter(getContext(),new WeakReference<IHistoryDeleteCallbacks>(this));
            recyclerView.setAdapter(activityHistoryDeleteAdapter);
        }
        activityHistoryDeleteAdapter.swapCursor(cursor);
    }

    // #3
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is reset, we need to clear out the
        //current cursor from the adapter.
        activityHistoryDeleteAdapter.swapCursor(null);
    }

    public void deleteThisActivity(ActivityHistorySummary activityHistorySummary) {
        Long id = activityHistorySummary.getRow_id();
        if (deleteSet.contains(activityHistorySummary.getRow_id())){
            deleteSet.remove(id);
        }
        else {
            deleteSet.add(id);
        }

        setDeleteCountOnButtion();
    };

    public boolean isSetToDelete(ActivityHistorySummary activityHistorySummary) {
        return deleteSet.contains(activityHistorySummary.getRow_id());
    }

}
