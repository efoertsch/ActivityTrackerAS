package com.fisincorporated.ExerciseTracker;


import java.util.SortedMap;
import java.util.TreeMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// ListActivity is wrapper class for activities that feature a ListView bond to a data source as primary UI
// and expose event handlers for list item selection
//public abstract class MainMenuListFragment extends ListActivity {
public abstract class MainMenuListFragment extends ListFragment {
	private SortedMap<String, Object> actions = new TreeMap<String, Object>();

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String key = (String) l.getItemAtPosition(position);
		 startActivityForResult((Intent) actions.get(key), 0);
		 
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prepareMenu();

		String[] keys = actions.keySet().toArray(
				new String[actions.keySet().size()]);

		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, keys));
	}
	
	public void addMenuItem(String label, Class<?> cls){
		actions.put(label, new Intent(getActivity(), cls	));
	}
	
	abstract void prepareMenu();
}
