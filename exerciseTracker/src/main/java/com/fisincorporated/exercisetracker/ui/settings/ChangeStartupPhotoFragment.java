package com.fisincorporated.exercisetracker.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

public class ChangeStartupPhotoFragment extends ExerciseDaggerFragment {

    private ImageView imageView;
    private ProgressBar progressBar;
    private String photoPath;

    @Inject
    public PhotoUtils photoUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_startup_photo, container, false);
        getReferencedViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForStartupPhoto();
    }

    private void getReferencedViews(View view) {
        imageView = (ImageView) view.findViewById(R.id.change_startup_photo_imageView);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GlobalValues.PICK_PHOTO);
        });
        progressBar = (ProgressBar) view.findViewById(R.id.change_startup_photo_progressBar);
        Button button = (Button) view.findViewById(R.id.change_startup_photo_remove_button);
        button.setOnClickListener(v -> {
            photoUtils.removeStorePhotoPreference();
            checkForStartupPhoto();
        });
    }

    private void checkForStartupPhoto() {
        String currentPhotoPath = photoUtils.getStartupPhotoPath();
        if (currentPhotoPath == null) {
            setPhotoDefaultImage();
        } else if (photoPath == null || !currentPhotoPath.equals(photoPath)) {
            photoPath = currentPhotoPath;
            loadUserPhoto(photoPath);
        }
    }
    private void loadUserPhoto(String photoPath) {
        if (!photoUtils.loadPhotoToImageView(imageView, photoPath, progressBar, null)) {
            setPhotoDefaultImage();
        }
    }

    private void setPhotoDefaultImage() {
        imageView.setImageResource(R.drawable.ic_photo_black_image);
        photoUtils.removeStorePhotoPreference();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case (GlobalValues.PICK_PHOTO): {
                if (resultCode == RESULT_OK
                        && intent != null
                        && intent.getData() != null) {
                    photoUtils.saveToInternalStorage(getActivity(), intent.getData());
                    checkForStartupPhoto();
                }
            }
        }
    }


}

