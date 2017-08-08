package cg.stevendende.deliciousrecipes.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.StepParsingAsyncTask;
import cg.stevendende.deliciousrecipes.ui.customviews.ExpandableTextLayoutMain;
import cg.stevendende.deliciousrecipes.ui.model.RecipeStep;

import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_RECIPE_ID;
import static cg.stevendende.deliciousrecipes.ui.MainActivity.EXTRA_STEP_ID;

public class RecipeStepDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_STEP = "cg.stevendende.extra.recipe";

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.expandable_text)
    ExpandableTextLayoutMain mDesciptionExpandableTV;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.shortDescription)
    TextView mShortDescTV;

    private RecipeStep mRecipeStep;
    private String mStepID;
    private String mRecipeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_step_details);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(EXTRA_RECIPE_ID) != null) {
            mRecipeID = intent.getStringExtra(EXTRA_RECIPE_ID);
            mStepID = intent.getStringExtra(EXTRA_STEP_ID);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_video_container,
                            StepDetailsFragment.newInstance(mRecipeID, mStepID))
                    .commit();

            //get data from DataBase and display
            Cursor cursor = StepDetailsFragment.loadStepData(mRecipeID, mStepID, this);

            /** parse the cursor to get a {@link RecipeStep } Object */
            new StepParsingAsyncTask() {
                @Override
                protected void onPostExecute(RecipeStep recipeStep) {
                    mRecipeStep = recipeStep;

                    Log.d("recipe_from_main", recipeStep == null ? "null" : "great " + recipeStep.getShortDesc());
                    mShortDescTV.setText(mRecipeStep.getShortDesc());
                    mDesciptionExpandableTV.setText(mRecipeStep.getDesc());
                }
            }.execute(cursor);
        } else {
            mRecipeStep = savedInstanceState.getParcelable(EXTRA_RECIPE_STEP);
            mShortDescTV.setText(mRecipeStep.getShortDesc());
            mDesciptionExpandableTV.setText(mRecipeStep.getDesc());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(EXTRA_RECIPE_STEP, mRecipeStep);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
