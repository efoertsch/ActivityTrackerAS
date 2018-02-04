package com.fisincorporated.exercisetracker.ui.photos.slideshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.photos.MediaDetail;
import com.fisincorporated.exercisetracker.ui.utils.ZoomOutPageTransformer;
import com.fisincorporated.exercisetracker.ui.video.VideoPlayerFragment;

import java.util.ArrayList;

// Based on Android FullScreenActivity example and modified as needed.
public class FullscreenPhotoPagerActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int AUTO_FAST_HIDE_DELAY = 100;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler hideHandler = new Handler();
    private View controlsView;
    private boolean visibleControls;
    private ViewPager pager;
    private ArrayList<MediaDetail> mediaDetails = new ArrayList<>();
    private int currentItem = 0;

    private final Runnable hidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable showPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            controlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable hideRunnable = this::hide;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener delayHideTouchListener = (view, motionEvent) -> {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        visibleControls = true;
        controlsView = findViewById(R.id.fullscreen_content_controls);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        if (getPhotoDetailList()) {
            // Instantiate a ViewPager and a PagerAdapter.
            pager = (ViewPager) findViewById(R.id.fullscreen_photo_pager);
            PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
            pager.setPageTransformer(true, new ZoomOutPageTransformer());
            pager.setOffscreenPageLimit(3);
            pager.setCurrentItem(currentItem);

            pager.setOnLongClickListener(v -> {
                toggle();
                return true;
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(AUTO_FAST_HIDE_DELAY);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(GlobalValues.PHOTO_DETAIL_LIST, mediaDetails);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mediaDetails = savedInstanceState.getParcelableArrayList(GlobalValues.PHOTO_DETAIL_LIST);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        delayedHide(AUTO_FAST_HIDE_DELAY);

    }

    private boolean getPhotoDetailList() {
        Intent intent = getIntent();
        mediaDetails = intent.getParcelableArrayListExtra(GlobalValues.PHOTO_DETAIL_LIST);
        if (mediaDetails == null || mediaDetails.size() == 0) {
            finish();
            return false;
        }
        currentItem = intent.getIntExtra(GlobalValues.PHOTO_DETAIL_INDEX, 0);
        return true;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            MediaDetail mediaDetail = mediaDetails.get(position);
            if (mediaDetail.isImage()) {
                return ScreenSlidePageFragment.getInstance(mediaDetail.getMediaPath());
            }
            else {
                return VideoPlayerFragment.getInstance(mediaDetail.getMediaPath());
            }
        }

        @Override
        public int getCount() {
            if (mediaDetails == null) {
                return 0;
            }
            return mediaDetails.size();
        }
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, delayMillis);
    }

    private void toggle() {
        if (visibleControls) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        controlsView.setVisibility(View.GONE);
        visibleControls = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable);
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        visibleControls = true;

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable);
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY);
    }

    public static class IntentBuilder{
        private Intent intent;

        private IntentBuilder(Context context){
            intent = new Intent(context, FullscreenPhotoPagerActivity.class);
        }

        public static IntentBuilder getBuilder(Context context) {
            return  new IntentBuilder(context);
        }

        public IntentBuilder setPhotoDetails(ArrayList<MediaDetail> mediaDetails){
            intent.putExtra(GlobalValues.PHOTO_DETAIL_LIST, mediaDetails);
            return this;
        }


        public IntentBuilder setPhotoDetailPosition(int position){
            intent.putExtra(GlobalValues.PHOTO_DETAIL_INDEX, position);
            return this;
        }

        public Intent build(){
            return intent;
        }
    }
}
