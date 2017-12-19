package com.fisincorporated.exercisetracker.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;

import java.util.ArrayList;

public class ExerciseDAO extends BaseDAO {

    private Cursor csr;

    public ExerciseDAO() {
        super();
    }

    /**
     * @param exerciser
     * @return ExerciseRecord For insert the _ID for the record will be assigned
     * after the insert
     */
    public ExerciseRecord createExerciseRecord(ExerciseRecord exerciser) {
        Long rowId = -1l;
        ContentValues values = new ContentValues();
        // all fields mandatory fields except for _ID
        values.put(Exercise.EXERCISE, exerciser.getExercise());
        values.put(Exercise.LOG_INTERVAL, Integer.toString(exerciser.getLogInterval()));
        values.put(Exercise.DEFAULT_LOG_INTERVAL, Integer.toString(exerciser.getDefaultLogInterval()));
        values.put(Exercise.LOG_DETAIL, Integer.toString(exerciser.getLogDetail()));
        values.put(Exercise.ELEVATION_IN_DIST_CALCS, Integer.toString(exerciser.getElevationInDistCalcs()));
        values.put(Exercise.MIN_DISTANCE_TO_LOG, Float.toString(exerciser.getMinDistanceToLog()));
        values.put(Exercise.PIN_EVERY_X_MILES, Integer.toString((exerciser.getPinEveryXMiles())));

        if (!dbIsOpen) {
            open();
        }
        rowId = database.insert(Exercise.EXERCISE_TABLE, null, values);
        exerciser.set_id(rowId);
        return exerciser;
    }

    public void createExerciseRecord(ArrayList<ExerciseRecord> exerciserArray) {
        for (int i = 0; i < exerciserArray.size(); ++i) {
            createExerciseRecord(exerciserArray.get(i));
        }

    }

    /**
     * @param exerciser The _id must be assigned in ler prior to calling this method
     */
    public void updateExercise(ExerciseRecord exerciser) {
        Long rowId = exerciser.get_id();
        ContentValues values = new ContentValues();
        values.put(Exercise._ID, exerciser.get_id());
        values.put(Exercise.EXERCISE, exerciser.getExercise());
        values.put(Exercise.LOG_INTERVAL,
                Integer.toString(exerciser.getLogInterval()));
        values.put(Exercise.DEFAULT_LOG_INTERVAL,
                Integer.toString(exerciser.getDefaultLogInterval()));
        values.put(Exercise.LOG_DETAIL,
                Integer.toString(exerciser.getLogDetail()));
        values.put(Exercise.TIMES_USED,
                Integer.toString(exerciser.getTimesUsed()));
        values.put(Exercise.ELEVATION_IN_DIST_CALCS, Integer.toString(exerciser.getElevationInDistCalcs()));
        values.put(Exercise.MIN_DISTANCE_TO_LOG, Float.toString(exerciser.getMinDistanceToLog()));
        values.put(Exercise.PIN_EVERY_X_MILES, Integer.toString((exerciser.getPinEveryXMiles())));

        if (!dbIsOpen) {
            open();
        }
        database.update(Exercise.EXERCISE_TABLE, values, " _id = ?",
                new String[]{rowId.toString()});
    }

    public int deleteExercise(Long rowId) {
        int count = 0;
        if (!dbIsOpen) {
            open();
        }

        database.delete(Exercise.EXERCISE_TABLE, Exercise._ID + " = " + rowId,
                null);
        return count;
    }

    public int deleteExercise(ExerciseRecord exerciser) {
        return deleteExercise(exerciser.get_id());
    }

    /**
     * @param exerciseRowid
     */

    public int deleteByExerciseRowid(long exerciseRowid) {
        int count = 0;
        if (!dbIsOpen) {
            open();
        }
        count = database.delete(Exercise.EXERCISE_TABLE, Exercise._ID + " = "
                + exerciseRowid, null);
        return count;
    }

    public ArrayList<ExerciseRecord> getAllExercises() {
        ArrayList<ExerciseRecord> exerciseArray = new ArrayList<ExerciseRecord>();
        csr = database.query(Exercise.EXERCISE_TABLE,
                new String[]{Exercise._ID, Exercise.EXERCISE, Exercise.LOG_INTERVAL, Exercise.DEFAULT_LOG_INTERVAL, Exercise.LOG_DETAIL
                        , Exercise.TIMES_USED, Exercise.MIN_DISTANCE_TO_LOG, Exercise.ELEVATION_IN_DIST_CALCS, Exercise.PIN_EVERY_X_MILES}
                , null, null, null, null, Exercise.DEFAULT_SORT_ORDER);

        if (!dbIsOpen) {
            open();
        }

        if (csr.getCount() == 0) {
            return exerciseArray;
        } else {
            csr.moveToFirst();
            while (!csr.isAfterLast()) {
                exerciseArray.add(new ExerciseRecord(
                        csr.getLong(csr.getColumnIndex(Exercise._ID)),
                        csr.getString(csr.getColumnIndex(Exercise.EXERCISE)),
                        csr.getInt(csr.getColumnIndex(Exercise.LOG_INTERVAL)),
                        csr.getInt(csr.getColumnIndex(Exercise.DEFAULT_LOG_INTERVAL)),
                        csr.getInt(csr.getColumnIndex(Exercise.LOG_DETAIL)),
                        csr.getInt(csr.getColumnIndex(Exercise.TIMES_USED)),
                        csr.getFloat(csr.getColumnIndex(Exercise.MIN_DISTANCE_TO_LOG)),
                        csr.getInt(csr.getColumnIndex(Exercise.LOG_DETAIL)),
                        csr.getInt(csr.getColumnIndex(Exercise.PIN_EVERY_X_MILES))));
                csr.moveToNext();
            }

        }

        csr.close();

        return exerciseArray;
    }

    public ExerciseRecord loadExerciseRecordById(double id) {
        ExerciseRecord ler = new ExerciseRecord();
        Cursor csr = null;
        try {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(Exercise.EXERCISE_TABLE);

            // xxxxx come up with better method of open/closing cursors.
            if (!dbIsOpen) {
                open();
            }

            // run the query since it's all ready to go
            csr = queryBuilder.query(database,
                    Exercise.exerciseColumnNames,
                    "  _id = ?", new String[]{id + ""}, null, null, null);

            if (csr.getCount() == 0) {
                ler.set_id(-1);
            } else {
                csr.moveToFirst();
                loadExerciseRecord(csr, ler);
            }

        } catch (SQLException sqle) {
            Log.e(GlobalValues.LOG_TAG,
                    " ExerciseDAO.load ExerciseRecordById SQL exception:"
                            + sqle.toString());
        } catch (Exception e) {
            Log.e(GlobalValues.LOG_TAG,
                    " ExerciseDAO.load ExerciseRecordById exception: "
                            + e.toString());
        } finally {
            try {
                if (csr != null && !csr.isClosed()) {
                    csr.close();
                }
            } catch (SQLException sqle) {
                ;
            }
        }
        return ler;

    }

    // The only mandatory column in the cursor must be _id
    // the cursor should already be pointing to the row to read.
    public static void loadExerciseRecord(Cursor csr, ExerciseRecord er) {
        try {
            for (int columnIndex = 0; columnIndex <= csr.getColumnCount() - 1; ++columnIndex) {
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise._ID)) {
                    er.set_id(csr.getLong(columnIndex));
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.EXERCISE)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setExercise(csr.getString(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.DEFAULT_LOG_INTERVAL)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setDefaultLogInterval(csr.getInt(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.LOG_INTERVAL)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setLogInterval(csr.getInt(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.LOG_DETAIL)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setLogDetail(csr.getInt(columnIndex));
                    }
                    continue;

                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.TIMES_USED)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setTimesUsed(csr.getInt(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.MIN_DISTANCE_TO_LOG)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setMinDistanceToLog(csr.getFloat(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.ELEVATION_IN_DIST_CALCS)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setElevationInDistCalcs(csr.getInt(columnIndex));
                    }
                    continue;
                }
                if (csr.getColumnName(columnIndex).equalsIgnoreCase(
                        Exercise.PIN_EVERY_X_MILES)) {
                    if (csr.getString(columnIndex) != null) {
                        er.setPinEveryXMiles(csr.getInt(columnIndex));
                    }
                    continue;
                }
            }
        } catch (SQLException sqle) {
            Log.e(GlobalValues.LOG_TAG,
                    "ExerciseDAO.loadExerciseRecord:"
                            + sqle.toString());
        } catch (Exception e) {
            Log.e(GlobalValues.LOG_TAG,
                    "ExerciseDAO.loadExerciseRecord:" + e.toString());
        }
    }

    public void updateTimesUsed(double id, int value) {
        if (!dbIsOpen) {
            open();
        }
        database.execSQL("update " + Exercise.EXERCISE_TABLE
                + " set " + Exercise.TIMES_USED + " = " + Exercise.TIMES_USED + " + " + value
                + " where " + Exercise._ID + " = " + id);
    }

}
