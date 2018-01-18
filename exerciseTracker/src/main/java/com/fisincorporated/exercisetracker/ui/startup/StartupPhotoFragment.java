package com.fisincorporated.exercisetracker.ui.startup;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.startactivity.StartExercise;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;

public class StartupPhotoFragment extends Fragment {

    private static final String TAG = StartupPhotoFragment.class.getSimpleName();

    private View view;
    private ImageView imageView;
    private String photoPath;
    private FloatingActionButton startFab;
    private TextView noPhotoTextView;
    private ProgressBar progressBar;
    private ValueAnimator valueAnimator;
    private int animationTime = 10 * 1000;
    private float animatedFloat;
    private int animatedInt;
    private float animatedDecimal;
    private int fabAlpha;

    private int[] ids = {R.drawable.ic_action_directions_walk,
            R.drawable.ic_action_directions_run,
            R.drawable.ic_action_directions_bike,
            R.drawable.ic_action_hiking,
            R.drawable.ic_action_skiing};

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.startup_photo, container, false);
        getReferencedViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForStartupPhoto();
        setupActivityFabAnimator(startFab);
        valueAnimator.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        valueAnimator.end();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void getReferencedViews(View view) {
        imageView = (ImageView) view.findViewById(R.id.startup_photo_view);
        noPhotoTextView = (TextView) view.findViewById(R.id.startup_photo_no_photo_text);
        progressBar = (ProgressBar) view.findViewById(R.id.change_startup_photo_progressBar);
        startFab = (FloatingActionButton) view.findViewById(R.id.startup_photo_fab);
        startFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StartExercise.class);
            startActivity(intent);
        });
        checkForStartupPhoto();
    }

    private void checkForStartupPhoto() {
        String currentPhotoPath = PhotoUtils.getStartupPhotoPath();
        if (currentPhotoPath == null) {
            noPhotoTextView.setVisibility(View.VISIBLE);
        } else {
            if (photoPath == null || !currentPhotoPath.equals(photoPath)) {
                photoPath = currentPhotoPath;
                loadUserPhoto(photoPath);
            }
        }
    }

    private void loadUserPhoto(String photoPath) {
        if (!PhotoUtils.loadPhotoToImageView(imageView, photoPath, progressBar, noPhotoTextView)) {
            noPhotoTextView.setVisibility(View.VISIBLE);

        }
    }

    private void setupActivityFabAnimator(final FloatingActionButton fab) {
        // To display all icons and fad in/out go from 0 - ids.length. But be careful not to handle possible index exception
        valueAnimator = ValueAnimator.ofFloat(0f, (float) ids.length ).setDuration(animationTime);
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int i = -1;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedFloat = (Float) animation.getAnimatedValue();
                if (animatedFloat >= ids.length) {
                    animatedFloat = ids.length - 0.001f;
                }
                animatedInt = (int) animatedFloat;
                animatedDecimal = animatedFloat - animatedInt;

                if (animatedDecimal <= 0.125) {
                    // fab in
                    fabAlpha = (int) (255f * animatedDecimal/0.125f);
                } else if (animatedDecimal >= 0.875) {
                    // fab out
                    fabAlpha =  255 - (int) (255f * (animatedDecimal - 0.875f)/0.125f);
                } else {
                    // full display
                    fabAlpha = 255;
                }
//                Log.d(TAG, "animatedFloat:" + animatedFloat +  " animatedInt:" + animatedInt
//                        +  " animatedDecimal:" + animatedDecimal
//                        + " Alpha:" +  fabAlpha);

                if (i != animatedInt && i < ids.length) {
                    fab.setImageDrawable(getResources().getDrawable(ids[animatedInt]));
                    i = animatedInt;
                }

                fab.setImageAlpha(fabAlpha);
            }
        });
    }

}