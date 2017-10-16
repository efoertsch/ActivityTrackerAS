package com.fisincorporated.exercisetracker;

import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;

/**
 * Created by ericfoertsch on 12/28/15.
 */
public class FunctionListActivityTest  extends ActivityInstrumentationTestCase2<FunctionListActivity> {

    private FunctionListActivity mFunctionListActivity;

    private MainMenuListFragment mMainMenuListFragment;

    public FunctionListActivityTest () {
        super(FunctionListActivity.class);
    }
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mFunctionListActivity = getActivity();
        mMainMenuListFragment = (MainMenuListFragment) mFunctionListActivity.getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

    }

    /**
     * Test if your test fixture has been set up correctly. You should always implement a test that
     * checks the correct setup of your test fixture. If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        assertNotNull("mFunctionListActivity is null", mFunctionListActivity);
        assertNotNull("mMainMenuListFragment is null",mMainMenuListFragment);
    }


}