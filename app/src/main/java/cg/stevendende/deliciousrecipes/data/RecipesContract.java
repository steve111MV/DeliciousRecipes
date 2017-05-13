/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class RecipesContract {

    public static final String DATABASE_NAME = "delrecipes.db";

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "cg.stevendende.deliciousrecipes";

    // The CONTENT_AUTHORITY is used to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths
    public static final String PATH_RECIPES = "recipes";
    public static final String PATH_RECIPE_INGREDIENTS = "ingredients";
    public static final String PATH_RECIPE_STEPS = "steps";


    /* Inner class that defines the table contents of Movie table */
    public static final class RecipeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPES;

        // Table name
        public static final String TABLE_NAME = "recipes";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SERVINGS = "servings";
        public static final String COLUMN_IMAGE = "image";

        public static Uri buildRecipeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getRecipeIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static final String[] COLUMNS_RECIPES = new String[]{
                RecipesContract.RecipeEntry.TABLE_NAME+"."+RecipesContract.RecipeEntry._ID,
                RecipesContract.RecipeEntry.TABLE_NAME+"."+ RecipeEntry.COLUMN_NAME,
                RecipesContract.RecipeEntry.TABLE_NAME+"."+RecipesContract.RecipeEntry.COLUMN_SERVINGS,
                RecipesContract.RecipeEntry.TABLE_NAME+"."+RecipesContract.RecipeEntry.COLUMN_IMAGE
        };

        public static final int INDEX_ID = 0;
        public static final int INDEX_NAME = 1;
        public static final int INDEX_SERVINGS = 2;
        public static final int INDEX_IMAGE = 3;

    }

    /* Inner class that defines the table contents of Ingredients table */
    public static final class IngredientEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE_INGREDIENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE_INGREDIENTS;

        // Table name
        public static final String TABLE_NAME = "ingredients";

        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT = "ingredient";

        public static final String COLUMN_UNIQUE_FIELD = "unique_field";

        public static final String COLUMN_RECIPE_ID = "recipe_id";

        public static Uri buildIngedientUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] COLUMNS_INGREDIENTS = new String[]{
                RecipesContract.IngredientEntry.TABLE_NAME+"."+RecipesContract.IngredientEntry._ID,
                RecipesContract.IngredientEntry.TABLE_NAME+"."+ IngredientEntry.COLUMN_QUANTITY,
                RecipesContract.IngredientEntry.TABLE_NAME+"."+ IngredientEntry.COLUMN_MEASURE,
                RecipesContract.IngredientEntry.TABLE_NAME+"."+ IngredientEntry.COLUMN_INGREDIENT
        };
    }

    /* Inner class that defines the table contents of Recipe-Steps table */
    public static final class RecipeStepEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE_STEPS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE_STEPS;

        // Table name
        public static final String TABLE_NAME = "steps";

        public static final String COLUMN_SHORT_DESCRIPTION = "short_desc";
        public static final String COLUMN_DESCRIPTION = "desc";
        public static final String COLUMN_VIDEO_URL = "video_url";
        public static final String COLUMN_IMAGE_URL = "thumbnail_url";

        public static final String COLUMN_UNIQUE_FIELD = "unique_field";
        public static final String COLUMN_RECIPE_ID = "recipe_id";

        public static Uri buildStepUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] COLUMNS_STEPS = new String[]{
                RecipesContract.RecipeStepEntry.TABLE_NAME+"."+RecipesContract.RecipeStepEntry._ID,
                RecipesContract.RecipeStepEntry.TABLE_NAME+"."+ RecipeStepEntry.COLUMN_SHORT_DESCRIPTION,
                RecipesContract.RecipeStepEntry.TABLE_NAME+"."+ RecipeStepEntry.COLUMN_DESCRIPTION,
                RecipesContract.RecipeStepEntry.TABLE_NAME+"."+ RecipeStepEntry.COLUMN_VIDEO_URL,
                RecipesContract.RecipeStepEntry.TABLE_NAME+"."+ RecipeStepEntry.COLUMN_IMAGE_URL
        };
    }

    }
