package com.example.danielandersson.ragestats.ui.activites;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.example.danielandersson.ragestats.R;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**

 * Created by danielandersson on 2017-09-13.
 */
@RunWith(AndroidJUnit4.class)
public class StatisticsActivityTest {


//    public class FragmentTestRule<F extends Fragment> extends ActivityTestRule<MainActivity> {
//        private final Class<F> mFragmentClass;
//        private F mFragment;
//
//        public FragmentTestRule(final Class<F> fragmentClass) {
//            super(MainActivity.class, true, false);
//            mFragmentClass = fragmentClass;
//        }
//    }

    @Before
    public void init(){
        mActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
//    @Rule
//    public FragmentTestRule<MainItemFragment> mMainItemFragmentFragmentTestRule = new FragmentTestRule<>(MainItemFragment.class);

    @Test
    public void recipeTopBarShowCorrectLabel() throws Exception {
        final int position = 1;

        // wait during 15 seconds for a view
        onView(isRoot()).perform(waitFor(TimeUnit.SECONDS.toMillis(2)));

        onView(withId(R.id.main_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, scrollTo()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        getInstrumentation().waitForIdleSync();

    }


    // Disclosure! Used code from below:
    // https://stackoverflow.com/questions/21417954/espresso-thread-sleep
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}