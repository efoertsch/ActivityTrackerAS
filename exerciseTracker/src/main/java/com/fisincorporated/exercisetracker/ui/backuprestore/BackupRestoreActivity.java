package com.fisincorporated.exercisetracker.ui.backuprestore;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;


public class BackupRestoreActivity extends ExerciseMasterFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityTitle(R.string.activity_backup_restore);
	}

	protected Fragment createFragment() {
		return new BackupRestoreFragment();
	}

	// added for tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}


}
