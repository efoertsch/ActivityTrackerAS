package com.fisincorporated.ExerciseTracker;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public abstract class MainMenuListFragment extends ListFragment {
    private ArrayList<String> options = new ArrayList<>();
    private ArrayList<Intent> optionsActivity = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_menu, container, false);

        ListView lvmenu = (ListView) view.findViewById(android.R.id.list);
        lvmenu.setAdapter(new MenuAdapter(getActivity(), options));
        return view;
    }


    void prepareMenu() {
        addMenuItem(getResources().getString(R.string.main_menu_start_activity), StartExercise.class);
        addMenuItem(getResources().getString(R.string.main_menu_activity_history), ActivityList.class);
        addMenuItem(getResources().getString(R.string.main_menu_exercise_setup), ExerciseList.class);
        addMenuItem(getResources().getString(R.string.main_menu_program_options), ProgramOptions.class);
        addMenuItem(getResources().getString(R.string.main_menu_about_activity_tracker), AboutActivityTracker.class);

    }

    public void addMenuItem(String label, Class<?> cls) {
        options.add(label);
        optionsActivity.add(new Intent(getActivity(), cls));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(optionsActivity.get(position), 0);
    }

    public class MenuAdapter extends ArrayAdapter<String> {
        public MenuAdapter(Context context, ArrayList<String> options) {
            super(context, 0, options);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_menu_row, parent, false);
            }
            // Lookup view for data population
            TextView tvMenuOption = (TextView) convertView.findViewById(R.id.tvMenuOption);
            tvMenuOption.setText(options.get(position));
            // Return the completed view to render on screen
            return convertView;
        }
    }

}
