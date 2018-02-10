package com.fisincorporated.exercisetracker.ui.history;


//TODO convert to RxJava
public interface IHistoryListCallbacks {

    void displayStats(ActivityHistorySummary activityHistorySummary, int position);

    void displayMap(ActivityHistorySummary activityHistorySummary);

    void deleteThisActivity(ActivityHistorySummary activityHistorySummary);

    void removeFromDeleteList(ActivityHistorySummary activityHistorySummary);

    boolean isSetToDelete(ActivityHistorySummary activityHistorySummary);
}
