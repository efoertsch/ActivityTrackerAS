package com.fisincorporated.ExerciseTracker;

import com.fisincorporated.database.TrackerDatabase.Exercise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ExerciseMaintenance extends ExerciseMasterFragmentActivity {

	public ExerciseMaintenance() {
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	protected Fragment createFragment() {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(Exercise.EXERCISE_TABLE, intent.getParcelableExtra(Exercise.EXERCISE_TABLE ));
		return   ExerciseMaintenanceFragment.newInstance(bundle);

	}

}
