package com.fisincorporated.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.fisincorporated.ExerciseTracker.GlobalValues;
import com.fisincorporated.database.TrackerDatabase.GPSLog;


public class GPSLogDAO extends BaseDAO {
 
	public GPSLogDAO(TrackerDatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

 
	/**
	 * 
	 * @param GPSLogRecord
	 * @return GPSLogRecord For insert the _ID for the record will be
	 *         assigned after the insert
	 */
	public GPSLogRecord createGPSLogRecord(
			GPSLogRecord gpslr) {
		Long rowId = -1l;
		ContentValues values = new ContentValues();
		// all fields mandatory fields except for _ID 
		values.put(GPSLog.LOCATION_EXERCISE_ID, Long.toString(gpslr.getLocationExerciseId()));
		values.put(GPSLog.LATITUDE, Float.toString(gpslr.getLatitude()));
		values.put(GPSLog.LONGITUDE, Float.toString(gpslr.getLongitude()));
		values.put(GPSLog.ELEVATION,  Integer.toString(gpslr.getElevation()));
		values.put(GPSLog.TIMESTAMP, gpslr.getTimestamp() );
		values.put(GPSLog.DISTANCE_FROM_LAST_POINT, gpslr.getDistanceFromLastPoint());

		try{ 
		if (!dbIsOpen) {
			open();
		}
		rowId = database.insert(GPSLog.GPSLOG_TABLE, null,
				values);
		gpslr.set_id(rowId);
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"GPSLogDAO.createGPSLogRecord SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"GPSLogDAO.createGPSLogRecord exception: "
							+ e.toString());
		}
		 //finally {close();}
		return gpslr;
		
		
	}

	public void createGPSLogRecord(ArrayList<GPSLogRecord> gpslrArray) {
		 for (int i = 0; i < gpslrArray.size(); ++i) {
			 createGPSLogRecord(gpslrArray.get(i));
		 }
		
			}
	/**
	 * 
	 * @param ler
	 *           The _id must be assigned in ler prior to calling this method
	 */
	public void updateGPSLog(GPSLogRecord gpslr) {
		Long rowId = gpslr.get_id();
		ContentValues values = new ContentValues();
		values.put(GPSLog.LOCATION_EXERCISE_ID, gpslr.getLocationExerciseId());
		values.put(GPSLog.LATITUDE, Float.toString(gpslr.getLatitude()));
		values.put(GPSLog.LONGITUDE, Float.toString(gpslr.getLongitude()));
		values.put(GPSLog.ELEVATION,  Integer.toString(gpslr.getElevation()));
		values.put(GPSLog.TIMESTAMP, gpslr.getTimestamp() );
		values.put(GPSLog.DISTANCE_FROM_LAST_POINT, gpslr.getDistanceFromLastPoint());

		try{
		if (!dbIsOpen) {
			open();
		}
		
		database.update(GPSLog.GPSLOG_TABLE, values,
				"where _id = ?", new String[] { rowId.toString() });
	} catch (SQLException sqle) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.updateGPSLog SQL exception:"
						+ sqle.toString());
	} catch (Exception e) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.updateGPSLog exception: "
						+ e.toString());
	}
	// finally {close();}
	}

	public int  deleteGPSLog(Long rowId) {
		int count = 0;
		try{ 
		if (!dbIsOpen) {
			open();
		}

		database.delete(GPSLog.GPSLOG_TABLE,
				GPSLog._ID + " = " + rowId, null);
	} catch (SQLException sqle) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.deleteGPSLog SQL exception:"
						+ sqle.toString());
	} catch (Exception e) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.deleteGPSLog exception: "
						+ e.toString());
	}
	// finally {close();}
		return count;
	}
   
	
	public int deleteGPSLog(GPSLogRecord gpslr) {
		return deleteGPSLog(gpslr.get_id());
	}
	
	
	
	/**
	 * 
	 * @param locationExerciseRowid
	 */
	public int deleteByLocationExerciseRowid(long locationExerciseRowid){
		int count = 0;
		try {
		if (!dbIsOpen) {
			open();
		}
		count = database.delete(GPSLog.GPSLOG_TABLE,
				GPSLog.LOCATION_EXERCISE_ID + " = " + locationExerciseRowid, null);
	} catch (SQLException sqle) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.deleteByLocationExerciseRowid SQL exception:"
						+ sqle.toString());
	} catch (Exception e) {
		Log.e(GlobalValues.LOG_TAG,
				"GPSLogDAO.deleteByLocationExerciseRowid exception: "
						+ e.toString());
	}
	// finally {close();}
		return count;
	}
	
	public int deleteGPSLogbyLerRowId(long lerRowId) {
		int count = 0;
		try {
			if (!dbIsOpen) {
				open();
			}
			count = database.delete(GPSLog.GPSLOG_TABLE,
					GPSLog.LOCATION_EXERCISE_ID + " = " + lerRowId, null);

		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"GPSLogDAO.deleteGPSLogbyLerRowId SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"GPSLogDAO.deleteGPSLogbyLerRowId exception: " + e.toString());
		} 
		 //finally {close();	}
		return count;
	}
	public ArrayList<GPSLogRecord> getGPSLog(Long lerRowId){
		Cursor csr;
		ArrayList<GPSLogRecord> gpslrArray = new ArrayList<GPSLogRecord>();
		if (!dbIsOpen) {
			open();
		}
		
		csr  = database.query(GPSLog.GPSLOG_TABLE,
				new String[] { GPSLog._ID,GPSLog.LATITUDE,GPSLog.LONGITUDE
				,GPSLog.ELEVATION, GPSLog.TIMESTAMP,GPSLog.DISTANCE_FROM_LAST_POINT }
				, " location_exercise_id = ? ", new String[]{""+lerRowId }, null, null, GPSLog.DEFAULT_SORT_ORDER);
 		if (csr.getCount() == 0) {
			return gpslrArray;
		} else {
			csr.moveToFirst();
			while (!csr.isAfterLast()){
				gpslrArray.add(new GPSLogRecord(
					csr.getLong(csr.getColumnIndex(GPSLog._ID)),
					lerRowId,
					csr.getFloat(csr.getColumnIndex(GPSLog.LATITUDE)),
					csr.getFloat(csr.getColumnIndex(GPSLog.LONGITUDE)),
					csr.getInt(csr.getColumnIndex(GPSLog.ELEVATION)),
					csr.getString(csr.getColumnIndex(GPSLog.TIMESTAMP)),
					csr.getInt(csr.getColumnIndex(GPSLog.DISTANCE_FROM_LAST_POINT))));
				}
				csr.moveToNext();
			}
			csr.close();
		return gpslrArray;
	}
	 

 
}
