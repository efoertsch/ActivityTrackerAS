package com.fisincorporated.exercisetracker.ui.media.mediagrid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.bumptech.glide.Glide;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.media.MediaDetail;

import java.util.ArrayList;

public class MediaGridFragment extends ExerciseDaggerFragment {

    private ArrayList<MediaDetail> mediaDetails;
    private  GridView gridView;

    public static MediaGridFragment newInstance() {
        MediaGridFragment fragment = new MediaGridFragment();
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
        gridView.setAdapter(new MediaGridAdapter(getContext(), Glide.with(this), mediaDetails));

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
            mediaDetails = bundle.getParcelableArrayList(GlobalValues.PHOTO_POINTS);
        }

    }
}
