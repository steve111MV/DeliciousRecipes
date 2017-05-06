package cg.stevendende.deliciousrecipes.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by STEVEN on 06/05/2017.
 */

public class RecipesContentProvider extends ContentProvider {



    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RecipesDBHelper mOpenHelper;

    static final int RECIPES = 100;
    static final int RECIPE = 101;
    static final int INGREDIENTS = 200;
    static final int RECIPE_STEPS = 300;
    static final int RECIPE_STEP = 301;

    /**
     * Implement this to initialize your content provider on startup.
     * This method is called for all registered content providers on the
     * application main thread at application launch time.  It must not perform
     * lengthy operations, or application startup will be delayed.
     * <p>
     * <p>You should defer nontrivial initialization (such as opening,
     * upgrading, and scanning databases) until the content provider is used
     * (via {@link #query}, {@link #insert}, etc).  Deferred initialization
     * keeps application startup fast, avoids unnecessary work if the provider
     * turns out not to be needed, and stops database errors (such as a full
     * disk) from halting application launch.
     * <p>
     * <p>If you use SQLite, {@link SQLiteOpenHelper}
     * is a helpful utility class that makes it easy to manage databases,
     * and will automatically defer opening until first use.  If you do use
     * SQLiteOpenHelper, make sure to avoid calling
     * {@link SQLiteOpenHelper#getReadableDatabase} or
     * {@link SQLiteOpenHelper#getWritableDatabase}
     * from this method.  (Instead, override
     * {@link SQLiteOpenHelper#onOpen} to initialize the
     * database when it is first opened.)
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        return false;
    }


    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecipesContract.CONTENT_AUTHORITY;

        // All RECIPES
        matcher.addURI(authority, RecipesContract.PATH_RECIPES, RECIPES);
        // RECIPES by ID
        matcher.addURI(authority, RecipesContract.PATH_RECIPES + "/#", RECIPE);

        //INGREDIENTS
        matcher.addURI(authority, RecipesContract.PATH_RECIPE_INGREDIENTS, INGREDIENTS);

        //RECIPE STEPS
        matcher.addURI(authority, RecipesContract.PATH_RECIPE_STEPS, RECIPE_STEPS);
        matcher.addURI(authority, RecipesContract.PATH_RECIPE_STEPS + "/#", RECIPE_STEP);

        return matcher;
    }

        /**
         * Implement this to handle query requests from clients.
         * This method can be called from multiple threads, as described in
         * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
         * and Threads</a>.
         * <p>
         * Example client call:<p>
         * <pre>// Request a specific record.
         * Cursor managedCursor = managedQuery(
         * ContentUris.withAppendedId(Contacts.People.CONTENT_URI, 2),
         * projection,    // Which columns to return.
         * null,          // WHERE clause.
         * null,          // WHERE clause value substitution
         * People.NAME + " ASC");   // Sort order.</pre>
         * Example implementation:<p>
         * <pre>// SQLiteQueryBuilder is a helper class that creates the
         * // proper SQL syntax for us.
         * SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
         *
         * // Set the table we're querying.
         * qBuilder.setTables(DATABASE_TABLE_NAME);
         *
         * // If the query ends in a specific record number, we're
         * // being asked for a specific record, so set the
         * // WHERE clause in our query.
         * if((URI_MATCHER.match(uri)) == SPECIFIC_MESSAGE){
         * qBuilder.appendWhere("_id=" + uri.getPathLeafId());
         * }
         *
         * // Make the query.
         * Cursor c = qBuilder.query(mDb,
         * projection,
         * selection,
         * selectionArgs,
         * groupBy,
         * having,
         * sortOrder);
         * c.setNotificationUri(getContext().getContentResolver(), uri);
         * return c;</pre>
         *
         * @param uri           The URI to query. This will be the full URI sent by the client;
         *                      if the client is requesting a specific record, the URI will end in a record number
         *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
         *                      that _id value.
         * @param projection    The list of columns to put into the cursor. If
         *                      {@code null} all columns are included.
         * @param selection     A selection criteria to apply when filtering rows.
         *                      If {@code null} then all rows are included.
         * @param selectionArgs You may include ?s in selection, which will be replaced by
         *                      the values from selectionArgs, in order that they appear in the selection.
         *                      The values will be bound as Strings.
         * @param sortOrder     How the rows in the cursor should be sorted.
         *                      If {@code null} then the provider is free to define the sort order.
         * @return a Cursor or {@code null}.
         */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor = null;

        switch (sUriMatcher.match(uri)) {

            case RECIPES: {
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        RecipesContract.RecipeEntry.TABLE_NAME,
                        RecipesContract.RecipeEntry.COLUMNS_RECIPES,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        RecipesContract.RecipeEntry.TABLE_NAME+"."+RecipesContract.RecipeEntry._ID+" DESC"
                );

            }
            break;
            case INGREDIENTS: {
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        RecipesContract.IngredientEntry.TABLE_NAME,
                        RecipesContract.IngredientEntry.COLUMNS_INGREDIENTS,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        RecipesContract.RecipeEntry.TABLE_NAME+"."+RecipesContract.RecipeEntry._ID
                );

            }
            break;
            case RECIPE_STEPS: {
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        RecipesContract.RecipeStepEntry.TABLE_NAME,
                        RecipesContract.RecipeStepEntry.COLUMNS_STEPS,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        RecipesContract.RecipeStepEntry.TABLE_NAME+"."+RecipesContract.RecipeStepEntry._ID
                );

            }
            break;

        }

        return returnCursor;
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the
     * given URI.  The returned MIME type should start with
     * <code>vnd.android.cursor.item</code> for a single record,
     * or <code>vnd.android.cursor.dir/</code> for multiple items.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>Note that there are no permissions needed for an application to
     * access this information; if your content provider requires read and/or
     * write permissions, or is not exported, all applications can still call
     * this method regardless of their access permissions.  This allows them
     * to retrieve the MIME type for a URI when dispatching intents.
     *
     * @param uri the URI to query.
     * @return a MIME type string, or {@code null} if there is no type.
     */
    @Nullable
    @Override
    public String getType(Uri uri) {

        // we use the Uri Matcher to determine what kind of URI it is.
        final int uriType = sUriMatcher.match(uri);

        switch (uriType) {
            case RECIPES:
                return RecipesContract.RecipeEntry.CONTENT_TYPE;
            case RECIPE:
                return RecipesContract.RecipeEntry.CONTENT_TYPE;
            case INGREDIENTS:
                return RecipesContract.IngredientEntry.CONTENT_TYPE;
            case RECIPE_STEPS:
                return RecipesContract.RecipeStepEntry.CONTENT_TYPE;
            case RECIPE_STEP:
                return RecipesContract.RecipeStepEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Implement this to handle requests to insert a new row.
     * As a courtesy, call {@link android.content.ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after inserting.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri    The content:// URI of the insertion request. This must not be {@code null}.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The URI for the newly inserted item.
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = uri;

        switch (match) {
            case RECIPES: {
                long _id = 0;

                try {
                    _id = db.insertOrThrow(RecipesContract.RecipeEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex) {
                    ex.printStackTrace();
                }

                if (_id > 0)
                    returnUri = RecipesContract.RecipeEntry.buildRecipeUri(values.getAsLong(RecipesContract.RecipeEntry._ID));
                break;
            }

            case INGREDIENTS: {
                long _id = 0;

                try {
                    _id = db.insertOrThrow(RecipesContract.IngredientEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex) {
                    ex.printStackTrace();
                }

                if (_id > 0)
                    returnUri = RecipesContract.IngredientEntry.buildIngedientUri(values.getAsLong(RecipesContract.IngredientEntry._ID));
                break;
            }

            case RECIPE_STEPS: {
                long _id = 0;

                try {
                    _id = db.insertOrThrow(RecipesContract.RecipeStepEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex) {
                    ex.printStackTrace();
                }

                if (_id > 0)
                    returnUri = RecipesContract.RecipeStepEntry.buildStepUri(values.getAsLong(RecipesContract.RecipeStepEntry._ID));
                break;
            }
        }

        return returnUri;
    }

    /**
     * Implement this to handle requests to delete one or more rows.
     * The implementation should apply the selection clause when performing
     * deletion, allowing the operation to affect multiple rows in a directory.
     * As a courtesy, call {@link android.content.ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after deleting.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>The implementation is responsible for parsing out a row ID at the end
     * of the URI, if a specific row is being deleted. That is, the client would
     * pass in <code>content://contacts/people/22</code> and the implementation is
     * responsible for parsing the record number (22) when creating a SQL statement.
     *
     * @param uri           The full URI to query, including a row ID (if a specific record is requested).
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs
     * @return The number of rows affected.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        String deleteId;

        // this makes delete all rows return the number of rows deleted
        //if (null == selection) selection = "1";
        switch (match) {
            case RECIPES:
                deleteId = uri.getLastPathSegment();
                selectionArgs =  new String[]{deleteId};
                rowsDeleted = db.delete(
                        RecipesContract.RecipeEntry.TABLE_NAME, RecipesContract.RecipeEntry._ID+"=?", selectionArgs);
                break;
            case INGREDIENTS:
                deleteId = uri.getLastPathSegment();
                selectionArgs =  new String[]{deleteId};
                rowsDeleted = db.delete(
                        RecipesContract.IngredientEntry.TABLE_NAME, RecipesContract.IngredientEntry._ID+"=?", selectionArgs);
                break;
            case RECIPE_STEPS:
                deleteId = uri.getLastPathSegment();
                selectionArgs =  new String[]{deleteId};
                rowsDeleted = db.delete(
                        RecipesContract.RecipeStepEntry.TABLE_NAME, RecipesContract.RecipeStepEntry._ID+"=?", selectionArgs);
                break;
            default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Implement this to handle requests to update one or more rows.
     * The implementation should update all rows matching the selection
     * to set the columns according to the provided values map.
     * As a courtesy, call {@link android.content.ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()}
     * after updating.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri           The URI to query. This can potentially have a record ID if this
     *                      is an update request for a specific record.
     * @param values        A set of column_name/value pairs to update in the database.
     *                      This must not be {@code null}.
     * @param selection     An optional filter to match rows to update.
     * @param selectionArgs
     * @return the number of rows affected.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match){
            case RECIPES:
                if(selectionArgs == null) {
                    selectionArgs = new String[]{RecipesContract.RecipeEntry.getRecipeIdFromUri(uri)+""};
                }
                rowsUpdated = db.update(
                        RecipesContract.RecipeEntry.TABLE_NAME,
                        values,
                        RecipesContract.RecipeEntry.TABLE_NAME + "." + RecipesContract.RecipeEntry._ID + "=?",
                        selectionArgs);
                break;
            case INGREDIENTS:
                if(selectionArgs == null) {
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }
                rowsUpdated = db.update(
                        RecipesContract.IngredientEntry.TABLE_NAME,
                        values,
                        RecipesContract.IngredientEntry.TABLE_NAME + "." + RecipesContract.IngredientEntry._ID + "=?",
                        selectionArgs);
                break;
            case RECIPE_STEPS:
                if(selectionArgs == null) {
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }
                rowsUpdated = db.update(
                        RecipesContract.RecipeStepEntry.TABLE_NAME,
                        values,
                        RecipesContract.RecipeStepEntry.TABLE_NAME + "." + RecipesContract.RecipeStepEntry._ID + "=?",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.i("change notified", "change has notified update for " + uri);
        }

        return rowsUpdated;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case RECIPES:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {

                        try {
                            long _id = db.insertOrThrow(RecipesContract.RecipeEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        } catch (SQLiteConstraintException ex) {
                            ex.printStackTrace();
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case INGREDIENTS:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {

                        try {
                            long _id = db.insertOrThrow(RecipesContract.IngredientEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        } catch (SQLiteConstraintException ex) {
                            ex.printStackTrace();
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case RECIPE_STEPS:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {

                        try {
                            long _id = db.insertOrThrow(RecipesContract.RecipeStepEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        } catch (SQLiteConstraintException ex) {
                            ex.printStackTrace();
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        if(returnCount>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }


    // This is a method specifically to assist the testing framework in running smoothly.
    // avoids a conflict by providing a way to terminate the ContentProvider while in andoidTest
    //
    // more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
