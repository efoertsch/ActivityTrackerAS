package com.fisincorporated.exercisetracker.ui.media.mediagrid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerActivity;
import com.fisincorporated.exercisetracker.ui.media.MediaPoint;
import com.fisincorporated.exercisetracker.ui.utils.DepthPageTransformer;

import java.util.ArrayList;

public class MediaGridPagerActivity extends ExerciseDaggerActivity {

    private ViewPager viewPager;
    private ArrayList<MediaPoint> mediaPoints;
    private int photoPointIndex = 0;
    private String title = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPager = new ViewPager(this);
        viewPager.setId(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int state) {
            }
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                photoPointIndex = position;
            }

        });

        FrameLayout frmLayout = (FrameLayout) findViewById(R.id.fragmentContainer);
        frmLayout.addView(viewPager);
        getPhotoPointsFromIntent();

        if (mediaPoints == null) {
            finish();
            return;
        }
        setActivityTitle(R.string.photo_points);
        setPhotoGrid(photoPointIndex);
        setActivityTitle(title);
    }

    private void getPhotoPointsFromIntent() {
        Intent intent = getIntent();
        photoPointIndex = intent.getIntExtra(GlobalValues.PHOTO_POINT_INDEX, 0);
        mediaPoints = intent.getParcelableArrayListExtra(GlobalValues.PHOTO_POINTS);
        title = intent.getStringExtra(GlobalValues.TITLE);
    }

    @Override
    protected Fragment createFragment() {
        return null;
    }

    public void setPhotoGrid(int position){
        viewPager.setAdapter(new MediaGridPagerAdapter<>(
                getSupportFragmentManager(),MediaGridFragment.class,
                mediaPoints));
        viewPager.setCurrentItem(photoPointIndex);

    }

    public static class IntentBuilder{
        private Intent intent;

        private IntentBuilder(Context context){
            intent = new Intent(context, MediaGridPagerActivity.class);
        }

        public static IntentBuilder getBuilder(Context context) {
            return  new MediaGridPagerActivity.IntentBuilder(context);
        }

        public IntentBuilder setTitle(String title){
            intent.putExtra(GlobalValues.TITLE, title);
            return this;
        }

        public IntentBuilder setPhotoPoints(ArrayList<MediaPoint> mediaPoints){
            intent.putExtra(GlobalValues.PHOTO_POINTS, mediaPoints);
            return this;
        }

        public IntentBuilder setPhotoPointPosition(int position){
            intent.putExtra(GlobalValues.PHOTO_POINT_INDEX, position);
            return this;
        }

        public Intent build(){
            return intent;
        }
    }


}
