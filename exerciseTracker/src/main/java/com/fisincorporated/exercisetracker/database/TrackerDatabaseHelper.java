package com.fisincorporated.exercisetracker.database;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.*;

public class TrackerDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = GlobalValues.DATABASE_NAME;
	// version 1 - initial implementation
	// version 2 - additional columns added to exercise and location/exercise
	// version 3 - no db change but update exercise usage and use for sorting in start exercise
	// tables
	private static final int DATABASE_VERSION = 3;
	private Context context;
	private static TrackerDatabaseHelper trackerDatabaseHelper = null;

	private TrackerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public static TrackerDatabaseHelper getTrackerDatabaseHelper(Context context) {
		if (trackerDatabaseHelper == null) {
			return new TrackerDatabaseHelper(context.getApplicationContext());
		} else
			return trackerDatabaseHelper;
	}

	@SuppressLint("Override")
	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Turn on referential integrity
		// db.setForeignKeyConstraintsEnabled(true);
		// Table to hold various parameters
		db.execSQL(TrackerDatabase.getCreateProgramSettingTableSQL());

		// Create the ExrcsLocation table
		db.execSQL(TrackerDatabase.getCreateLocationTableSQL());

		// Create the Exercise table(Would prefer Activity but don't
		// want to get confused with Android Activity
		db.execSQL(TrackerDatabase.getCreateExerciseTableSQL());

		// and the associative table
		db.execSQL(TrackerDatabase.getCreateExerciseLocationTableSQL());

		// Create the GPSLog table
		db.execSQL(TrackerDatabase.getCreateGPSLogTableSQL());
		// and index
		db.execSQL(TrackerDatabase.getCreateGPSLogIndex1());

		// create the referential integrity triggers
		// db.execSQL(TrackerDatabase.getCreateTriggerInsertActivity());
		// db.execSQL(TrackerDatabase.getCreateTriggerInsertGPSLog());

		// initial load of tables
		// loadExerciseTableIfNeeded(db);
		loadExerciseTable(db);
		loadProgramSettingTableIfNeeded(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// If you need to add a column
		if (oldVersion < 2) {
			db.execSQL("ALTER TABLE " + Exercise.EXERCISE_TABLE + "  ADD COLUMN "
					+ Exercise.ELEVATION_IN_DIST_CALCS + " INTEGER DEFAULT 0 ");
			db.execSQL("ALTER TABLE " + Exercise.EXERCISE_TABLE + "  ADD COLUMN "
					+ Exercise.MIN_DISTANCE_TO_LOG + " NUMBER DEFAULT 10 ");
			db.execSQL("ALTER TABLE " + LocationExercise.LOCATION_EXERCISE_TABLE
					+ "  ADD COLUMN " + LocationExercise.MAX_SPEED_TO_POINT
					+ " NUMBER NOT NULL DEFAULT 0");
		}
		if (oldVersion < 3) {
			// no change to com.fisincorporated.exercisetracker.database but update useage fields.
			db.execSQL("update " + Exercise.EXERCISE_TABLE  
					+ " set " +  Exercise.TIMES_USED 
					+ " = (select count(*) from " +  LocationExercise.LOCATION_EXERCISE_TABLE
					+ " where " + LocationExercise.LOCATION_EXERCISE_TABLE + "." + LocationExercise.EXERCISE_ID 
					+ " = " + Exercise.EXERCISE_TABLE + "." + Exercise._ID + ") "  ); 
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	public void loadExerciseTableIfNeeded(SQLiteDatabase db) {
		Cursor cursor = null;
		Log.i(GlobalValues.LOG_TAG,
				"TrackerDatabaseHelper.loadExerciseTableIfNeeded db version: "
						+ db.getVersion());
		String[] projection = { Exercise._ID };
		cursor = db.query(Exercise.EXERCISE_TABLE, // The table to query
				projection, // The columns to return
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null); // The sort order
		// if no records load table
		if (!cursor.moveToFirst()) {
			loadExerciseTable(db);
		}
		cursor.close();

	}

	public void loadExerciseTable(SQLiteDatabase db) {
		// picking up array of strings from
		// http://stackoverflow.com/questions/4326037/android-resource-array-of-arrays
		// Note that default log values must be in quotes.
		ContentValues rowValues = new ContentValues();
		Resources res = context.getResources();
		TypedArray ta = res.obtainTypedArray(R.array.exerciseList);
		int n = ta.length();
		String[][] array = new String[n][];
		for (int i = 0; i < n; ++i) {
			int id = ta.getResourceId(i, 0);
			if (id > 0) {
				array[i] = res.getStringArray(id);
				Log.i(GlobalValues.LOG_TAG, "Exercise: " + array[i][0] + "|"
						+ array[i][1] + "|" + array[i][2]);
				rowValues.put(Exercise.EXERCISE, array[i][0]);
				rowValues.put(Exercise.LOG_INTERVAL, Integer.valueOf(array[i][1]));
				rowValues.put(Exercise.DEFAULT_LOG_INTERVAL,
						Integer.valueOf(array[i][1]));
				rowValues.put(Exercise.LOG_DETAIL, Integer.valueOf(array[i][2]));
				rowValues.put(Exercise.MIN_DISTANCE_TO_LOG,
						Float.valueOf(array[i][3]));
				rowValues.put(Exercise.ELEVATION_IN_DIST_CALCS,
						Float.valueOf(array[i][4]));
				db.insert(Exercise.EXERCISE_TABLE, null, rowValues);
			}
		}
		ta.recycle(); // Important!
	}

	public void loadProgramSettingTableIfNeeded(SQLiteDatabase db) {
		String[] projection = { ProgramSetting._ID };
		Cursor cursor = db.query(ProgramSetting.PROGRAM_SETTING_TABLE, // The
																							// table to
																							// query
				projection, // The columns to return
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null); // The sort order
		// if no records load table
		if (!cursor.moveToFirst()) {
			loadProgramSettingTable(db);
		}
		;
		cursor.close();

	}

	public void loadProgramSettingTable(SQLiteDatabase db) {
		// picking up array of strings from
		// http://stackoverflow.com/questions/4326037/android-resource-array-of-arrays
		// Note that default log values must be in quotes.
		ContentValues rowValues = new ContentValues();
		Resources res = context.getResources();
		TypedArray ta = res.obtainTypedArray(R.array.defaultSettings);
		int n = ta.length();
		String[][] array = new String[n][];
		for (int i = 0; i < n; ++i) {
			int id = ta.getResourceId(i, 0);
			if (id > 0) {
				array[i] = res.getStringArray(id);
				Log.i(GlobalValues.LOG_TAG, "Program Setting:" + array[i][0] + "|"
						+ array[i][1]);
				rowValues.put(ProgramSetting.NAME, array[i][0]);
				rowValues.put(ProgramSetting.VALUE, array[i][1]);
				db.insert(ProgramSetting.PROGRAM_SETTING_TABLE, null, rowValues);
			}
		}
		ta.recycle(); // Important!
	}

	public String getProgramOption(SQLiteDatabase db, String key,
			String defaultValue) {
		String value = defaultValue;
		Cursor cursor = db.rawQuery(
				TrackerDatabase.getProgramSettingValueSQL(key), null);
		if (cursor.moveToFirst()) {
			value = cursor.getString(cursor.getColumnIndex(ProgramSetting.VALUE));
		}
		;
		cursor.close();
		return value;
	}

	public boolean setProgramOption(SQLiteDatabase db, String key, String value) {
		try {
			ContentValues values = new ContentValues();
			values.put(ProgramSetting.VALUE, value);
			if (0 == db.update(ProgramSetting.PROGRAM_SETTING_TABLE, values,
					" name = '" + key + "'", null)) {
				values.put(ProgramSetting.NAME, key);
				if (-1 == db.insert(ProgramSetting.PROGRAM_SETTING_TABLE, null,
						values)) {
					Log.e(GlobalValues.LOG_TAG,
							"TrackerDatabaseHelper.setProgramOption. Was not able to add ProgramSetting");
				}
			}
			return true;
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"TrackerDatabaseHelper.setProgramOption exception"
							+ e.toString());
			return false;
		}
	}

	public void createActivitySQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations, int sortOrder) {
		StringBuffer where = new StringBuffer();

		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			where.append(" where ");
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ "."
				+ LocationExercise._ID
				+ ","
				+ Exercise.EXERCISE
				+ ","
				+ ExrcsLocation.LOCATION
				+ ", "
				+ " case when start_timestamp isnull then '"
				+ GlobalValues.NO_DATE
				+ "' "
				+ " else date("
				+ LocationExercise.START_TIMESTAMP
				+ ") "
				// + " || ' ' " + "||" + " time(" + LocationExercise.START_TIMESTAMP
				// + ") "
				+ " end   " + GlobalValues.START_DATE + " ," + "coalesce("
				+ LocationExercise.DESCRIPTION + ",'') description " + ","
				+ LocationExercise.EXERCISE_ID + " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE + " JOIN "
				+ Exercise.EXERCISE_TABLE + " on " + LocationExercise.EXERCISE_ID
				+ " = " + Exercise.EXERCISE_TABLE + "." + Exercise._ID + " JOIN "
				+ ExrcsLocation.LOCATION_TABLE + " on "
				+ LocationExercise.LOCATION_ID + " = "
				+ ExrcsLocation.LOCATION_TABLE + "." + ExrcsLocation._ID
				+ where.toString() + " order by ");
		switch (sortOrder) {
		case 1:
			query.append(Exercise.EXERCISE + " asc");
			break;
		case 2:
			query.append(Exercise.EXERCISE + " desc");
			break;
		case 3:
			query.append(ExrcsLocation.LOCATION + " asc");
			break;
		case 4:
			query.append(ExrcsLocation.LOCATION + " desc");
			break;
		case 5:
			query.append(LocationExercise.START_TIMESTAMP + " asc");
			break;
		case 6:
			query.append(LocationExercise.START_TIMESTAMP + " desc");
			break;
		case 7:
			query.append(LocationExercise.DESCRIPTION + " asc");
			break;
		case 8:
			query.append(LocationExercise.DESCRIPTION + " desc");
			break;
		default:
			query.append(Exercise.EXERCISE + " asc");
			break;
		}
	}

	// replace createExerciseWherePhrase and createLocationWherePhrase
	private void createActivityWherePhrase(StringBuffer wherePhrase,
			ArrayList<String> selections, String columnName) {
		if (selections.size() == 0)
			return;
		if (wherePhrase.length() != 0
				&& !wherePhrase.toString().trim().equalsIgnoreCase("where"))
			wherePhrase.append(" and ");
		for (int i = 0; i < selections.size(); ++i) {
			wherePhrase.append(i == 0 ? columnName + " in ( '"
					+ selections.get(i).trim() + "'" : ",'"
					+ selections.get(i).trim() + "'");
		}
		wherePhrase.append(")");
	}

	// Prior month daily activity
	// First of 2 queries to find date 1 month ago and the number of days since 1 month ago 
	public void getMonthAgoDateSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations) {
		StringBuffer where = new StringBuffer();
		where.append(" where date(" + LocationExercise.START_TIMESTAMP
				+ ") >=  date('now','-1 month') ");
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {

			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ "  min_activity_date "
				+ " , (julianday(date('now')) - julianday(min_activity_date))  time_index "
				+ " from (select " + " substr(min("
				+ LocationExercise.START_TIMESTAMP
				+ "),1,10)  min_activity_date "
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ where.toString() + " ) x");

	}

	
	// Prior month daily activity
	// 2nd of 2 queries that find daily activity by exercise and the 'day index' that the activity occurred 

	public void getDailyActivityDistancesSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations, String startTimeStamp) {
		StringBuffer where = new StringBuffer();
		where.append("where " + LocationExercise.START_TIMESTAMP + " notnull "
				+ " and  date(" + LocationExercise.START_TIMESTAMP + ") >= '"
				+ startTimeStamp + "' ");
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select " + "date(substr("
				+ LocationExercise.START_TIMESTAMP + ",1,10)) activity_date" + ","
				+ Exercise.EXERCISE + "," + "sum( " + LocationExercise.DISTANCE
				+ " ) " + LocationExercise.DISTANCE + "," + " julianday(substr("
				+ LocationExercise.START_TIMESTAMP + ",1,10)) - julianday('"
				+ startTimeStamp + "')  time_index" + " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE + " JOIN "
				+ Exercise.EXERCISE_TABLE + " on " + LocationExercise.EXERCISE_ID
				+ " = " + Exercise.EXERCISE_TABLE + "." + Exercise._ID + " JOIN "
				+ ExrcsLocation.LOCATION_TABLE + " on "
				+ LocationExercise.LOCATION_ID + " = "
				+ ExrcsLocation.LOCATION_TABLE + "." + ExrcsLocation._ID + " "
				+ where.toString() + " group by activity_date, time_index, "
				+ Exercise.EXERCISE + " order by activity_date, time_index, "
				+ Exercise.EXERCISE);
	}
	
	// Weekly activity
	// Get earliest activity date and number of weeks to last activity date
	public void getMinWeekAndNumberOfWeeksSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations) {
		StringBuffer where = new StringBuffer();
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			where.append(" where ");
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		// find sunday of earliest date
		// and number of weeks between current date and earliest date 
		query.append("select "
				+ " datetime(julianday(min(substr(start_timestamp,1,10))) -  strftime('%w', min(start_timestamp)) )  min_activity_date "
				+ ",((julianday(date('now'), 'weekday 0') + case strftime('%w', date('now')) when '0' then 7 else 0 end) "
				+ " - ( julianday(min(substr(start_timestamp,1,10))) - strftime('%w', min(start_timestamp))  )) / 7 time_index"
				+ " from " + LocationExercise.LOCATION_EXERCISE_TABLE + " JOIN "
				+ Exercise.EXERCISE_TABLE + " on " + LocationExercise.EXERCISE_ID
				+ " = " + Exercise.EXERCISE_TABLE + "." + Exercise._ID + " JOIN "
				+ ExrcsLocation.LOCATION_TABLE + " on "
				+ LocationExercise.LOCATION_ID + " = "
				+ ExrcsLocation.LOCATION_TABLE + "." + ExrcsLocation._ID
				+ where.toString());

	}



	// Get weekly activity totals by exercise and the 'week index' of the activity
	public void getWeeklyActivityDistancesSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations, String startTimeStamp) {
		StringBuffer where = new StringBuffer();
		where.append("where " + LocationExercise.START_TIMESTAMP + " notnull "
				+ " and  date(" + LocationExercise.START_TIMESTAMP + ") >= '"
				+ startTimeStamp + "' ");
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ " datetime(julianday( substr(start_timestamp,1,10))  -  strftime('%w', start_timestamp) ) activity_date"
				+ ","
				+ Exercise.EXERCISE
				+ ","
				+ "sum( "
				+ LocationExercise.DISTANCE
				+ " ) "
				+ LocationExercise.DISTANCE
				+ ","
				+ "cast(((julianday(substr(start_timestamp,1,10) ) -  strftime('%w', start_timestamp) ) - julianday('"
				+ startTimeStamp
				+ "'))/7 as int)  time_index"
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ " "
				+ where.toString()
				+ " group by activity_date, time_index, "
				+ Exercise.EXERCISE
				+ " order by activity_date, time_index, "
				+ Exercise.EXERCISE);
	}

	// Monthly totals
	// First of 2 queries to find staring month and number of months of data
	public void getMinDateAndNumMonthsSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations ) {
		StringBuffer where = new StringBuffer();
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			where.append(" where ");
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ " substr(min(start_timestamp),1,7) ||'-01' activity_date "
				+ " ,case "
				+ " when strftime('%Y',date('now')) =  strftime('%Y',min(start_timestamp)) "
				+ "   then  strftime('%m',date('now')) -  strftime('%m',min(start_timestamp)) + 1 "
				+ " else "
				+ " 12 - strftime('%m',min(start_timestamp))  +  ( 12 * (strftime('%Y',date('now')) - strftime('%Y',min(start_timestamp))- 1)) "
				+ " + strftime('%m',date('now'))  end  time_index" 
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ " "
				+ where.toString()
				);
	}
	
	public void getMonthlyActivityDistancesSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations, String startTimeStamp) {
		StringBuffer where = new StringBuffer();
		where.append("where " + LocationExercise.START_TIMESTAMP + " notnull "
				+ " and  date(" + LocationExercise.START_TIMESTAMP + ") >= '"
				+ startTimeStamp + "' ");
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ " substr(start_timestamp,1,7) ||'-01'  activity_date"
				+ ","
				+ Exercise.EXERCISE
				+ ","
				+ "sum( "
				+ LocationExercise.DISTANCE
				+ " ) "
				+ LocationExercise.DISTANCE
				+ " ,case "
				+ " when strftime('%Y',start_timestamp) =  strftime('%Y','" + startTimeStamp + "' ) "
				+ "   then  strftime('%m',start_timestamp) -  strftime('%m','" + startTimeStamp + "' ) "  
				+ " else "
				+ " 12 - strftime('%m','" + startTimeStamp + "')  +  ( 12 * (strftime('%Y',start_timestamp) - strftime('%Y','" + startTimeStamp + "' ) - 1)) "
				+ " + strftime('%m',start_timestamp) end as time_index"
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ " "
				+ where.toString()
				+ " group by activity_date, time_index, "
				+ Exercise.EXERCISE
				+ " order by activity_date, time_index, "
				+ Exercise.EXERCISE);
	}
	
	// Yearly totals
	// First of 2 queries to find staring year and number of years of data
	public void getMinYearAndNumYearsSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations ) {
		StringBuffer where = new StringBuffer();
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			where.append(" where ");
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ " substr(min(start_timestamp),1,4)||'-01-01'   activity_date "
				+ " ,case "
				+ " when strftime('%Y',date('now')) =  strftime('%Y',min(start_timestamp)) "
				+ "   then 1 "
				+ " else "
				+ "  strftime('%Y',date('now')) - strftime('%Y',min(start_timestamp))+ 1 "
				+ "  end  time_index" 
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ " "
				+ where.toString()
				);
	}
	
	public void getYearlyActivityDistancesSQL(StringBuffer query,
			ArrayList<String> selectedExercises,
			ArrayList<String> selectedLocations, String startTimeStamp) {
		StringBuffer where = new StringBuffer();
		where.append("where " + LocationExercise.START_TIMESTAMP + " notnull "
				+ " and  date(" + LocationExercise.START_TIMESTAMP + ") >= '"
				+ startTimeStamp + "' ");
		if (selectedExercises.size() != 0 || selectedLocations.size() != 0) {
			createActivityWherePhrase(where, selectedExercises, Exercise.EXERCISE);
			createActivityWherePhrase(where, selectedLocations,
					ExrcsLocation.LOCATION);
		}
		query.append("select "
				+ " substr(start_timestamp,1,4)  activity_date"
				+ ","
				+ Exercise.EXERCISE
				+ ","
				+ "sum( "
				+ LocationExercise.DISTANCE
				+ " ) "
				+ LocationExercise.DISTANCE
				+ " , strftime('%Y',start_timestamp) -  strftime('%Y','" + startTimeStamp + "' )  as time_index "  
				+ " from "
				+ LocationExercise.LOCATION_EXERCISE_TABLE
				+ " JOIN "
				+ Exercise.EXERCISE_TABLE
				+ " on "
				+ LocationExercise.EXERCISE_ID
				+ " = "
				+ Exercise.EXERCISE_TABLE
				+ "."
				+ Exercise._ID
				+ " JOIN "
				+ ExrcsLocation.LOCATION_TABLE
				+ " on "
				+ LocationExercise.LOCATION_ID
				+ " = "
				+ ExrcsLocation.LOCATION_TABLE
				+ "."
				+ ExrcsLocation._ID
				+ " "
				+ where.toString()
				+ " group by activity_date, time_index, "
				+ Exercise.EXERCISE
				+ " order by activity_date, time_index, "
				+ Exercise.EXERCISE);
	}

	
	
}
