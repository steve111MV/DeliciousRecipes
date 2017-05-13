/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.sync.RecipesSyncAdapter;

public class MainActivity extends AppCompatActivity
        implements RecipesFragment.RecipesFragmentCallbackInterface,
        RecipeDetailsFragment.StepsCallbackInterface {

    private static final String TAG_MAIN_FRAGMENT = "main";
    private static final String TAG_DETAILS_FRAGMENT = "steps";
    private static final String TAG_INGREDIENTS_FRAGMENT = "ingredients";

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
    public void onRecipeItemClick(String id, String recipeName) {
        /**
         * - Transition to Details fragment while in onePane
         * - Show details in second fragments when in twoPanes
         **/
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container,
                            RecipeDetailsFragment.newInstance(id, recipeName))
                    .commit();

            mSelectedRecipeName = recipeName;
            mCurrentFragment = TAG_DETAILS_FRAGMENT;

            //set Recipe name as Title in Toolbar
            toolbar.setTitle(recipeName);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        // here
        //
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //here
        //
    }

    @Override
    public void onStepClickListener(String stepID) {
        //TODO handle step clicks
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
    }

    private void resetAppTitle() {
        setTitle(R.string.app_name);
    }

    @Override
    public void onBackPressed() {

        if (mCurrentFragment.equals(TAG_INGREDIENTS_FRAGMENT)) {
            mCurrentFragment = TAG_DETAILS_FRAGMENT;
            setTitle(mSelectedRecipeName);
        } else if (mCurrentFragment.equals(TAG_DETAILS_FRAGMENT)) {
            resetAppTitle();
            mCurrentFragment = TAG_MAIN_FRAGMENT;
        } else if (mCurrentFragment.equals(TAG_MAIN_FRAGMENT)) {
            //TODO hint a double-click to exit
        }

        super.onBackPressed();
    }
}
