/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.sync.RecipesSyncAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_MAIN_FRAGMENT = "main";
    private static final String TAG_RECIPE_DETAILS_FRAGMENT = "steps";
    private static final String TAG_INGREDIENTS_FRAGMENT = "ingredients";
    private static final String TAG_STEP_DETAILS = "step_details";

    public static final String EXTRA_RV_SCHROLL_POSITION = "cg.stevendende.deliciousrecipes.mainactivity.schroll";
    public static final String EXTRA_RECIPE_NAME = "extra_recipe_name";
    public static final String EXTRA_STEP_NAME = "extra_step_name";
    public static final String EXTRA_STEP_ID = "extra_step_id";
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    public static final String EXTRA_CURRENT_FRAGMENT = "fragment_tag";

    private static final long SWIPE_REFRESHING_TIMEOUT = 12000;
    private String mCurrentFragment = TAG_MAIN_FRAGMENT;
    private String mSelectedRecipeName;

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    void setReadyForExit() {
        isReadyForExit = true;
        Snackbar.make((View) toolbar.getParent(), R.string.alert_exit_doubleclick, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (isReadyForExit) {
            super.onBackPressed();
        } else {
            setReadyForExit();
        }
    }
}
