package com.fisincorporated.exercisetracker.ui.master;



import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

import dagger.android.support.DaggerFragment;

public class ExerciseMasterFragment extends DaggerFragment {
	protected TrackerDatabaseHelper databaseHelper = null;
	protected SQLiteDatabase database = null;
	protected Cursor csrUtility;
	protected IHandleSelectedAction callBacks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDatabaseSetup();
	}

	@Override
	public void onAttach(Context context){
		super.onAttach(context);
		callBacks = (IHandleSelectedAction) context;
	}
	public void onDetach(){
		super.onDetach();
		callBacks = null;
	}

	//TODO user Dagger injection
	public void getDatabaseSetup() {
		if (databaseHelper == null) {
			databaseHelper = TrackerDatabaseHelper.getTrackerDatabaseHelper();
		}
		if (database == null) {
			database = databaseHelper.getDatabase();
		}

		if (!database.isOpen()) {
			database = databaseHelper.getWritableDatabase();
		}
	}

}
