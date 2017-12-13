package com.fisincorporated.exercisetracker;


import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import com.fisincorporated.exercisetracker.ui.drive.DriveActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;

public class TestDriveActivity {

    @Rule
    public ActivityTestRule<DriveActivity> mActivityRule = new ActivityTestRule<>(
            DriveActivity.class);

    @Test
    public void testBackupDbToDrive() {
        //Press button for load to Drive
        onView(ViewMatchers.withId(R.id.activity_drive_backup_fab)).perform(click());
    }
}
