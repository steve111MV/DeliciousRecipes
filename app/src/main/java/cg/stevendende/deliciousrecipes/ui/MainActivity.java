/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.sync.RecipesSyncAdapter;

import static cg.stevendende.deliciousrecipes.ui.RecipeDetailsFragment.EXTRA_RECIPE_NAME;

public class MainActivity extends AppCompatActivity
        implements RecipesFragment.RecipesFragmentCallbackInterface,
        RecipeDetailsFragment.StepsCallbackInterface {

    private static final String TAG_MAIN_FRAGMENT = "main";
    private static final String TAG_DETAILS_FRAGMENT = "steps";
    private static final String TAG_INGREDIENTS_FRAGMENT = "ingredients";
    private static final String TAG_STEP_DETAILS = "step_details";

    public static final String EXTRA_STEP_ID = "step_id";
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_CURRENT_FRAGMENT = "fragment_tag";

    private static final long SWIPE_REFRESHING_TIMEOUT = 12000;
    private String mSelectedRecipeName;
    private String mCurrentFragment = TAG_MAIN_FRAGMENT;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RecipesSyncAdapter.initializeSyncAdapter(this);

        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RecipesFragment())
                    .commit();
        } else {
            mSelectedRecipeName = savedInstanceState.getString(EXTRA_RECIPE_NAME);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RecipesSyncAdapter.syncImmediately(MainActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            swipeRefresh.setRefreshing(false);
                            //Toast.makeText(getActivity(), getResources().getString(R.string.alert_internet), Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, SWIPE_REFRESHING_TIMEOUT);
            }
        });
    }

    @Override
    public void onRecipeItemClick(String recipeID, String recipeName) {
        /**
         * - Transition to Details fragment while in onePane
         * - Show details in second fragments when in twoPanes
         **/
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container,
                            RecipeDetailsFragment.newInstance(recipeID, recipeName))
                    .commit();

            mSelectedRecipeName = recipeName;
            mCurrentFragment = TAG_DETAILS_FRAGMENT;

            //set Recipe name as Title in Toolbar
            toolbar.setTitle(recipeName);
            showBackButton();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_RECIPE_NAME, mSelectedRecipeName);
        outState.putString(EXTRA_CURRENT_FRAGMENT, mCurrentFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedRecipeName =
                savedInstanceState.getString(EXTRA_RECIPE_NAME);
        mCurrentFragment =
                savedInstanceState.getString(EXTRA_CURRENT_FRAGMENT);

        switchToolbarTitles();
    }

    /**
     *
     */
    private void switchToolbarTitles() {
        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            setTitle(mSelectedRecipeName + " - " + getString(R.string.ingredients));
        } else if (mCurrentFragment.equals(TAG_DETAILS_FRAGMENT)) {
            setTitle(mSelectedRecipeName);
        } else if (mCurrentFragment.equals(TAG_MAIN_FRAGMENT)) {
            resetAppTitle();
        }
    }

    @Override
    public void onStepClickListener(String recipeID, String stepID) {
        //TODO handle step clicks
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container,
                        StepDetailsFragment
                                .newInstance(recipeID, stepID))
                .commit();

        mCurrentFragment = TAG_STEP_DETAILS;

        showBackButton();
    }

    private void showBackButton() throws NullPointerException {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void hideBackButton() throws NullPointerException {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onIngredientsClickListener(String recipeID) {
        //TODO handle ingredients click
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, RecipeIngredientsFragment.newInstance(recipeID, mSelectedRecipeName))
                .commit();

        mCurrentFragment = TAG_INGREDIENTS_FRAGMENT;

        setTitle(mSelectedRecipeName
                + " - " +
                getString(R.string.ingredients));

        showBackButton();
    }

    private void resetAppTitle() {
        setTitle(R.string.app_name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {

        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            mCurrentFragment = TAG_DETAILS_FRAGMENT;
            setTitle(mSelectedRecipeName);
        } else if (mCurrentFragment.equals(TAG_DETAILS_FRAGMENT)) {

            mCurrentFragment = TAG_MAIN_FRAGMENT;
            resetAppTitle();
            hideBackButton();
        } else if (mCurrentFragment.equals(TAG_MAIN_FRAGMENT)) {
            //TODO hint a double-click to exit
        }

        super.onBackPressed();
    }
}
