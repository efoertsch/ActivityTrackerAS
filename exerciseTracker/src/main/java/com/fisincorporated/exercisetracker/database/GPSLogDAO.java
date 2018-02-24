package com.fisincorporated.exercisetracker.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;

import java.util.ArrayList;


public class GPSLogDAO extends BaseDAO {

    public GPSLogDAO(SQLiteDatabase database) {
        super(database);
    }


    /**
     * @param gpsLogRecord
     * @return GPSLogRecord For insert the _ID for the record will be
     * assigned after the insert
     */
    public GPSLogRecord createGPSLogRecord(GPSLogRecord gpsLogRecord) {
        Long rowId = -1l;
        ContentValues values = new ContentValues();
        // all fields mandatory fields except for _ID
        values.put(GPSLog.LOCATION_EXERCISE_ID, Long.toString(gpsLogRecord.getLocationExerciseId()));
        values.put(GPSLog.LATITUDE, Float.toString(gpsLogRecord.getLatitude()));
        values.put(GPSLog.LONGITUDE, Float.toString(gpsLogRecord.getLongitude()));
        values.put(GPSLog.ELEVATION, Integer.toString(gpsLogRecord.getElevation()));
        values.put(GPSLog.TIMESTAMP, gpsLogRecord.getTimestamp());
        values.put(GPSLog.DISTANCE_FROM_LAST_POINT, gpsLogRecord.getDistanceFromLastPoint());

        try {
            rowId = database.insert(GPSLog.GPSLOG_TABLE, null,
                    values);
            gpsLogRecord.set_id(rowId);
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
        return gpsLogRecord;

    }

    public void createGPSLogRecord(ArrayList<GPSLogRecord> gpslrArray) {
        for (int i = 0; i < gpslrArray.size(); ++i) {
            createGPSLogRecord(gpslrArray.get(i));
        }

    }

    /**
     * @param gpsLogRecord The _id must be assigned in ler prior to calling this method
     */
    public void updateGPSLog(GPSLogRecord gpsLogRecord) {
        Long rowId = gpsLogRecord.get_id();
        ContentValues values = new ContentValues();
        values.put(GPSLog.LOCATION_EXERCISE_ID, gpsLogRecord.getLocationExerciseId());
        values.put(GPSLog.LATITUDE, Float.toString(gpsLogRecord.getLatitude()));
        values.put(GPSLog.LONGITUDE, Float.toString(gpsLogRecord.getLongitude()));
        values.put(GPSLog.ELEVATION, Integer.toString(gpsLogRecord.getElevation()));
        values.put(GPSLog.TIMESTAMP, gpsLogRecord.getTimestamp());
        values.put(GPSLog.DISTANCE_FROM_LAST_POINT, gpsLogRecord.getDistanceFromLastPoint());

        try {
            database.update(GPSLog.GPSLOG_TABLE, values,
                    "where _id = ?", new String[]{rowId.toString()});
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

    public int deleteGPSLog(Long rowId) {
        int count = 0;
        try {
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
     * @param locationExerciseRowid
     */
    public int deleteByLocationExerciseRowid(long locationExerciseRowid) {
        int count = 0;
        try {
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

    public ArrayList<GPSLogRecord> getGPSLog(Long lerRowId) {
        Cursor csr;
        ArrayList<GPSLogRecord> gpslrArray = new ArrayList<GPSLogRecord>();
        csr = database.query(GPSLog.GPSLOG_TABLE,
                new String[]{GPSLog._ID, GPSLog.LATITUDE, GPSLog.LONGITUDE
                        , GPSLog.ELEVATION, GPSLog.TIMESTAMP, GPSLog.DISTANCE_FROM_LAST_POINT}
                , " location_exercise_id = ? ", new String[]{"" + lerRowId}, null, null, GPSLog.DEFAULT_SORT_ORDER);
        if (csr.getCount() == 0) {
            return gpslrArray;
        } else {
            csr.moveToFirst();
            while (!csr.isAfterLast()) {
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
