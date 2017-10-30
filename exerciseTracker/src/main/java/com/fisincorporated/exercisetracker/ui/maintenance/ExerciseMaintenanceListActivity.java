package com.fisincorporated.exercisetracker.ui.maintenance;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ExerciseMaintenanceListActivity extends ExerciseMasterFragmentActivity implements IExerciseCallbacks {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.exercise_list);
    }

    @Override
    protected Fragment createFragment() {
        return new ExerciseMaintenanceListFragment();
    }

    // for tablet implementation
    public void onExerciseSelected(int exerciseId, int position) {
        Fragment newDetail;
        if (findViewById(R.id.detailFragmentContainer) == null) {
            // start info from bundle to load to intent and start instance of
            // ExerciseMaintenancePagerActivity
            if (exerciseId == -1) {
                // this is for an ADD of new exercise
                Intent intent = new Intent(this, ExerciseMaintenanceDetailActivity.class);
                ExerciseRecord exerciseRecord = new ExerciseRecord();
                intent.putExtra(Exercise.EXERCISE_TABLE, exerciseRecord);
                startActivity(intent);
            } else {
                // this is for update/delete of existing exercise, use view pager
                Intent intent = new Intent(this, ExerciseMaintenancePagerActivity.class);
                intent.putExtra(GlobalValues.CURSOR_POSITION, position);
                startActivity(intent);
            }
        } else {
            // tablet version
            // display the activity info in the detailfragment container
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Bundle args = new Bundle();
            args.putParcelable(Exercise.EXERCISE_TABLE,
                    new ExerciseDAO(databaseHelper).loadExerciseRecordById(exerciseId));
            args.putInt(GlobalValues.CURSOR_POSITION, position);
            newDetail = ExerciseMaintenanceDetailFragment.newInstance(args);
            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

}
