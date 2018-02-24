package com.fisincorporated.exercisetracker.ui.history;


import android.database.Cursor;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;


public class ActivityHistorySummary {

    public enum ACTIVITY_HISTORY_ACTION {
        DISPLAY_STATS,
        DISPLAY_MAP}

    private ACTIVITY_HISTORY_ACTION action;
    private long row_id;
    private String exercise;
    private String location;
    private String activityDate;
    private String description;

    private int position;

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

    public ACTIVITY_HISTORY_ACTION getAction() {
        return action;
    }

    public ActivityHistorySummary setAction(ACTIVITY_HISTORY_ACTION action) {
        this.action = action;
        return this;
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

    public int getPosition() {
        return position;
    }

    public ActivityHistorySummary setPosition(int position) {
        this.position = position;
        return this;
    }
}
