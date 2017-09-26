package cg.stevendende.deliciousrecipes;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.HashMap;

import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.ui.model.RecipeStep;

/**
 * Created by STEVEN on 16/05/2017.
 */

public class RecipesParserAsyncTask extends AsyncTask<Cursor, Object, String[][]> {
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String[][] doInBackground(Cursor... params) {
        //The cursor coresponding to a unique entry of RecipeStep
        Cursor cursor = params[0];
        String[][] recipes = new String[2][params[0].getCount()];

        try {
            int i = 0;
            String[] tabTexts = new String[params[0].getCount()];
            String[] tabValues = new String[params[0].getCount()];

            while (cursor.moveToNext()) {
                tabTexts[i] = cursor.getString(RecipesContract.RecipeEntry.INDEX_NAME);
                tabValues[i] = cursor.getInt(RecipesContract.RecipeEntry.INDEX_ID) + "";
                i++;
            }

            recipes[0] = tabTexts;
            recipes[1] = tabValues;
        } catch (IllegalStateException | NullPointerException ex) {
            ex.printStackTrace();
        }

        return recipes;
    }
}
