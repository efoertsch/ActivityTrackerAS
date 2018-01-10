package com.fisincorporated.exercisetracker.ui.photos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

import java.io.File;
import java.util.ArrayList;



public class PhotoGridAdapter extends ArrayAdapter {

    private Context context;
    private LayoutInflater inflater;

    private ArrayList<PhotoDetail> photoDetails;

    public PhotoGridAdapter(Context context, ArrayList<PhotoDetail> photoDetails) {
        super(context, R.layout.photo_fragment_gridview_imageview, photoDetails);
        this.context = context;
        this.photoDetails = photoDetails;

        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return photoDetails.size();
    }

    public PhotoDetail getItem(int position) {
        return photoDetails.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.photo_fragment_gridview_imageview, parent, false);
        }
        Glide.with(context)
                .load(Uri.fromFile(new File(photoDetails.get(position).getPhotoPath())))
                .into((ImageView) convertView);
        convertView.setTag(position);
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FullscreenPhotoPagerActivity.class);
            intent.putExtra(GlobalValues.PHOTO_DETAIL_LIST, photoDetails);
            intent.putExtra(GlobalValues.PHOTO_POINT_INDEX, (int) v.getTag());
            context.startActivity(intent);
        });

        return convertView;
    }
}
