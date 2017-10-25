package com.fisincorporated.exercisetracker.ui.history;


import android.database.Cursor;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;


public class ActivityHistorySummary {

    private long row_id;
    private String exercise;
    private String location;
    private String activityDate;
    private String description;

    private ActivityHistorySummary(){}

    public static ActivityHistorySummary getFromCursor(Cursor cursor) {

        ActivityHistorySummary activityHistorySummary = new ActivityHistorySummary();
        activityHistorySummary.row_id = cursor.getLong(cursor.getColumnIndex(TrackerDatabase.LocationExercise._ID));
        activityHistorySummary.exercise = cursor.getString(cursor.getColumnIndex(Exercise.EXERCISE));
        activityHistorySummary.location = cursor.getString(cursor.getColumnIndex(ExrcsLocation.LOCATION));
        activityHistorySummary.activityDate = cursor.getString(cursor.getColumnIndex(GlobalValues.START_DATE));
        activityHistorySummary.description = cursor.getString(cursor.getColumnIndex(LocationExercise.DESCRIPTION));

        return activityHistorySummary;
    }

    public long getRow_id() {
        return row_id;
    }

    public String getExercise() {
        return exercise;
    }

    public String getLocation() {
        return location;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public String getDescription() {
        return description;
    }

    public String createActivityTitle(){
        return exercise + "@" + location + " " + activityDate;
    }
}
