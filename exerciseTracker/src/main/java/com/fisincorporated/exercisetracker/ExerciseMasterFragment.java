package com.fisincorporated.exercisetracker;



import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.interfaces.IHandleSelectedAction;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ExerciseMasterFragment extends Fragment {
	protected TrackerDatabaseHelper databaseHelper = null;
	protected SQLiteDatabase database = null;
	protected Cursor csrUtility;
	protected static String imperialMetric;
	protected static String imperial;
	protected static String feetMeters;
	protected static String milesKm;
	protected static String mphKph;
	protected IHandleSelectedAction callBacks;
	
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDatabaseSetup();
	}
		

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		callBacks = (IHandleSelectedAction) activity;
	}
	public void onDetach(){
		super.onDetach();
		callBacks = null;
	}

	public void getDatabaseSetup() {
		if (databaseHelper == null)
			databaseHelper = TrackerDatabaseHelper
					.getTrackerDatabaseHelper(getActivity().getApplicationContext());
		if (database == null)
			database = databaseHelper.getWritableDatabase();
		if (!database.isOpen())
			database = databaseHelper.getWritableDatabase();
	}

	/**
	 * Find out if to display distances in ft/miles vs m/km
	 */
	protected void findDisplayUnits() {
		imperial = getResources().getString(R.string.imperial);
		imperialMetric = databaseHelper.getProgramOption(database, getResources()
				.getString(R.string.display_units), imperial);
		if (imperialMetric.equalsIgnoreCase(getResources().getString(
				R.string.imperial))) {
			feetMeters = "ft";
			milesKm = "miles";
			mphKph = "mph";
		} else {
			feetMeters = "m";
			milesKm = "km";
			mphKph = "kph";

		}

	}
	
	public void onDestroy() {
		if (database != null) {
			if (database.isOpen())
				database.close();
			database = null;
		}
		super.onDestroy();
	}

	@Override
	public void finalize() {
		if (database != null) {
			if (database.isOpen())
					database.close();
			database = null;
		}
	}
}
