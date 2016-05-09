package com.fisincorporated.ExerciseTracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class FunctionListActivity extends ExerciseMasterFragmentActivity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setDisplayHomeAsUpEnabled(false);
		mAppBarLayout.setExpanded(true);
	}

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new Functions();
	}

}
