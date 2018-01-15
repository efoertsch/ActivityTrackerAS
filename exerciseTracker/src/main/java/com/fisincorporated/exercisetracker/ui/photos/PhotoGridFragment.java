package com.fisincorporated.exercisetracker.ui.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.bumptech.glide.Glide;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;

import java.util.ArrayList;

public class PhotoGridFragment extends ExerciseMasterFragment {

    private ArrayList<PhotoDetail> photoDetails;
    private  GridView gridView;

    public static PhotoGridFragment newInstance() {
        PhotoGridFragment fragment = new PhotoGridFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment_gridview, container, false);
        getReferencedViews(view);
        lookForArguments(savedInstanceState);
        gridView.setAdapter(new PhotoGridAdapter(getContext(), Glide.with(this),  photoDetails));

        return view;
    }

    private void getReferencedViews(View view) {
        gridView = (GridView) view.findViewById(R.id.photo_fragment_gridview);
    }

    private void lookForArguments(Bundle savedInstanceState) {
        Bundle bundle = null;
        if (getArguments() != null) {
            bundle = getArguments();
        }
        if (bundle != null) {
            photoDetails = bundle.getParcelableArrayList(GlobalValues.PHOTO_POINTS);
        }

    }
}
