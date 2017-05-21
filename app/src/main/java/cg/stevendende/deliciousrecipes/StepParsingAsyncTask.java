package cg.stevendende.deliciousrecipes;

import android.database.Cursor;
import android.os.AsyncTask;

import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.ui.model.RecipeStep;

/**
 * Created by STEVEN on 16/05/2017.
 */

public class StepParsingAsyncTask extends AsyncTask<Cursor, Object, RecipeStep> {
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
    protected RecipeStep doInBackground(Cursor... params) {
        //The cursor coresponding to a unique entry of RecipeStep
        Cursor cursor = params[0];
        RecipeStep step = null;
        try {
            if (cursor.moveToFirst()) {
                step = new RecipeStep();
                step.setId(cursor.getInt(RecipesContract.RecipeStepEntry.INDEX_ID));
                step.setShortDesc(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_SHORT_DESC));
                step.setDesc(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_DESCRIPTION));
                step.setVideoUrl(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_VIDEO_URL));
                step.setThumbnailUrl(cursor.getString(RecipesContract.RecipeStepEntry.INDEX_THUMBNAIL_URL));
            }
        } catch (IllegalStateException | NullPointerException ex) {
            ex.printStackTrace();
        }

        return step;
    }
}
