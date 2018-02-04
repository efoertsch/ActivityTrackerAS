package com.fisincorporated.exercisetracker.ui.photos.photogrid;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.photos.MediaDetail;
import com.fisincorporated.exercisetracker.ui.photos.slideshow.FullscreenPhotoPagerActivity;

import java.io.File;
import java.util.ArrayList;



public class PhotoGridAdapter extends ArrayAdapter {

    private Context context;
    private final RequestManager glide;
    private LayoutInflater inflater;

    private ArrayList<MediaDetail> mediaDetails;

    public PhotoGridAdapter(Context context, RequestManager glide, ArrayList<MediaDetail> mediaDetails) {
        super(context, R.layout.photo_fragment_gridview_imageview, mediaDetails);
        this.context = context;
        this.glide = glide;
        this.mediaDetails = mediaDetails;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return mediaDetails.size();
    }

    public MediaDetail getItem(int position) {
        return mediaDetails.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.photo_fragment_gridview_imageview, parent, false);
        }
        ImageView glideImageView =  (ImageView) convertView.findViewById(R.id.photo_fragment_gridview_glide_imageview);
        ImageView VideoIconImageView = (ImageView) convertView.findViewById(R.id.photo_fragment_gridview_video_icon);

        if (mediaDetails.get(position).isVideo()) {
            glide.load(Uri.fromFile(new File(mediaDetails.get(position).getMediaPath())))
                    .into(glideImageView);
            VideoIconImageView.setVisibility(View.VISIBLE);
        } else {
            // is photo
            glide.load(Uri.fromFile(new File(mediaDetails.get(position).getMediaPath())))
                    .into(glideImageView);
            VideoIconImageView.setVisibility(View.GONE);

        }

        convertView.setTag(position);
        convertView.setOnClickListener(v -> {
            FullscreenPhotoPagerActivity.IntentBuilder intentBuilder = FullscreenPhotoPagerActivity.IntentBuilder.getBuilder(getContext());
            intentBuilder.setPhotoDetails(mediaDetails).setPhotoDetailPosition((int) v.getTag());
            context.startActivity(intentBuilder.build());
        });

        return convertView;
    }
}
