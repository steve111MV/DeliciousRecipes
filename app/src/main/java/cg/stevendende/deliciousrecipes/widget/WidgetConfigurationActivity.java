package cg.stevendende.deliciousrecipes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.RecipesParserAsyncTask;
import cg.stevendende.deliciousrecipes.data.RecipesContract;

public class WidgetConfigurationActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_RECIPE_PREF = "pref_selected_recipe";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    String[][] recipesTextValues;

    @BindView(R.id.recipes_spiner)
    Spinner mSpinner;

    @BindView(R.id.add_button)
    Button mButton;

    public WidgetConfigurationActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);
        ButterKnife.bind(this);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID,
        // finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        new RecipesParserAsyncTask() {

            @Override
            protected void onPostExecute(String[][] recipesTextValues2) {
                super.onPostExecute(recipesTextValues2);

                recipesTextValues = recipesTextValues2;

                mSpinner.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        if (recipesTextValues != null)
                            return recipesTextValues[0].length;
                        return 0;
                    }

                    @Override
                    public Object getItem(int position) {
                        return null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {


                        if (convertView == null)
                            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

                        //Show recipe name on Spiner Item
                        ((TextView) convertView.findViewById(android.R.id.text1)).setText(recipesTextValues[0][position]);

                        return convertView;
                    }
                });

                mSpinner.setSelection(getSelectedPosition(loadIntervalPref(getApplicationContext(), mAppWidgetId)));

                View.OnClickListener mOnClickListener = new View.OnClickListener() {
                    public void onClick(View v) {
                        final Context context = WidgetConfigurationActivity.this;

                        // When the button is clicked, store the RecipeIS string locally
                        String[] values = recipesTextValues[1];
                        String widgetText = values[mSpinner.getSelectedItemPosition()];
                        saveIntervalPref(context, mAppWidgetId, widgetText);

                        // Make sure we pass back the original appWidgetId
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    }
                };

                mButton.setOnClickListener(mOnClickListener);

            }
        }.execute(
                // Get Recipes cursor
                getContentResolver().query(RecipesContract.RecipeEntry.CONTENT_URI,
                        RecipesContract.RecipeEntry.COLUMNS_RECIPES,
                        null, null, null)
        );

    }


    private int getSelectedPosition(String interval) {

        //Recipes values array is at position 1 in recipesTextValues
        String[] values = recipesTextValues[1];
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(interval)) return i;
        }
        return 0;

    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveIntervalPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(EXTRA_SELECTED_RECIPE_PREF, text);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadIntervalPref(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String DEFAULT_RECIPE = "1";
        String interval = prefs.getString(EXTRA_SELECTED_RECIPE_PREF, DEFAULT_RECIPE);
        return interval;
    }
}
