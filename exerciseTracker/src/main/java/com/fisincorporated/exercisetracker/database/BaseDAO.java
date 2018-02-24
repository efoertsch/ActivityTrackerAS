package com.fisincorporated.exercisetracker.database;

import android.database.sqlite.SQLiteDatabase;

public abstract class BaseDAO {
 // Database fields
	protected static SQLiteDatabase database = null;
	protected static TrackerDatabaseHelper dbHelper = null;
	protected static boolean dbIsOpen = false;

	protected BaseDAO(SQLiteDatabase database) {
		this.database = database;
	}

}
