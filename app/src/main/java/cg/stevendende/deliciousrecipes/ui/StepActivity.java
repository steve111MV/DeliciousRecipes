/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.sync.RecipesSyncAdapter;
import cg.stevendende.deliciousrecipes.widget.ListViewWidgetProvider;

public class StepActivity extends AppCompatActivity
        implements RecipeDetailsFragment.StepsCallbackInterface {

    private static final String TAG_MAIN_FRAGMENT = "main";
    private static final String TAG_RECIPE_DETAILS_FRAGMENT = "steps";
    private static final String TAG_INGREDIENTS_FRAGMENT = "ingredients";
    private static final String TAG_STEP_DETAILS = "step_details";

    public static final String EXTRA_CURRENT_FRAGMENT = "fragment_tag";

    private static final long SWIPE_REFRESHING_TIMEOUT = 12000;
    private String mSelectedRecipeName, mSelectedStepName;
    private String mCurrentFragment = TAG_MAIN_FRAGMENT;

    private String mRecipeID, mStepID;

    private boolean mTwoPane = false;
    private boolean isReadyForExit = false;
    private int mFragmentContainerId;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_step);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        showBackButton();

        RecipesSyncAdapter.initializeSyncAdapter(this);

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getAction().equals(RecipesFragment.ACTION_MAIN_ACTIVITY)) {
            mSelectedRecipeName = intent.getStringExtra(MainActivity.EXTRA_RECIPE_NAME);
            mRecipeID = intent.getStringExtra(MainActivity.EXTRA_RECIPE_ID);

            //set Recipe name as Title in Toolbar
            getSupportActionBar().setTitle(mSelectedRecipeName);
        }

        // Set activity to FullScreen when il Landscape and playing a recipe video
        if (mCurrentFragment.equals(TAG_RECIPE_DETAILS_FRAGMENT)
                && getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {

            //only after activity has recreated
            if (savedInstanceState != null) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        if (findViewById(R.id.details_fragment_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            mFragmentContainerId = R.id.details_fragment_container;

            // In two-pane mode, we show the detail view in this activity by
            // replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                onStepItemclick("1", mSelectedRecipeName);
            }
        } else {
            mTwoPane = false;
            mFragmentContainerId = R.id.fragment_container;

            //getSupportActionBar().setElevation(0f);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, RecipeDetailsFragment.newInstance(mRecipeID, mStepID))
                    .commit();

            //Load ingredients Fragment on right-screen while in TabletScreen
            if (mTwoPane) {
                onIngredientsClick(mRecipeID);
            }
        } else {
            String tmpRecipeID = savedInstanceState.getString(MainActivity.EXTRA_RECIPE_ID);
            if (tmpRecipeID != null) {
                mRecipeID = tmpRecipeID;
                mSelectedRecipeName = savedInstanceState.getString(MainActivity.EXTRA_RECIPE_NAME);
                mSelectedStepName = savedInstanceState.getString(MainActivity.EXTRA_STEP_NAME);
                mCurrentFragment = savedInstanceState.getString(MainActivity.EXTRA_CURRENT_FRAGMENT);
            }

            //keep Recipe Step OR Recipe Ingredients
            // on top of Toolbar after Srceen rotation
            switchToolbarTitles();
        }

        //If there's an Intent then (From the widget),
        // we load the corresponding recipe Steps Fragment
        //Intent intent = getIntent();


        if (intent != null && intent.getAction().equals(ListViewWidgetProvider.ACTION_WIDGET_CLICK)) {
            onNewIntent(intent);
        }

    }

    @Override
    public void onStepItemclick(String stepID, String stepName) {
        /**
         * - Transition to Details fragment while in onePane
         * - Show details in second fragments when in twoPanes
         **/

        mStepID = stepID;
        mSelectedStepName = stepName;

        try {
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;

            if (mTwoPane) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(mFragmentContainerId,
                                StepDetailsFragment.newInstance(mRecipeID, stepID))
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(mFragmentContainerId,
                                StepDetailsFragment.newInstance(mRecipeID, stepID))
                        .commit();
            }


            getSupportActionBar().setTitle(mSelectedRecipeName + " - " + mSelectedStepName);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onIngredientsClick(String recipeID) {

        //No BackStack in TwoPanes
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(mFragmentContainerId, RecipeIngredientsFragment.newInstance(recipeID, mSelectedRecipeName))
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(mFragmentContainerId, RecipeIngredientsFragment.newInstance(recipeID, mSelectedRecipeName))
                    .commit();
        }
        mCurrentFragment = TAG_INGREDIENTS_FRAGMENT;

        getSupportActionBar().setTitle(mSelectedRecipeName
                + " - " +
                getString(R.string.ingredients));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(MainActivity.EXTRA_RECIPE_ID, mRecipeID);
        outState.putString(MainActivity.EXTRA_STEP_ID, mStepID);
        outState.putString(MainActivity.EXTRA_RECIPE_NAME, mSelectedRecipeName);
        outState.putString(MainActivity.EXTRA_STEP_NAME, mSelectedStepName);
        outState.putString(EXTRA_CURRENT_FRAGMENT, mCurrentFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedRecipeName =
                savedInstanceState.getString(MainActivity.EXTRA_RECIPE_NAME);
        mSelectedStepName =
                savedInstanceState.getString(MainActivity.EXTRA_STEP_NAME);
        mRecipeID =
                savedInstanceState.getString(MainActivity.EXTRA_RECIPE_ID);
        mStepID =
                savedInstanceState.getString(MainActivity.EXTRA_STEP_ID);
        mCurrentFragment =
                savedInstanceState.getString(EXTRA_CURRENT_FRAGMENT);
    }

    /**
     *
     */
    private void switchToolbarTitles() {
        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            getSupportActionBar().setTitle(mSelectedRecipeName + " - " + getString(R.string.ingredients));
        } else {
            if (mTwoPane)
                getSupportActionBar().setTitle(mSelectedRecipeName + " - " + mSelectedStepName);
            else
                getSupportActionBar().setTitle(mSelectedRecipeName);
        }
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mRecipeID = intent.getStringExtra(ListViewWidgetProvider.EXTRA_ITEM_RECIPE_ID);
        mSelectedRecipeName = intent.getStringExtra(ListViewWidgetProvider.EXTRA_ITEM);

        //Handle the widget item click
        if (intent.getAction().equals(ListViewWidgetProvider.ACTION_WIDGET_CLICK)) {
            onStepItemclick(mRecipeID, mSelectedRecipeName);
        }
    }

    @Override
    public void onBackPressed() {

        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;
            getSupportActionBar().setTitle(mSelectedRecipeName);
            super.onBackPressed();
        } else if (mCurrentFragment.equals(TAG_STEP_DETAILS)) {
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;
            getSupportActionBar().setTitle(mSelectedRecipeName);
            hideBackButton();
            super.onBackPressed();
        } else super.onBackPressed();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
