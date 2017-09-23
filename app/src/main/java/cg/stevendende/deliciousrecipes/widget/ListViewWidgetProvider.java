package cg.stevendende.deliciousrecipes.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import cg.stevendende.deliciousrecipes.R;
import cg.stevendende.deliciousrecipes.ui.MainActivity;

/**
 * Created by STEVEN on 08/08/2017.
 */

public class ListViewWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TOAST = "cg.stevendende.bakingapp.widget.TOAST_ACTION";
    public static final String ACTION_DETAILS = "cg.stevendende.bakingapp.widget.DETAILS_ACTION";
    public static final String ACTION_WIDGET_CLICK = "cg.stevendende.bakingapp.widget.CLICK_ACTION";
    public static final String EXTRA_ITEM = "cg.stevendende.backingapp.widget.EXTRA_ITEM";
    public static final String EXTRA_ITEM_STEP_ID = "cg.stevendende.backingapp.widget.EXTRA_ITEM_STEP_ID";
    public static final String EXTRA_ITEM_RECIPE_ID = "cg.stevendende.backingapp.widget.EXTRA_ITEM_RECIPE_ID";

    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is TOAST_ACTION. If it is, the app widget
    // displays a Toast message for the current item.
    @Override
    public void onReceive(Context context, Intent intent) {

        //AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(ACTION_DETAILS)) {
            Log.i("BALog", "clicked on e item, action:" + intent.getAction());
        }

        Toast.makeText(context, "Touched view ", Toast.LENGTH_SHORT).show();
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            RemoteViews remoteViews = updateWidgetListView(context,
                    appWidgetIds[i]);

            //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.widgetListView);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i],
                    R.id.widgetListView);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.

        //For ListView
        Intent svcIntent = new Intent(context, ListViewWidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(appWidgetId, R.id.widgetListView, svcIntent);

        rv.setEmptyView(R.id.widgetListView, R.id.empty_view);
        //When item is clicked on ListView
        /*
        Intent startActivityIntent = new Intent(context, ListViewWidgetProvider.class);
        startActivityIntent.setAction(ACTION_TOAST);
        PendingIntent startActivityPendingIntent = PendingIntent.getBroadcast(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widgetListView, startActivityPendingIntent);
        */

        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setAction(ACTION_WIDGET_CLICK);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent clickPI = PendingIntent
                .getActivity(context, appWidgetId,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setPendingIntentTemplate(R.id.widgetListView, clickPI);


        return rv;
    }

}
