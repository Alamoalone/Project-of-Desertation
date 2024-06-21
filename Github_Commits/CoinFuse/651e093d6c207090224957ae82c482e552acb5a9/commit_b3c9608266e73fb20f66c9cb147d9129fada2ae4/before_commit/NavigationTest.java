package com.hxl.cryptonumismatist.navigating;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.KeyEvent;

import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.hxl.cryptonumismatist.R;
import com.hxl.cryptonumismatist.app.MainActivity;
import com.hxl.cryptonumismatist.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@RunWith(AndroidJUnit4.class)
@SmallTest
@HiltAndroidTest
public class NavigationTest {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    NavController rootNavController;
    NavController mainNavController;

    @Before
    public void setUp() {
        hiltRule.inject();
        scenarioRule.getScenario().onActivity(activity -> rootNavController = Navigation.findNavController(activity, R.id.nav_host_fragment_main));
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource);
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource);
    }

    @Test
    public void testAppNavigation() {
        // App starts with welcome fragment
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.welcomeFragment);

        // app navigates to navigation fragment with coins menu fragment open
        onView(withId(R.id.tv_skip)).perform(ViewActions.click());
        scenarioRule.getScenario().onActivity(activity -> mainNavController = Navigation.findNavController(activity.findViewById(R.id.fragment_main_container)));
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.navigationFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);

        // coins menu fragment navigates to coin details fragment
        onView(withId(R.id.rv_coins)).perform(actionOnItemAtPosition(0, ViewActions.click()));
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.coinDetailsFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);
        assertNotNull(rootNavController.getCurrentBackStackEntry().getArguments().getString("coin"));


        // coin details fragment navigates to navigation fragment with coins menu fragment open
        pressBack();
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.navigationFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);

        // coin menu fragment navigates to coin details fragment through search view also
        onView(withId(R.id.search_bar)).perform(click());
        onView(withId(R.id.search_view)).check(matches(isDisplayed()));

        onView(withId(com.google.android.material.R.id.search_view_edit_text))
                .perform(typeText("bitcoin"), pressKey(KeyEvent.KEYCODE_ENTER));
        onView(withId(R.id.rv_coin_search)).perform(actionOnItemAtPosition(0, ViewActions.click()));

        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.coinDetailsFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);
        assertNotNull(rootNavController.getCurrentBackStackEntry().getArguments().getString("coin"));

        // coin details fragment navigates to navigation fragment with coins menu fragment open
        pressBack();
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.navigationFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);

        // coins menu fragment closes searchView on back navigation
        onView(withId(R.id.search_view)).check(matches(isDisplayed()));
        pressBack();
        assertEquals(rootNavController.getCurrentDestination().getId(), R.id.navigationFragment);
        assertEquals(mainNavController.getCurrentDestination().getId(), R.id.coinsMenuFragment);

        // navigation fragment closes app on back press and invokes activity recreation
        pressBackUnconditionally();
        assertEquals(Lifecycle.State.CREATED, scenarioRule.getScenario().getState());
    }
}
