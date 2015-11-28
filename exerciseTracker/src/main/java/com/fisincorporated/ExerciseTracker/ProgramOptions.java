package com.fisincorporated.ExerciseTracker;

import android.support.v4.app.Fragment;

public class ProgramOptions extends ExerciseMasterFragmentActivity {

	public ProgramOptions() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Fragment createFragment() {
		return   new ProgramOptionsFragment();
	}


}