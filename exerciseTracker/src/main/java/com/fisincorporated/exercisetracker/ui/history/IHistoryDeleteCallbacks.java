package com.fisincorporated.exercisetracker.ui.history;

public interface IHistoryDeleteCallbacks extends IHistoryCallbacks {

    void deleteThisActivity(ActivityHistorySummary activityHistorySummary);

    boolean isSetToDelete(ActivityHistorySummary activityHistorySummary);
}
