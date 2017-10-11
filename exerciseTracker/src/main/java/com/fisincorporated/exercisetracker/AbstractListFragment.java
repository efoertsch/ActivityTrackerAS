package com.fisincorporated.exercisetracker;

import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public abstract class AbstractListFragment extends ListFragment {
	protected TrackerDatabaseHelper databaseHelper = null;
	protected SQLiteDatabase database = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
// use by implementing class
	public void getDatabaseSetup() {
		if (databaseHelper == null)
			databaseHelper = TrackerDatabaseHelper
					.getTrackerDatabaseHelper(getActivity().getApplicationContext());
		if (database == null)
			database = databaseHelper.getWritableDatabase();
		if (!database.isOpen())
			database = databaseHelper.getWritableDatabase();
	}
	
	
	public void onDestroy() {
		if (database != null) {
			if (database.isOpen()){
				database.close();
			}
			database = null;
		}
		super.onDestroy();
	}
	
	
	@Override
	public void finalize() {
		if (database != null) {
			if (database.isOpen()){
				database.close();
			}
			database = null;
		}
	}
}
