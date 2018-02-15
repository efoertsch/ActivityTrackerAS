package com.fisincorporated.exercisetracker.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class BaseDAO {
 // Database fields
	protected static SQLiteDatabase database = null;
	protected static TrackerDatabaseHelper dbHelper = null;
	protected static boolean dbIsOpen = false;

	//TODO set up Dagger injection
	protected BaseDAO() {
		if(dbHelper == null) {
			dbHelper = TrackerDatabaseHelper.getTrackerDatabaseHelper();
		}
	}
	public void open() throws SQLException {
		if (database == null || !database.isOpen()) {
			database = dbHelper.getWritableDatabase();
			dbIsOpen = true;
		}
	}

}
