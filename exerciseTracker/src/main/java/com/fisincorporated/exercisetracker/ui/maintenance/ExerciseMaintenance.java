package com.fisincorporated.exercisetracker.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ExerciseMaintenance extends ExerciseMasterFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityTitle(R.string.activity_history);
	}

	
	@Override
	protected Fragment createFragment() {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(Exercise.EXERCISE_TABLE, intent.getParcelableExtra(Exercise.EXERCISE_TABLE ));
		return   ExerciseMaintenanceFragment.newInstance(bundle);

	}

}
