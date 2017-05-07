package cg.stevendende.deliciousrecipes;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import cg.stevendende.deliciousrecipes.data.RecipesContract;

/**
 * Created by STEVEN on 07/05/2017.
 */

public class ApiJSONParser {
    public static final Object[] parseJson(JSONArray jsonArray) throws JSONException {

        ContentValues[]
                recipesValuesArray = null,
                ingredientsValuesArray = null,
                recipeStepsValuesArray = null;


        Vector<ContentValues> recipesValuesVector =
                new Vector<ContentValues>(jsonArray.length());

        Vector<ContentValues> ingredientsValuesVector =
                new Vector<ContentValues>(jsonArray.length());

        Vector<ContentValues> stepsValuesVector =
                new Vector<ContentValues>(jsonArray.length());

        //Temporary objects
        ContentValues values = null;
        JSONArray ingredientsJsonArray, stepsJsonAray;

        //looper
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject recipeJsonObj = jsonArray.getJSONObject(i);
            values = new ContentValues();

            //Parse the Recipe first -> then the recipe's ingredients & steps
            values.put(
                    RecipesContract.RecipeEntry._ID,
                    recipeJsonObj.getInt(Constants.TAG_RECIPE_ID));
            values.put(
                    RecipesContract.RecipeEntry.COLUMN_NAME,
                    recipeJsonObj.getString(Constants.TAG_RECIPE_NAME));
            values.put(
                    RecipesContract.RecipeEntry.COLUMN_SERVINGS,
                    recipeJsonObj.getInt(Constants.TAG_RECIPE_SERVINGS));
            values.put(
                    RecipesContract.RecipeEntry.COLUMN_IMAGE,
                    recipeJsonObj.getString(Constants.TAG_RECIPE_IMAGE));
            recipesValuesVector.add(values);

            // for each recipe, we parse & append its ingredients & steps
            // respectively into ingredientsValuesVector & stepsValuesVector
            ingredientsJsonArray = recipeJsonObj.getJSONArray(Constants.TAG_RECIPE_INGREDIENTS);
            stepsJsonAray = recipeJsonObj.getJSONArray(Constants.TAG_RECIPE_STEPS);

            //parsing the ingredients
            for (int j = 0; j < ingredientsJsonArray.length(); j++) {

                JSONObject obj = ingredientsJsonArray.getJSONObject(i);
                values = new ContentValues();

                // this is the ID of the RECIPE,
                // the foreign key referencing RECIPES database table entry
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_RECIPE_ID,
                        recipeJsonObj.getInt(Constants.TAG_RECIPE_ID));

                values.put(
                        RecipesContract.IngredientEntry.COLUMN_QUANTITY,
                        obj.getDouble(Constants.TAG_INGREDIENT_QUANTITY));
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_QUANTITY,
                        obj.getString(Constants.TAG_INGREDIENT_MEASURE));
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_QUANTITY,
                        obj.getString(Constants.TAG_INGREDIENT_NAME));

                //this vector will contain the ingredients of all recipes
                ingredientsValuesVector.add(values);
            }

            //parsing the recipes steps
            for (int j = 0; j < stepsJsonAray.length(); j++) {

                JSONObject obj = stepsJsonAray.getJSONObject(i);
                values = new ContentValues();

                // this is the ID of the RECIPE,
                // the foreign key referencing RECIPES database table entry
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID,
                        recipeJsonObj.getInt(Constants.TAG_RECIPE_ID));

                //ID from API, usefull for positioning
                values.put(
                        RecipesContract.RecipeStepEntry._ID,
                        obj.getInt(Constants.TAG_STEP_ID));
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION,
                        obj.getString(Constants.TAG_STEP_SHORT_DESCRIPTION));
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_DESCRIPTION,
                        obj.getString(Constants.TAG_STEP_DESCRIPTION));
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_VIDEO_URL,
                        obj.getString(Constants.TAG_STEP_VIDEO_URL));
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_IMAGE_URL,
                        obj.getString(Constants.TAG_STEP_IMAGE_URL));

                //this vector will contain the steps of all recipes
                stepsValuesVector.add(values);
            }
        }

        //Let's convert the Vector<ContentValues> to a ContentValues Array.
        if (recipesValuesVector.size() > 0) {
            recipesValuesArray = new ContentValues[recipesValuesVector.size()];
            recipesValuesVector.toArray(recipesValuesArray);
        }
        if (ingredientsValuesVector.size() > 0) {
            ingredientsValuesArray = new ContentValues[ingredientsValuesVector.size()];
            ingredientsValuesVector.toArray(ingredientsValuesArray);
        }
        if (stepsValuesVector.size() > 0) {
            recipeStepsValuesArray = new ContentValues[stepsValuesVector.size()];
            stepsValuesVector.toArray(recipeStepsValuesArray);
        }

        return new Object[]{
                recipesValuesArray,
                ingredientsValuesArray,
                recipeStepsValuesArray
        };
    }
}
