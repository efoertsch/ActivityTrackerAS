package com.fisincorporated.ExerciseTracker;

public class Functions extends MainMenuListFragment {

    @Override
    void prepareMenu() {
        addMenuItem(getResources().getString(R.string.main_menu_start_activity), StartExercise.class);
        addMenuItem(getResources().getString(R.string.main_menu_activity_history), ActivityList.class);
        addMenuItem(getResources().getString(R.string.main_menu_exercise_setup), ExerciseList.class);
        addMenuItem(getResources().getString(R.string.main_menu_program_options), ProgramOptions.class);
        addMenuItem(getResources().getString(R.string.main_menu_about_activity_tracker), AboutActivityTracker.class);

    }

}