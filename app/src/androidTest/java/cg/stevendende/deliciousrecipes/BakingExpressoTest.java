package cg.stevendende.deliciousrecipes;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;

import com.forkingcode.espresso.contrib.DescendantViewActions;

import org.junit.Rule;
import org.junit.Test;

import cg.stevendende.deliciousrecipes.ui.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by STEVEN on 15/08/2017.
 */
//@RunWith(AndroidJUnit4.class)
public class BakingExpressoTest {

    // 1st recipe / 3rd step
    public static final String RECIPE_CHEESCAKE_FIRST__STEP_THIRD__SHORT_DESCRIPTION = "Prep the cookie crust.";
    public static final String RECIPE_NUTELLA_FOUTH__INGREDIENTS_FIRST__QUANTITY = "283.0";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true);

    //Recipes

    public void testNameATPosition() {

        //Perform a click on first element in the RecyclerView (position 0)
        onView(withId(R.id.recyclerView)).perform(
                scrollToPosition(0),
                actionOnItemAtPosition(0, DescendantViewActions.checkViewAction(matches(withText("Cheesecake")))
                ));
    }

    @Test
    public void testRecipesRecyclerView_stepsRecyclerView_stepDetailsView() {

        //RECIPES
        //Perform a click on first RECIPE element in the RecyclerView (position 0)
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, click()));

        //RECIPE steps list(RecyclerView)
        //Perform a click on third STEP element in the RecyclerView (position 2)
        onView(withId(R.id.stepsRecyclerView)).perform(actionOnItemAtPosition(2, click()));

        //Recipe Step Details-Activity
        //Match the view's text with the intended ShortDescription text (check)
        onView(withId(R.id.shortDescription)).check(matches(withText(RECIPE_CHEESCAKE_FIRST__STEP_THIRD__SHORT_DESCRIPTION)));

        // return to main activity (double press of back(button)
        pressBack();
        pressBack();
    }

    @Test
    public void testRecipesRecyclerView_ingredientsRecyclerView_ingredientDetailsView() {
        //RECIPES
        //Perform a click on second RECIPE element in the RecyclerView (position 1)
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(1, click()));

        //Perform a click on INGREDIENT view
        //  (used as a button to access ingredients list)
        onView(withId(R.id.ingredients)).perform(ViewActions.click());

        //RECIPE ingredients list(RecyclerView)
        //Perform a scroll to the last ingredient in the RecyclerView (position 9)
        onView(withId(R.id.ingredientsRecyclerView)).perform(actionOnItemAtPosition(9, scrollTo()));

        //Match the quantity TextView's text with the expected at position 9 (check)
        //onView(withId(R.id.ingredientsRecyclerView));

        /*onView(withId(R.id.ingredientsRecyclerView))
                .inRoot(assertThat(withId(R.id.quantity), org.hamcrest.Matchers.equalTo("283.0")))
                .onChildView(withId(R.id.quantity))
                .check(matches(withText(RECIPE_NUTELLA_FOUTH__INGREDIENTS_FIRST__QUANTITY)) );
        */

        // return to main activity (double press of back(button)
        pressBack();
        pressBack();
    }
}
