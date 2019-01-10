package com.fisincorporated.exercisetracker.ui.startup;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.fisincorporated.exercisetracker.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExerciseDrawerActivityTest {

    @Rule
    public ActivityTestRule<ExerciseDrawerActivity> mActivityTestRule = new ActivityTestRule<>(ExerciseDrawerActivity.class);

    @Test
    public void exerciseDrawerActivityTest() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button2), withText("Select later"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.startup_photo_fab), withContentDescription("Start Activity"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_airport_weather_layout),
                                        childAtPosition(
                                                withId(R.id.app_frame_layout),
                                                0)),
                                3),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatAutoCompleteTextView = onView(
                allOf(withId(R.id.start_exercise_actvLocation),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1)));
        appCompatAutoCompleteTextView.perform(scrollTo(), replaceText("Acont"), closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView2 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Acont"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1)));
        appCompatAutoCompleteTextView2.perform(scrollTo(), click());

        ViewInteraction appCompatAutoCompleteTextView3 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Acont"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1)));
        appCompatAutoCompleteTextView3.perform(scrollTo(), replaceText("Act"));

        ViewInteraction appCompatAutoCompleteTextView4 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Act"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatAutoCompleteTextView4.perform(closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView5 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Act"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1)));
        appCompatAutoCompleteTextView5.perform(scrollTo(), click());

        ViewInteraction appCompatAutoCompleteTextView6 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Act"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1)));
        appCompatAutoCompleteTextView6.perform(scrollTo(), replaceText("Acton"));

        ViewInteraction appCompatAutoCompleteTextView7 = onView(
                allOf(withId(R.id.start_exercise_actvLocation), withText("Acton"),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl2),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatAutoCompleteTextView7.perform(closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.start_exercise_spnrExercise),
                        childAtPosition(
                                allOf(withId(R.id.start_exercise_rl1),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1)));
        appCompatSpinner.perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(withClassName(is("android.support.v7.widget.DropDownListView")))
                .atPosition(5);
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.start_exercise_btnStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
