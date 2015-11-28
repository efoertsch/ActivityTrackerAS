package com.fisincorporated.ExerciseTracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
//import com.google.android.gms.maps.SupportMapFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//public class MapFragment extends SupportMapFragment {
public class MapFragment extends Fragment {

	 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//View view = super.onCreateView(inflater, container, savedInstanceState);
		// the following line will cause Duplicate id 0x...., tag null, or parent id 0x0 with another fragment
		// when called a second time for a new map
		View view = inflater.inflate(R.layout.map_fragment, container, false);
		// Can't retain fragments nested in other fragments
		//setRetainInstance(true);
		return view;
	}
	
	 

}
