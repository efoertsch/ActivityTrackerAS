package com.fisincorporated.exercisetracker.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;

import java.sql.Timestamp;

public class LocationExerciseDAO extends BaseDAO {

	public LocationExerciseDAO(SQLiteDatabase database) {
		super(database);
	}

	/**
	 * 
	 * @param  ler
	 * @return LocationExerciseRecord For insert the _ID for the record will be
	 *         assigned after the insert
	 */
	public LocationExerciseRecord createLocationExercise(
			LocationExerciseRecord ler) {
		Long rowId = -1l;
		ContentValues values = new ContentValues();
		// mandatory fields
		values.put(LocationExercise.LOCATION_ID, ler.getLocationId().toString());
		values.put(LocationExercise.EXERCISE_ID, ler.getExerciseId().toString());
		// option for insert
		if (ler.getDescription() != null) {
			values.put(LocationExercise.DESCRIPTION, ler.getDescription());
		}
		if (ler.getStartTimestamp() != null) {
			values.put(LocationExercise.START_TIMESTAMP, ler.getStartTimestamp()
					.toString());
		}
		if (ler.getEndTimestamp() != null) {
			values.put(LocationExercise.END_TIMESTAMP, ler.getEndTimestamp()
					.toString());
		}
		if (ler.getDistance() != null) {
			values.put(LocationExercise.DISTANCE, ler.getDistance().toString());
		}
		if (ler.getAverageSpeed() != null) {
			values.put(LocationExercise.AVERAGE_SPEED, ler.getAverageSpeed()
					.toString());
		}
		if (ler.getStartAltitude() != null) {
			values.put(LocationExercise.START_ALTITUDE, ler.getStartAltitude()
					.toString());
		}
		if (ler.getEndAltitude() != null) {
			values.put(LocationExercise.END_ALTITUDE, ler.getEndAltitude()
					.toString());
		}
		if (ler.getAltitudeGained() != null) {
			values.put(LocationExercise.ALTITUDE_GAINED, ler.getAltitudeGained()
					.toString());
		}
		if (ler.getAltitudeLost() != null) {
			values.put(LocationExercise.ALTITUDE_LOST, ler.getAltitudeLost()
					.toString());
		}
		if (ler.getStartLatitude() != null) {
			values.put(LocationExercise.START_LATITUDE, ler.getStartLatitude()
					.toString());
		}
		if (ler.getStartLongitude() != null) {
			values.put(LocationExercise.START_LONGITUDE, ler.getStartLongitude()
					.toString());
		}
		if (ler.getEndLatitude() != null) {
			values.put(LocationExercise.END_LATITUDE, ler.getEndLatitude()
					.toString());
		}
		if (ler.getEndLongitude() != null) {
			values.put(LocationExercise.END_LONGITUDE, ler.getEndLongitude()
					.toString());
		}
		if (ler.getLogInterval() != null) {
			values.put(LocationExercise.LOG_INTERVAL, ler.getLogInterval()
					.toString());
		}
		if (ler.getLogDetail() != null) {
			values.put(LocationExercise.LOG_DETAIL, ler.getLogDetail().toString());
		}
		if (ler.getMaxSpeedToPoint() != null) {
			values.put(LocationExercise.MAX_SPEED_TO_POINT, ler
					.getMaxSpeedToPoint().toString());
		}

		if (ler.getCurrentAltitude() != null) {
			values.put(LocationExercise.CURRENT_ALTITUDE, ler
					.getCurrentAltitude().toString());
		}

		if (ler.getMinAltitude() != null) {
			values.put(LocationExercise.MIN_ALTITUDE, ler
					.getMinAltitude().toString());
		}

		if (ler.getMaxAltitude() != null) {
			values.put(LocationExercise.MAX_ALTITUDE, ler
					.getMaxAltitude().toString());
		}

		if (ler.getTimezone() != null) {
			values.put(LocationExercise.TIMEZONE, ler.getTimezone());
		}

		if (ler.getGmtHourOffset() != null) {
			values.put(LocationExercise.GMT_HOUR_OFFSET, ler.getGmtHourOffset());
		}

		if (ler.getGmtMinuteOffset() != null) {
			values.put(LocationExercise.GMT_MINUTE_OFFSET, ler.getGmtMinuteOffset());
		}


		try {
			rowId = database.insert(LocationExercise.LOCATION_EXERCISE_TABLE,
					null, values);
			ler.set_id(rowId);

		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.createLocationExercise SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.createLocationExercise exception: "
							+ e.toString());
		} 
		//finally {close();	}

		return ler;

	}

	/**
	 * 
	 * @param ler
	 *           The _id must be assigned in ler prior to calling this method
	 */
	public void updateLocationExercise(LocationExerciseRecord ler) {
		Long rowId = ler.get_id();
		ContentValues values = new ContentValues();
		// optional for update
		if (ler.getDescription() != null) {
			values.put(LocationExercise.DESCRIPTION, ler.getDescription());
		}
		if (ler.getStartTimestamp() != null) {
			values.put(LocationExercise.START_TIMESTAMP, ler.getStartTimestamp()
					.toString());
		}
		if (ler.getEndTimestamp() != null) {
			values.put(LocationExercise.END_TIMESTAMP, ler.getEndTimestamp()
					.toString());
		}
		if (ler.getDistance() != null) {
			values.put(LocationExercise.DISTANCE, ler.getDistance().toString());
		}
		if (ler.getAverageSpeed() != null) {
			values.put(LocationExercise.AVERAGE_SPEED, ler.getAverageSpeed()
					.toString());
		}
		if (ler.getStartAltitude() != null) {
			values.put(LocationExercise.START_ALTITUDE, ler.getStartAltitude()
					.toString());
		}
		if (ler.getEndAltitude() != null) {
			values.put(LocationExercise.END_ALTITUDE, ler.getEndAltitude()
					.toString());
		}
		if (ler.getAltitudeGained() != null) {
			values.put(LocationExercise.ALTITUDE_GAINED, ler.getAltitudeGained()
					.toString());
		}
		if (ler.getAltitudeLost() != null) {
			values.put(LocationExercise.ALTITUDE_LOST, ler.getAltitudeLost()
					.toString());
		}
		if (ler.getStartLatitude() != null) {
			values.put(LocationExercise.START_LATITUDE, ler.getStartLatitude()
					.toString());
		}
		if (ler.getStartLongitude() != null) {
			values.put(LocationExercise.START_LONGITUDE, ler.getStartLongitude()
					.toString());
		}
		if (ler.getEndLatitude() != null) {
			values.put(LocationExercise.END_LATITUDE, ler.getEndLatitude()
					.toString());
		}
		if (ler.getEndLongitude() != null) {
			values.put(LocationExercise.END_LONGITUDE, ler.getEndLongitude()
					.toString());
		}
		if (ler.getLogInterval() != null) {
			values.put(LocationExercise.LOG_INTERVAL, ler.getLogInterval()
					.toString());
		}
		if (ler.getLogDetail() != null) {
			values.put(LocationExercise.LOG_DETAIL, ler.getLogDetail().toString());
		}

		if (ler.getMaxSpeedToPoint() != null) {
			values.put(LocationExercise.MAX_SPEED_TO_POINT, ler
					.getMaxSpeedToPoint().toString());
		}

		if (ler.getCurrentAltitude() != null) {
			values.put(LocationExercise.CURRENT_ALTITUDE, ler
					.getCurrentAltitude().toString());
		}

		if (ler.getMinAltitude() != null) {
			values.put(LocationExercise.MIN_ALTITUDE, ler
					.getMinAltitude().toString());
		}

		if (ler.getMaxAltitude() != null) {
			values.put(LocationExercise.MAX_ALTITUDE, ler
					.getMaxAltitude().toString());
		}

		if (ler.getTimezone() != null) {
			values.put(LocationExercise.TIMEZONE, ler
					.getTimezone());
		}

		if (ler.getGmtHourOffset() != null) {
			values.put(LocationExercise.GMT_HOUR_OFFSET, ler.getGmtHourOffset().toString());
		}

		if (ler.getGmtMinuteOffset() != null) {
			values.put(LocationExercise.GMT_MINUTE_OFFSET, ler.getGmtMinuteOffset().toString());
		}

		try {
			database.update(LocationExercise.LOCATION_EXERCISE_TABLE, values,
					" _id = ?", new String[] { rowId.toString() });
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.updateLocationExercise SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.updateLocationExercise exception: "
							+ e.toString());
		} 
		//finally {	close(); 		}
	}

	public int deleteLocationExercise(Long rowId) {
		int count = 0;
		try {
			count = database.delete(LocationExercise.LOCATION_EXERCISE_TABLE,
					LocationExercise._ID + " = " + rowId, null);
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.deleteLocationExercise SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.deleteLocationExercise exception: "
							+ e.toString());
		}
		//finally{	close();	}
		 return count;
	}

	public void deleteLocationExercise(LocationExerciseRecord ler) {
		deleteLocationExercise(ler.get_id());
	}

	public LocationExerciseRecord loadLocationExerciseRecordById(double id) {
		LocationExerciseRecord ler = new LocationExerciseRecord();
		Cursor csr = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(LocationExercise.LOCATION_EXERCISE_TABLE);

			// run the query since it's all ready to go
			csr = queryBuilder.query(database,
					LocationExercise.getLocationExerciseColumnNames, "  _id = ?",
					new String[] { id + "" }, null, null, null);

			if (csr.getCount() == 0) {
				ler.set_id(-1);
			} else {
				csr.moveToFirst();
				loadLocationExerciseRecord(csr, ler);

			}

		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.loadLocationExerciseRecordById SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.loadLocationExerciseRecordById exception: "
							+ e.toString());

		} finally {
			try {	if (csr != null && !csr.isClosed()) {csr.close();	}	} catch (SQLException sqle) {	;	}
			//close();
		}
		return ler;

	}

	// The only mandatory column in the cursor must be _id
	// the cursor should already be pointing to the row to read.
	public void loadLocationExerciseRecord(Cursor csr, LocationExerciseRecord ler) {
		try {
			for (int columnIndex = 0; columnIndex <= csr.getColumnCount() - 1; ++columnIndex) {
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise._ID)) {
					ler.set_id(csr.getLong(columnIndex));
					continue;
				}

				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.LOCATION_ID)) {
					if (csr.getString(columnIndex) != null) {
						ler.setLocationId(csr.getLong(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.EXERCISE_ID)) {
					if (csr.getString(columnIndex) != null) {
						ler.setExerciseId(csr.getLong(columnIndex));
					}
					continue;
				}

				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.DESCRIPTION)) {
					if (csr.getString(columnIndex) != null) {
						ler.setDescription(csr.getString(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.START_TIMESTAMP)) {
					if (csr.getString(columnIndex) != null) {
						ler.setStartTimestamp(Timestamp.valueOf(csr
								.getString(columnIndex)));
					}
					continue;

				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.END_TIMESTAMP)) {
					if (csr.getString(columnIndex) != null) {
						ler.setEndTimestamp(Timestamp.valueOf(csr
								.getString(columnIndex)));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.DISTANCE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setDistance(csr.getInt(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.AVERAGE_SPEED)) {
					if (csr.getString(columnIndex) != null) {
						ler.setAverageSpeed(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.START_ALTITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setStartAltitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.END_ALTITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setEndAltitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.ALTITUDE_GAINED)) {
					if (csr.getString(columnIndex) != null) {
						ler.setAltitudeGained(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.ALTITUDE_LOST)) {
					if (csr.getString(columnIndex) != null) {
						ler.setAltitudeLost(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.START_LATITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setStartLatitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.START_LONGITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setStartLongitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.END_LATITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setEndLatitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.END_LONGITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setEndLongitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.LOG_INTERVAL)) {
					if (csr.getString(columnIndex) != null) {
						ler.setLogInterval(csr.getInt(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.LOG_DETAIL)) {
					if (csr.getString(columnIndex) != null) {
						ler.setLogDetail(csr.getInt(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.MAX_SPEED_TO_POINT)) {
					if (csr.getString(columnIndex) != null) {
						ler.setMaxSpeedToPoint(csr.getFloat(columnIndex));
					}
					continue;
				}

				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.MIN_ALTITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setMinAltitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.MAX_ALTITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setMaxAltitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.CURRENT_ALTITUDE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setCurrentAltitude(csr.getFloat(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.TIMEZONE)) {
					if (csr.getString(columnIndex) != null) {
						ler.setTimezone(csr.getString(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.GMT_HOUR_OFFSET)) {
					if (csr.getString(columnIndex) != null) {
						ler.setGmtHourOffset(csr.getInt(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						LocationExercise.GMT_MINUTE_OFFSET)) {
					if (csr.getString(columnIndex) != null) {
						ler.setGmtMinuteOffset(csr.getInt(columnIndex));
					}
					continue;
				}

			}
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.loadLocationExerciseRecord:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.loadLocationExerciseRecord:" + e.toString());
		}
	}

	// convenience so you don't need to create another object/query to get
	// location for this activity
	public String getLocation(Long locationId) {
		String location = null;
		Cursor csr = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(ExrcsLocation.LOCATION_TABLE);
			// run the query since it's all ready to go
			csr = queryBuilder.query(database,
					new String[] { ExrcsLocation.LOCATION }, "  _id = ?",
					new String[] { locationId + "" }, null, null, null);

			if (csr.getCount() == 0) {
				location = "";
			} else {
				csr.moveToFirst();
				location = csr.getString(0);
		}
			

		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.getLocation SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.getLocation exception: " + e.toString());
		} finally {
			try {	if (csr != null && !csr.isClosed()) {csr.close();	}	} catch (SQLException sqle) {	;	}
			//close();
		}
		return location;

	}

	// convenience so you don't need to create another object/query to get
	// location for this activity
	public String getExercise(Long exerciseId) {
		String exercise = null;
		Cursor csr = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Exercise.EXERCISE_TABLE);
			// run the query since it's all ready to go
			csr = queryBuilder.query(database, new String[] { Exercise.EXERCISE },
					"  _id = ?", new String[] { exerciseId + "" }, null, null, null);

			if (csr.getCount() == 0) {
				exercise = "";
			} else {
				csr.moveToFirst();
				exercise = csr.getString(0);

			}
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.getExercise SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"LocationExerciseDAO.getExercise exception: " + e.toString());
		} finally {
			try {	if (csr != null && !csr.isClosed()) {csr.close();	}	} catch (SQLException sqle) {	;	}
			//close();
		}
		return exercise;

	}

}
