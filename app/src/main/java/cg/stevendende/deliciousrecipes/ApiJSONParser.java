package cg.stevendende.deliciousrecipes;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
                stepsValuesArray = null;


        Vector<ContentValues> recipesValuesVector =
                new Vector<ContentValues>(jsonArray.length());

        ArrayList<ContentValues> ingredientsValuesList = new ArrayList<>();
        ArrayList<ContentValues> stepsValuesList = new ArrayList<>();

        //Temporary objects
        ContentValues values = null;
        String uniqueValue;
        JSONArray ingredientsJsonArray, stepsJsonAray;

        //looper
        JSONObject recipeJsonObj;
        for (int i = 0; i < jsonArray.length(); i++) {
            recipeJsonObj = jsonArray.getJSONObject(i);
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
            // respectively into ingredientsValuesList & stepsValuesList
            ingredientsJsonArray = recipeJsonObj.getJSONArray(Constants.TAG_RECIPE_INGREDIENTS);
            stepsJsonAray = recipeJsonObj.getJSONArray(Constants.TAG_RECIPE_STEPS);

            //*******************************************************
            //parsing the ingredients ****************************
            JSONObject ingredient;
            for (int j = 0; j < ingredientsJsonArray.length(); j++) {

                ingredient = ingredientsJsonArray.getJSONObject(j);
                values = new ContentValues();

                // this is the ID of the RECIPE,
                // the foreign key referencing RECIPES database table entry
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_RECIPE_ID,
                        recipeJsonObj.getInt(Constants.TAG_RECIPE_ID));

                // A Unique field -- protection against duplications
                uniqueValue = recipeJsonObj.getInt(Constants.TAG_RECIPE_ID)
                        + "_" + ingredient.getString(Constants.TAG_INGREDIENT_NAME);
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_UNIQUE_FIELD,
                        uniqueValue);

                values.put(
                        RecipesContract.IngredientEntry.COLUMN_QUANTITY,
                        ingredient.getDouble(Constants.TAG_INGREDIENT_QUANTITY));
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_MEASURE,
                        ingredient.getString(Constants.TAG_INGREDIENT_MEASURE));
                values.put(
                        RecipesContract.IngredientEntry.COLUMN_INGREDIENT,
                        ingredient.getString(Constants.TAG_INGREDIENT_NAME));

                //this vector will contain the ingredients of all recipes
                ingredientsValuesList.add(values);
            }

            //******************************************************
            //parsing the recipes steps ***************************
            JSONObject step;
            for (int k = 0; k < stepsJsonAray.length(); k++) {

                step = stepsJsonAray.getJSONObject(k);
                values = new ContentValues();

                // this is the ID of the RECIPE,
                // the foreign key referencing RECIPES database table entry
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID,
                        jsonArray.getJSONObject(i).getInt(Constants.TAG_RECIPE_ID));

                //ID from API, usefull for positioning
                values.put(
                        RecipesContract.RecipeStepEntry._ID,
                        // +1 beacause the ID starts at 0, & the CursorAdapter
                        // will throw an exception
                        step.getInt(Constants.TAG_STEP_ID) + 1);

                // A Unique field -- protection against duplications
                uniqueValue = recipeJsonObj.getInt(Constants.TAG_RECIPE_ID)
                        + "_" + step.getInt(Constants.TAG_STEP_ID);
                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_UNIQUE_FIELD,
                        uniqueValue);

                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION,
                        step.getString(Constants.TAG_STEP_SHORT_DESCRIPTION));

                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_DESCRIPTION,
                        step.getString(Constants.TAG_STEP_DESCRIPTION));

                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_VIDEO_URL,
                        step.getString(Constants.TAG_STEP_VIDEO_URL));

                values.put(
                        RecipesContract.RecipeStepEntry.COLUMN_IMAGE_URL,
                        step.getString(Constants.TAG_STEP_IMAGE_URL));

                //this vector will contain the steps of all recipes
                stepsValuesList.add(values);
            }
        }

        //Let's convert the Vector<ContentValues> to a ContentValues Array.
        if (recipesValuesVector.size() > 0) {
            recipesValuesArray = new ContentValues[recipesValuesVector.size()];
            recipesValuesVector.toArray(recipesValuesArray);
        }
        if (ingredientsValuesList.size() > 0) {
            ingredientsValuesArray = ingredientsValuesList
                    .toArray(new ContentValues[ingredientsValuesList.size()]);
        }
        if (stepsValuesList.size() > 0) {
            stepsValuesArray = stepsValuesList
                    .toArray(new ContentValues[stepsValuesList.size()]);
        }

        return new Object[]{
                recipesValuesArray,
                ingredientsValuesArray,
                stepsValuesArray
        };
    }
}
