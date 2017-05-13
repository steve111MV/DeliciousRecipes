/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes;

import cg.stevendende.deliciousrecipes.data.RecipesContract;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class Constants {
    // Create a table to hold Recipes
    public static final String SQL_CREATE_RECIPES_TABLE = "CREATE TABLE " + RecipesContract.RecipeEntry.TABLE_NAME + " (" +

            // the ID of the recipe entry as returned by the API
            RecipesContract.RecipeEntry._ID + " INTEGER," +

            RecipesContract.RecipeEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            RecipesContract.RecipeEntry.COLUMN_SERVINGS + " INTEGER DEFAULT 0," +
            RecipesContract.RecipeEntry.COLUMN_IMAGE + " TEXT," +

            // To ensure the application doesn't duplicate recipes in database,
            // we create a UNIQUE constraint
            " UNIQUE (" + RecipesContract.RecipeEntry._ID + ") );";


    // Create a table to hold Recipe Ingredients
    public static final String SQL_CREATE_RECIPE_INGREDIENTS_TABLE = "CREATE TABLE " + RecipesContract.IngredientEntry.TABLE_NAME + " (" +

            // the ID of the recipe entry as returned by the API
            RecipesContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

            RecipesContract.IngredientEntry.COLUMN_QUANTITY + " DOUBLE, " +
            RecipesContract.IngredientEntry.COLUMN_MEASURE + " TEXT, " +
            RecipesContract.IngredientEntry.COLUMN_INGREDIENT + " TEXT, " +

            RecipesContract.IngredientEntry.COLUMN_UNIQUE_FIELD + " TEXT NOT NULL, " +
            RecipesContract.IngredientEntry.COLUMN_RECIPE_ID + " INTEGER, " +

            // Set up the "RecipeStepEntry.COLUMN_RECIPE_ID" column
            // as a foreign key to recipes table.
            " FOREIGN KEY (" + RecipesContract.IngredientEntry.COLUMN_RECIPE_ID + ") REFERENCES " +
            RecipesContract.RecipeEntry.TABLE_NAME + " (" + RecipesContract.RecipeEntry._ID + "), "

            // To ensure the application doesn't duplicate recipes,
            // we create a UNIQUE constraint
            + " UNIQUE (" + RecipesContract.IngredientEntry.COLUMN_UNIQUE_FIELD + ") );";

    // Create a table to hold Recipes
    public static final String SQL_CREATE_RECIPE_STEPS_TABLE = "CREATE TABLE " + RecipesContract.RecipeStepEntry.TABLE_NAME + " (" +

            // the ID of the recipe entry as returned by the API
            RecipesContract.RecipeStepEntry._ID + " INTEGER," +

            RecipesContract.RecipeStepEntry.COLUMN_SHORT_DESCRIPTION + " TEXT, " +
            RecipesContract.RecipeStepEntry.COLUMN_DESCRIPTION + " TEXT, " +
            RecipesContract.RecipeStepEntry.COLUMN_VIDEO_URL + " TEXT, " +
            RecipesContract.RecipeStepEntry.COLUMN_IMAGE_URL + " TEXT, " +

            RecipesContract.RecipeStepEntry.COLUMN_UNIQUE_FIELD + " TEXT NOT NULL, " +
            RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID + " INTEGER, " +

            // Set up the "IngredientEntry.COLUMN_RECIPE_ID" column
            // as a foreign key to recipes table.
            " FOREIGN KEY (" + RecipesContract.RecipeStepEntry.COLUMN_RECIPE_ID + ") REFERENCES " +
            RecipesContract.RecipeEntry.TABLE_NAME + " (" + RecipesContract.RecipeEntry._ID + "), " +

            // To ensure the application doesn't duplicate recipes,
            // we create a UNIQUE constraint
            " UNIQUE (" + RecipesContract.RecipeStepEntry.COLUMN_UNIQUE_FIELD + ") );";

    //API JSON parsing
    public static final String TAG_RECIPE_ID = "id";
    public static final String TAG_RECIPE_NAME = "name";
    public static final String TAG_RECIPE_SERVINGS = "servings";
    public static final String TAG_RECIPE_IMAGE = "image";

    public static final String TAG_RECIPE_INGREDIENTS = "ingredients";
    public static final String TAG_RECIPE_STEPS = "steps";

    public static final String TAG_INGREDIENT_QUANTITY = "quantity";
    public static final String TAG_INGREDIENT_MEASURE = "measure";
    public static final String TAG_INGREDIENT_NAME = "ingredient";

    public static final String TAG_STEP_ID = "id";
    public static final String TAG_STEP_SHORT_DESCRIPTION = "shortDescription";
    public static final String TAG_STEP_DESCRIPTION = "description";
    public static final String TAG_STEP_VIDEO_URL = "videoURL";
    public static final String TAG_STEP_IMAGE_URL = "thumbnailURL";

}