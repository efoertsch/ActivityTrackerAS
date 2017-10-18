package com.fisincorporated.exercisetracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ProgramOptions extends ExerciseMasterFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityTitle(R.string.activity_options);
	}

	@Override
	protected Fragment createFragment() {
		return   new ProgramOptionsFragment();
	}


}