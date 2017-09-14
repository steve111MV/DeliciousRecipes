/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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

import static cg.stevendende.deliciousrecipes.ui.RecipeDetailsFragment.EXTRA_RECIPE_NAME;

public class MainActivity extends AppCompatActivity
        implements RecipesFragment.RecipesFragmentCallbackInterface,
        RecipeDetailsFragment.StepsCallbackInterface {

    private static final String TAG_MAIN_FRAGMENT = "main";
    private static final String TAG_RECIPE_DETAILS_FRAGMENT = "steps";
    private static final String TAG_INGREDIENTS_FRAGMENT = "ingredients";
    private static final String TAG_STEP_DETAILS = "step_details";

    public static final String EXTRA_STEP_ID = "step_id";
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_CURRENT_FRAGMENT = "fragment_tag";

    private static final long SWIPE_REFRESHING_TIMEOUT = 12000;
    private String mSelectedRecipeName;
    private String mCurrentFragment = TAG_MAIN_FRAGMENT;

    private boolean mTwoPane = false;
    private boolean isReadyForExit = false;
    private int mFragmentContainerId;

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

        setSupportActionBar(toolbar);

        RecipesSyncAdapter.initializeSyncAdapter(this);

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

        if (findViewById(R.id.recipe_detail_fragment_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            mFragmentContainerId = R.id.recipe_detail_fragment_container;

            // In two-pane mode, we show the detail view in this activity by
            // replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                onRecipeItemClick("1", mSelectedRecipeName);
            }
        } else {
            mTwoPane = false;
            mFragmentContainerId = R.id.fragment_container;

            //getSupportActionBar().setElevation(0f);
            }

        if (!mTwoPane && savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(mFragmentContainerId, new RecipesFragment())
                    .commit();
        } else if (savedInstanceState != null) {
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


        //If there's an Intent then (From the widget),
        // we load the corresponding recipe Steps Fragment
        Intent intent = getIntent();

        if (intent != null && intent.getAction().equals(ListViewWidgetProvider.ACTION_WIDGET_CLICK)) {
            onNewIntent(intent);
        }
    }

    @Override
    public void onRecipeItemClick(String recipeID, String recipeName) {
        /**
         * - Transition to Details fragment while in onePane
         * - Show details in second fragments when in twoPanes
         **/
        try {

            if (mTwoPane) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(mFragmentContainerId,
                                RecipeDetailsFragment.newInstance(recipeID, recipeName))
                        .commit();

            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(mFragmentContainerId,
                                RecipeDetailsFragment.newInstance(recipeID, recipeName))
                        .commit();

                //set Recipe name as Title in Toolbar
                toolbar.setTitle(recipeName);
                showBackButton();
            }

            mSelectedRecipeName = recipeName;
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;

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
        } else if (mCurrentFragment.equals(TAG_RECIPE_DETAILS_FRAGMENT)) {
            setTitle(mSelectedRecipeName);
        } else if (mCurrentFragment.equals(TAG_MAIN_FRAGMENT)) {
            resetAppTitle();
        }
    }

    @Override
    public void onStepClickListener(String recipeID, String stepID) {

        Intent intent = new Intent(MainActivity.this, RecipeStepDetailsActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeID);
        intent.putExtra(EXTRA_STEP_ID, stepID);

        startActivity(intent);
        /*getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(mFragmentContainerId,
                        StepDetailsFragment
                                .newInstance(recipeID, stepID))
                .commit();*/

        //mCurrentFragment = TAG_STEP_DETAILS;
        //showBackButton();
    }

    @Override
    public void onIngredientsClickListener(String recipeID) {
        //TODO handle ingredients click
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(mFragmentContainerId, RecipeIngredientsFragment.newInstance(recipeID, mSelectedRecipeName))
                .commit();

        mCurrentFragment = TAG_INGREDIENTS_FRAGMENT;

        setTitle(mSelectedRecipeName
                + " - " +
                getString(R.string.ingredients));

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //Handle the widget item click
        if (intent.getAction().equals(ListViewWidgetProvider.ACTION_WIDGET_CLICK)) {
            onRecipeItemClick(
                    intent.getStringExtra(ListViewWidgetProvider.EXTRA_ITEM_ID),
                    intent.getStringExtra(ListViewWidgetProvider.EXTRA_ITEM)
            );
        }
    }

    void setReadyForExit() {
        isReadyForExit = true;

        Snackbar.make((View) toolbar.getParent(), R.string.alert_exit_doubleclick, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;
            setTitle(mSelectedRecipeName);
            super.onBackPressed();
        } else if (mCurrentFragment.equals(TAG_STEP_DETAILS)) {
            mCurrentFragment = TAG_RECIPE_DETAILS_FRAGMENT;
            setTitle(mSelectedRecipeName);
            super.onBackPressed();
        } else if (mCurrentFragment.equals(TAG_RECIPE_DETAILS_FRAGMENT)) {

            mCurrentFragment = TAG_MAIN_FRAGMENT;
            resetAppTitle();
            hideBackButton();
            super.onBackPressed();
        } else if (mCurrentFragment.equals(TAG_MAIN_FRAGMENT)) {
            //TODO hint a double-click to exit
            //hideBackButton();
            if (isReadyForExit) {
                super.onBackPressed();
            } else {
                setReadyForExit();
            }

        } else super.onBackPressed();

    }
}
