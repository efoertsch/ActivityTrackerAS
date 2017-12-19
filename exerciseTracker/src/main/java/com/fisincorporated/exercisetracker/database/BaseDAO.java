package com.fisincorporated.exercisetracker.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class BaseDAO {
 // Database fields
	protected static SQLiteDatabase database = null;
	protected static TrackerDatabaseHelper dbHelper = null;
	protected static boolean dbIsOpen = false;

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
 
//	public void close() {
//		try {
//			dbHelper.close();
//			dbIsOpen = false;
//		} catch (SQLException sqle) {
//			Log.e(GlobalValues.LOG_TAG, "LocationExerciseDAO.close SQL exception:"
//					+ sqle.toString());
//		} catch (Exception e) {
//			Log.e(GlobalValues.LOG_TAG, "LocationExerciseDAO.close exception: "
//					+ e.toString());
//		}
//	}
//	public void finalize(){
//		if (database != null){
//			database.close();
//		}
//	}

}
