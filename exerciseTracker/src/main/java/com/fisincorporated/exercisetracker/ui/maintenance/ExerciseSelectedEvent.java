package com.fisincorporated.exercisetracker.ui.maintenance;


/**
 * Bus msg for a selected exercise
 */

public class ExerciseSelectedEvent {
    private int exerciseId;
    private int position;

    public ExerciseSelectedEvent(int exerciseId, int position){
        this.exerciseId = exerciseId;
        this.position = position;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public int getPosition() {
        return position;
    }
}
