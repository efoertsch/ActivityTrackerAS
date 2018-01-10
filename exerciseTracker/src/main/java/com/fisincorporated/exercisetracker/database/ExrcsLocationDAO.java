package com.fisincorporated.exercisetracker.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;

import java.util.ArrayList;

public class ExrcsLocationDAO extends BaseDAO {

	public ExrcsLocationDAO() {
		super();
	}
 
	/**
	 * 
	 * @param exrcsLocation
	 * @return exrcsLocationecord For insert the _ID for the record will be
	 *         assigned after the insert
	 */
	public ExrcsLocationRecord createExrcsLocationRecord(ExrcsLocationRecord exrcsLocation) {
		Long rowId = -1l;
		ContentValues values = new ContentValues();
		// all fields mandatory fields except for _ID
		values.put(ExrcsLocation.LOCATION, exrcsLocation.getLocation());
		values.put(ExrcsLocation.TIMES_USED,
				Integer.toString(exrcsLocation.getTimesUsed()));

		if (!dbIsOpen) {
			open();
		}
		rowId = database.insert(ExrcsLocation.LOCATION_TABLE, null, values);
		exrcsLocation.set_id(rowId);
		return exrcsLocation;
	}

	public void createExrcsLocationRecord(
			ArrayList<ExrcsLocationRecord> exrcsLocationArray) {
		for (int i = 0; i < exrcsLocationArray.size(); ++i) {
			createExrcsLocationRecord(exrcsLocationArray.get(i));
		}

	}

	/**
	 * 
	 * @param exrcsLocation
	 *           The _id must be assigned in ler prior to calling this method
	 */
	public void updateExrcsLocation(ExrcsLocationRecord exrcsLocation) {
		Long rowId = exrcsLocation.get_id();
		ContentValues values = new ContentValues();
		values.put(ExrcsLocation.LOCATION, exrcsLocation.getLocation());
		values.put(Exercise.TIMES_USED,
				Integer.toString(exrcsLocation.getTimesUsed()));

		if (!dbIsOpen) {
			open();
		}
		database.update(ExrcsLocation.LOCATION_TABLE, values, "where _id = ?",
				new String[] { rowId.toString() });
	}

	public int deleteExrcsLocation(Long rowId) {
		int count = 0;
		if (!dbIsOpen) {
			open();
		}

		database.delete(ExrcsLocation.LOCATION_TABLE, Exercise._ID + " = "
				+ rowId, null);
		return count;
	}

	public int deleteExercise(ExrcsLocationRecord exrcsLocation) {
		return deleteExrcsLocation(exrcsLocation.get_id());
	}

	public int deleteByExrcsLocationRowid(long exrcsLocationRowid) {
		int count = 0;
		if (!dbIsOpen) {
			open();
		}
		count = database.delete(ExrcsLocation.LOCATION_TABLE, ExrcsLocation._ID
				+ " = " + exrcsLocationRowid, null);
		return count;
	}

	public ArrayList<ExrcsLocationRecord> getAllLocations() {
		Cursor csr = null;
		ArrayList<ExrcsLocationRecord> exrcsLocationArray = new ArrayList<ExrcsLocationRecord>();
		try{
			if (!dbIsOpen) {
				open();
			}
		csr = database.query(ExrcsLocation.LOCATION_TABLE,
				new String[] { ExrcsLocation._ID, ExrcsLocation.LOCATION,
						ExrcsLocation.TIMES_USED }, null, null, null, null,
				Exercise.DEFAULT_SORT_ORDER);

		
		if (csr.getCount() == 0) {
			return exrcsLocationArray;
		} else {
			csr.moveToFirst();
			while (!csr.isAfterLast()) {
				exrcsLocationArray.add(new ExrcsLocationRecord(csr.getLong(csr
						.getColumnIndex(ExrcsLocation._ID)), csr.getString(csr
						.getColumnIndex(ExrcsLocation.LOCATION)), csr.getInt(csr
						.getColumnIndex(ExrcsLocation.TIMES_USED))));
				csr.moveToNext();
			}
		}
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					" ExrcsLocationDAO.getAllLocations SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					" ExrcsLocationDAO.getAllLocations exception: "
							+ e.toString());
		} finally {
			try {	if (csr != null && !csr.isClosed()) {csr.close();} } catch (SQLException sqle) {	;}
		}
		return exrcsLocationArray;
	}

	public ExrcsLocationRecord loadExrcsLocationRecordById(long id) {
		ExrcsLocationRecord elr = new ExrcsLocationRecord();
		Cursor csr = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(ExrcsLocation.LOCATION_TABLE);

			// xxxxx come up with better method of open/closing cursors.
			 if (!dbIsOpen) {
			 open();
			 }

			// run the query since it's all ready to go
			csr = queryBuilder.query(database,
					ExrcsLocation.getLocationColumnNames, "  _id = ?",
					new String[] { id + "" }, null, null, null);

			if (csr.getCount() == 0) {
				elr.set_id(-1);
			} else {
				csr.moveToFirst();
				loadExrcsLocationRecord(csr, elr);
			}

		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					" ExrcsLocationDAO.loadExrcsLocationecordById SQL exception:"
							+ sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					" ExrcsLocationDAO.loadExrcsLocationecordById exception: "
							+ e.toString());
		} finally {
			try {	if (csr != null && !csr.isClosed()) {csr.close();} } catch (SQLException sqle) {	;}
		}
		return elr;

	}

	// The only mandatory column in the cursor must be _id
	// the cursor should already be pointing to the row to read.
	public void loadExrcsLocationRecord(Cursor csr, ExrcsLocationRecord elr) {
		try {
			for (int columnIndex = 0; columnIndex < csr.getColumnCount() - 1; ++columnIndex) {
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						ExrcsLocation._ID)) {
					elr.set_id(csr.getLong(columnIndex));
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						ExrcsLocation.LOCATION)) {
					if (csr.getString(columnIndex) != null) {
						elr.setLocation(csr.getString(columnIndex));
					}
					continue;
				}
				if (csr.getColumnName(columnIndex).equalsIgnoreCase(
						Exercise.TIMES_USED)) {
					if (csr.getString(columnIndex) != null) {
						elr.setTimesUsed(csr.getInt(columnIndex));
					}
					continue;
				}
			}
		} catch (SQLException sqle) {
			Log.e(GlobalValues.LOG_TAG,
					"ExrcsLocationDAO.loadExrcsLocationRecord:" + sqle.toString());
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"ExrcsLocationDAO.loadExrcsLocationRecord:" + e.toString());
		}
	}
}
