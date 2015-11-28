package com.fisincorporated.ExerciseTracker;

import  com.fisincorporated.ExerciseTracker.R;
 
public class Functions extends MainMenuListFragment {

    @Override
    void prepareMenu() {
   	 //addMenuItem("1. Start Activity", StartExercise.class);
   	 //addMenuItem("2. List Prior Activities", PriorActivitiesListDoNotUse.class);
       //addMenuItem("3. Exercise Setup", ExerciseList.class);
       //addMenuItem("4. Program Options", ProgramOptions.class);
       addMenuItem(getResources().getString(R.string.main_menu_start_activity), StartExercise.class);
   	 addMenuItem(getResources().getString(R.string.main_menu_list_prior_activities), ActivityList.class);
       addMenuItem(getResources().getString(R.string.main_menu_exercise_setup), ExerciseList.class);
       addMenuItem(getResources().getString(R.string.main_menu_program_options), ProgramOptions.class);
       addMenuItem(getResources().getString(R.string.main_menu_about_activity_tracker),AboutActivityTracker.class);
       
       
        
        
    }

}