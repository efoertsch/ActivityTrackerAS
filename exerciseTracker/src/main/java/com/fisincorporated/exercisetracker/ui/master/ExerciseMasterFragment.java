package com.fisincorporated.exercisetracker.ui.master;



import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

public class ExerciseMasterFragment extends Fragment {
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
	
	public void onDestroy() {
//		if (database != null) {
//			if (database.isOpen())
//				database.close();
//			database = null;
//		}
		super.onDestroy();
	}

//	@Override
//	public void finalize() {
//		if (database != null) {
//			if (database.isOpen())
//					database.close();
//			database = null;
//		}
//	}
}
