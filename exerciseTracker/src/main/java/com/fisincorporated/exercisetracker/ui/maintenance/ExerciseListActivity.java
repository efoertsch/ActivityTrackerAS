package com.fisincorporated.exercisetracker.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.fisincorporated.exercisetracker.ExerciseListFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ExerciseListActivity extends ExerciseMasterFragmentActivity implements
        ExerciseListFragment.Callbacks {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityTitle(R.string.exercise_list);
	}

	@Override
	protected Fragment createFragment() {
		return new ExerciseListFragment();
	}

	// for tablet implementation
	public void onExerciseSelected(Bundle args) {
		Fragment newDetail = null;
		if (findViewById(R.id.detailFragmentContainer) == null) {
			// start info from bundle to load to intent and start instance of
			// ExercisePagerActivity
			if (((ExerciseRecord) args.getParcelable(Exercise.EXERCISE_TABLE))
					.get_id() == -1) {
				// this is for and ADD of new exercise
				Intent intent = new Intent(this, ExerciseMaintenance.class);
				intent.putExtra(Exercise.EXERCISE_TABLE,
						(ExerciseRecord) args.getParcelable(Exercise.EXERCISE_TABLE));
				startActivity(intent);
			} else {
				// this is for update/delete of existing exercise, use view pager
				Intent intent = new Intent(this, ExercisePagerActivity.class);
				intent.putExtra(GlobalValues.CURSOR_POSITION,
						args.getInt(GlobalValues.CURSOR_POSITION));
				startActivity(intent);
			}
		} else {
			// tablet version
			// display the activity info in the detailfragment container
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			newDetail = ExerciseMaintenanceFragment.newInstance(args);
			if (oldDetail != null) {
				ft.remove(oldDetail);
			}
			ft.add(R.id.detailFragmentContainer, newDetail);
			ft.addToBackStack(null);
			ft.commit();
		}
	}

}
