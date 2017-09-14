package cg.stevendende.deliciousrecipes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.data.RecipesContract;
import cg.stevendende.deliciousrecipes.model.WidgetItem;
import cg.stevendende.deliciousrecipes.ui.MainActivity;

/**
 * Created by STEVEN on 10/08/2017.
 */

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int mCount = 10;
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;

    public ListViewRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        //for (int i = 0; i < mCount; i++) {
        //mWidgetItems.add(new WidgetItem(" Widget number "+i));
        //}

        mCursor = mContext.getContentResolver().query(RecipesContract.RecipeEntry.CONTENT_URI,

                RecipesContract.RecipeEntry.COLUMNS_RECIPES,
                null, null, RecipesContract.RecipeEntry.TABLE_NAME
                        + "." + RecipesContract.RecipeEntry._ID + " DESC");

        if (mCursor != null) {

            while (mCursor.moveToNext()) {
                mWidgetItems.add(new WidgetItem(mCursor.getString(RecipesContract.RecipeEntry.INDEX_NAME)));
            }
        }


    }

    /**
     * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
     * RemoteViewsFactory to respond to data changes by updating any internal references.
     * <p>
     * Note: expensive tasks can be safely performed synchronously within this method. In the
     * interim, the old data will be displayed within the widget.
     *
     * @see AppWidgetManager#notifyAppWidgetViewDataChanged(int[], int)
     */
    @Override
    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        Uri uri = RecipesContract.RecipeEntry.CONTENT_URI;
        mCursor = mContext.getContentResolver().query(uri,
                null,
                null,
                null,
                RecipesContract.RecipeEntry._ID + " DESC");

        Binder.restoreCallingIdentity(identityToken);
    }

    /**
     * Called when the last RemoteViewsAdapter that is associated with this factory is
     * unbound.
     */
    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    /**
     * See { @link Adapter#getCount()}
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {

        return mCursor != null ? mCursor.getCount() : 0;
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }
        RemoteViews widgetRow = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        widgetRow.setTextViewText(android.R.id.text1, mCursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));

        widgetRow.setInt(android.R.id.text1, "setTextColor", Color.WHITE);

        //Define a click Intent
        Intent intent = new Intent();
        Bundle extras = new Bundle();

        //set DATA i bundle
        extras.putString(ListViewWidgetProvider.EXTRA_ITEM_ID, mCursor.getInt(RecipesContract.RecipeEntry.INDEX_ID) + "");
        extras.putString(ListViewWidgetProvider.EXTRA_ITEM, mCursor.getString(RecipesContract.RecipeEntry.INDEX_NAME));

        intent.putExtras(extras);
        widgetRow.setOnClickFillInIntent(android.R.id.text1, intent);

        // Make it possible to distinguish the individual on-click
        // action of a given item
        //widgetRow.setOnClickPendingIntent(R.id.widgetListView, PendingIntent.getBroadcast(mContext,0,intent, PendingIntent.FLAG_UPDATE_CURRENT));

        // Return the RemoteViews object.
        return widgetRow;
    }

    /**
     * This allows for the use of a custom loading view which appears between the time that
     * {@link #getViewAt(int)} is called and returns. If null is returned, a default loading
     * view will be used.
     *
     * @return The RemoteViews representing the desired loading view.
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /**
     * See { @link Adapter#getViewTypeCount()}.
     *
     * @return The number of types of Views that will be returned by this factory.
     */
    @Override
    public int getViewTypeCount() {
        //former value was 0
        return 1;
    }

    /**
     * See { @link Adapter#getItemId(int)}.
     *
     * @param position The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return mCursor.moveToPosition(position) ? mCursor.getLong(0) : position;
    }

    /**
     * See { @link Adapter#hasStableIds()}.
     *
     * @return True if the same id always refers to the same object.
     */
    @Override
    public boolean hasStableIds() {
        //default was false
        return true;
    }

}