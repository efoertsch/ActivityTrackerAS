package com.fisincorporated.exercisetracker.ui.startup;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;

import java.io.File;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PhotoFragment extends Fragment {

    View view;

    ImageView imageView;

    String photoPath;

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.startup_photo, container, false);
        imageView = (ImageView) view.findViewById(R.id.startup_photo_view);
        photoPath = PhotoUtils.getStartupPhotoPath(getActivity());
        loadUserPhoto();
        return view;
    }

    @Override
    public void onResume() {
        String currentPhotoPath;
        super.onResume();
        currentPhotoPath = PhotoUtils.getStartupPhotoPath(getActivity());
        if (currentPhotoPath != null  &&
                (photoPath == null ||  (photoPath != null && !currentPhotoPath.equals(photoPath)))) {
            photoPath = currentPhotoPath;
            loadUserPhoto();
        }
       // getPhotoOrDefaultImage();
    }

    private void loadUserPhoto() {
        if (!PhotoUtils.loadPhotoToImageView(imageView, photoPath)) {
            imageView.setImageResource(R.drawable.zion_canyon);
        }
    }

    private void getPhotoOrDefaultImage() {
        ImageView imageView = (ImageView) view.findViewById(R.id.startup_photo_view);
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(GlobalValues.IMAGE_DIR, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, GlobalValues.IMAGE_FILE_NAME);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.zion_canyon)
                .fallback(R.drawable.zion_canyon);
        Glide.with(this)
                .load(mypath)
                .apply(options)
                .transition(withCrossFade())
                .into(imageView);
    }

}