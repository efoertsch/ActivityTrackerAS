package com.fisincorporated.exercisetracker;

import android.support.v4.app.Fragment;
 

public class BackupRestore extends ExerciseMasterFragmentActivity {

	protected Fragment createFragment() {
		return new BackupRestoreFragment();
	}

	// added for tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}


}
