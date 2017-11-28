package com.fisincorporated.exercisetracker.database;

import android.provider.BaseColumns;

public final class TrackerDatabase {
    private TrackerDatabase() {
    }

    // Exercise (Walk, Hiking, etc.)
    public static final class Exercise implements BaseColumns {
        private Exercise() {
        }

        public static final String EXERCISE_TABLE = "exercise";
        public static final String EXERCISE = "exercise";
        public static final String DEFAULT_LOG_INTERVAL = "default_log_interval";
        public static final String LOG_INTERVAL = "log_interval";
        public static final String LOG_DETAIL = "log_detail";
        public static final String TIMES_USED = "times_used";
        public static final String ELEVATION_IN_DIST_CALCS = "elevation_in_dist_calcs";
        public static final String MIN_DISTANCE_TO_LOG = "min_distance_to_log";
        public static final String PIN_EVERY_X_MILES = "pin_every_x_miles";
        public static final String DEFAULT_SORT_ORDER = "exercise COLLATE NOCASE ASC";

        public static final String[] exerciseColumnNames = {
                _ID, EXERCISE, DEFAULT_LOG_INTERVAL,
                LOG_INTERVAL, LOG_DETAIL, TIMES_USED
                , ELEVATION_IN_DIST_CALCS, MIN_DISTANCE_TO_LOG, PIN_EVERY_X_MILES};
    }

    // Create the Type of Activity table (Alpine Skiing, Biking, ...
    public final static String getCreateExerciseTableSQL() {
        // Note BaseColumns defines _ID
        return "CREATE TABLE IF NOT EXISTS  " + Exercise.EXERCISE_TABLE
                + "  (" + Exercise._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Exercise.EXERCISE + " TEXT , "
                + Exercise.DEFAULT_LOG_INTERVAL + " INTEGER DEFAULT 60 ,"
                + Exercise.LOG_INTERVAL + " INTEGER DEFAULT 60,"
                + Exercise.LOG_DETAIL + " INTEGER DEFAULT 0,"
                + Exercise.TIMES_USED + " INTEGER DEFAULT 0,"
                + Exercise.ELEVATION_IN_DIST_CALCS + " INTEGER DEFAULT 0,"
                + Exercise.MIN_DISTANCE_TO_LOG + " INTEGER DEFAULT 0,"
                + Exercise.PIN_EVERY_X_MILES + " INTEGER DEFAULT -1"
                + ");";
    }

    public final static String doExercisesExistSQL() {
        return "Select _id from " + Exercise.EXERCISE_TABLE + "LIMIT 1";
    }

    // General location such as White Mountains, Loon Mountain
    // Problem - ExrcsLocation same name as Android ExrcsLocation.
    // Created getLocation to return class to make/use under 'alias'
    public static final class ExrcsLocation implements BaseColumns {
        private ExrcsLocation() {
        }

        public static final String LOCATION_TABLE = "location";
        public static final String LOCATION = "location";
        public static final String TIMES_USED = "times_used";
        public static final String DEFAULT_SORT_ORDER = "location COLLATE NOCASE  ASC";

        public static final String[] getLocationColumnNames = {
                _ID, LOCATION, TIMES_USED};
    }

    public final static String getCreateLocationTableSQL() {
        // Note BaseColumns defines _ID
        return "CREATE TABLE IF NOT EXISTS  " + ExrcsLocation.LOCATION_TABLE + " ("
                + ExrcsLocation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + ExrcsLocation.LOCATION + " TEXT  ,"
                + ExrcsLocation.TIMES_USED + " INTEGER DEFAULT 0"
                + ");";
    }

    public static final class LocationExercise implements BaseColumns {
        private LocationExercise() {
        }

        public static final String LOCATION_EXERCISE_TABLE = "location_exercise";
        public static final String LOCATION_ID = "location_id";
        public static final String EXERCISE_ID = "exercise_id";
        public static final String DESCRIPTION = "description";
        public static final String START_TIMESTAMP = "start_timestamp";
        public static final String END_TIMESTAMP = "end_timestamp";
        public static final String DISTANCE = "distance";
        public static final String AVERAGE_SPEED = "average_speed";
        public static final String START_ALTITUDE = "start_altitude";
        public static final String END_ALTITUDE = "end_altitude";
        public static final String ALTITUDE_GAINED = "altitude_gained";
        public static final String ALTITUDE_LOST = "altitude_lost";
        public static final String START_LATITUDE = "start_latitude";
        public static final String START_LONGITUDE = "start_longitude";
        public static final String END_LATITUDE = "end_latitude";
        public static final String END_LONGITUDE = "end_longitude";
        public static final String LOG_INTERVAL = "log_interval";
        public static final String LOG_DETAIL = "log_detail";
        public static final String MAX_SPEED_TO_POINT = "max_speed_to_point";

        public static final String DEFAULT_SORT_ORDER = START_TIMESTAMP + " ASC";

        public static final String[] getLocationExerciseColumnNames = {
                _ID, LOCATION_ID, EXERCISE_ID,
                DESCRIPTION, START_TIMESTAMP,
                END_TIMESTAMP, DISTANCE, AVERAGE_SPEED, START_ALTITUDE, END_ALTITUDE,
                ALTITUDE_GAINED, ALTITUDE_LOST, START_LATITUDE, START_LONGITUDE,
                END_LATITUDE, END_LONGITUDE, LOG_INTERVAL, LOG_DETAIL,
                MAX_SPEED_TO_POINT
        };

    }

    // Create the Type of Activity table (Alpine Skiing, Biking, ...
    public final static String getCreateExerciseLocationTableSQL() {
        return "CREATE TABLE IF NOT EXISTS  " + LocationExercise.LOCATION_EXERCISE_TABLE
                + "  (" + LocationExercise._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + LocationExercise.LOCATION_ID
                + " INTEGER NOT NULL REFERENCES " + ExrcsLocation.LOCATION_TABLE
                + "(" + ExrcsLocation._ID + ") ," + LocationExercise.EXERCISE_ID
                + " INTEGER NOT NULL REFERENCES "
                + Exercise.EXERCISE_TABLE + "(" + Exercise._ID + "), "
                + LocationExercise.DESCRIPTION + " TEXT, "
                + LocationExercise.START_TIMESTAMP + " TIMESTAMP, "
                + LocationExercise.END_TIMESTAMP + " TIMESTAMP ,"
                + LocationExercise.DISTANCE + " INTEGER ,"
                + LocationExercise.AVERAGE_SPEED + " NUMBER  ,"
                + LocationExercise.START_ALTITUDE + " NUMBER  ,"
                + LocationExercise.END_ALTITUDE + " NUMBER ,"
                + LocationExercise.ALTITUDE_GAINED + " NUMBER  ,"
                + LocationExercise.ALTITUDE_LOST + " NUMBER ,"
                + LocationExercise.START_LATITUDE + " NUMBER ,"
                + LocationExercise.START_LONGITUDE + " NUMBER ,"
                + LocationExercise.END_LATITUDE + " NUMBER ,"
                + LocationExercise.END_LONGITUDE + " NUMBER , "
                + LocationExercise.LOG_INTERVAL + " NUMBER, "
                + LocationExercise.LOG_DETAIL + " INTEGER NOT NULL DEFAULT 0, "
                + LocationExercise.MAX_SPEED_TO_POINT + " NUMBER NOT NULL DEFAULT 0"
                + ");";

    }

    // GPS_Do_not_use points saved for the Activity and date
    public static final class GPSLog implements BaseColumns {
        private GPSLog() {
        }

        public static final String GPSLOG_TABLE = "gpslog";
        // foreign key to the activity table
        public static final String LOCATION_EXERCISE_ID = "location_exercise_id";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ELEVATION = "elevation";
        public static final String TIMESTAMP = "timestamp";
        public static final String DISTANCE_FROM_LAST_POINT = "distance_from_last_point";
        public static final String DEFAULT_SORT_ORDER = "location_exercise_id ASC, TIMESTAMP ASC";
    }

    public final static String getCreateGPSLogTableSQL() {
        return "CREATE TABLE IF NOT EXISTS  "
                + GPSLog.GPSLOG_TABLE
                + " ("
                + GPSLog._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                // this is a foreign key to the ExrcsLocation Activity table
                + GPSLog.LOCATION_EXERCISE_ID + " INTEGER NOT NULL REFERENCES "
                + LocationExercise.LOCATION_EXERCISE_TABLE + "("
                + LocationExercise._ID + ") ,"
                + GPSLog.LATITUDE + " NUMBER,"
                + GPSLog.LONGITUDE + " NUMBER,"
                + GPSLog.ELEVATION + " NUMBER,"
                + GPSLog.TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + GPSLog.DISTANCE_FROM_LAST_POINT + " NUMBER  DEFAULT 0" + ");";
    }

    public final static String getCreateGPSLogIndex1() {
        return "CREATE INDEX IF NOT EXISTS GPSLOG_IX1 ON "
                + GPSLog.GPSLOG_TABLE + " ( " + GPSLog.LOCATION_EXERCISE_ID
                + " ASC ," + GPSLog._ID + " ASC  )  ;";
    }

}
