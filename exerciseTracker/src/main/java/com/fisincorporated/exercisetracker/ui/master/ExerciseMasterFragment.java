package com.fisincorporated.exercisetracker.ui.master;



import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.interfaces.IHandleSelectedAction;

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
		Resources res = getResources();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		imperialMetric = sharedPref.getString(res.getString(R.string.display_units), res.getString(R.string.imperial));
		if (imperialMetric.equalsIgnoreCase(res.getString(R.string.imperial))) {
			feetMeters = res.getString(R.string.feet_abbrev);
			milesKm = res.getString(R.string.miles);
			mphKph = res.getString(R.string.miles_per_hour_abbrev);
		} else {
			feetMeters = res.getString(R.string.meters_abbrev);
			milesKm = res.getString(R.string.kilometers_abbrev);
			mphKph = res.getString(R.string.kilometers_per_hours_abbrev);
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
