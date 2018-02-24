package com.fisincorporated.exercisetracker.ui.history;


import java.util.HashSet;

public class DeleteHistoryListEvent {

    private final HashSet<Long> deleteSet;

    public DeleteHistoryListEvent(HashSet<Long> deleteSet){
        this.deleteSet = deleteSet;
    }
    public HashSet<Long> getDeleteSet() {
        return deleteSet;
    }

}
