/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV/DeliciousRecipes
 */

package cg.stevendende.deliciousrecipes.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import cg.stevendende.deliciousrecipes.ApiJSONParser;
import cg.stevendende.deliciousrecipes.Constants;
import cg.stevendende.deliciousrecipes.MyApplication;
import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.volley.CustomJSONArrayRequest;

public class RecipesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = RecipesSyncAdapter.class.getSimpleName();
    public static final String RECIPES_JSON_URL_REDIRECT = "http://go.udacity.com/android-baking-app-json";
    public static final String RECIPES_JSON_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    // Interval at which to sync with the API, in seconds.
    // 60 seconds (1 minute) * 2 = 2 mins
    public static final int SYNC_INTERVAL = 30 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public RecipesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {


        //Log.i("Sync", "on perform sych");
        //Log.e("json", Constants.JSON);

        /*
        try{
            parseJSON(new JSONArray(Constants.JSON));
        } catch(JSONException ex){
            Log.e("JSON","test");
        }*/

        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(
                RECIPES_JSON_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        try {
                            parseJSON(jsonArray);
                            getContext().getContentResolver().notifyChange(RecipesContract.RecipeEntry.CONTENT_URI.normalizeScheme(), null);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("BALog", "volley request failed, network isue");
                    }
                }
        );

        MyApplication.getInstance(getContext()).addToRequestQueue(jsonRequest);

    }

    private void parseJSON(JSONArray jsonArray) throws JSONException {
        Log.e(LOG_TAG, jsonArray.toString());
        int recipesInsertCount = 0;
        int ingredientsInsertCount = 0;
        int stepsInsertCount = 0;

        try {
            final int INDEX_RECIPES = 0, INDEX_INGREDIENTS = 1, INDEX_STEPS = 2;
            //0:recipes, 1: ingredients, 2:steps
            Object[] apiObjects = ApiJSONParser.parseJson(jsonArray);

            recipesInsertCount = getContext()
                    .getContentResolver()
                    .bulkInsert(RecipesContract.RecipeEntry.CONTENT_URI,
                            (ContentValues[]) apiObjects[INDEX_RECIPES]);

            ingredientsInsertCount = getContext()
                    .getContentResolver()
                    .bulkInsert(RecipesContract.IngredientEntry.CONTENT_URI,
                            (ContentValues[]) apiObjects[INDEX_INGREDIENTS]);

            stepsInsertCount = getContext()
                    .getContentResolver()
                    .bulkInsert(RecipesContract.RecipeStepEntry.CONTENT_URI,
                            (ContentValues[]) apiObjects[INDEX_STEPS]);

            if (recipesInsertCount > 0) {
                getContext().getContentResolver().notifyChange(RecipesContract.RecipeEntry.CONTENT_URI, null, false);
            }

            Log.e(LOG_TAG, "inserted recipes " + recipesInsertCount);
            Log.e(LOG_TAG, "inserted ingredients for all " + ingredientsInsertCount);
            Log.e(LOG_TAG, "inserted steps for all " + stepsInsertCount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getResources().getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getResources().getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        RecipesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getResources().getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
        Log.i("Sync", "initializing");
    }
}