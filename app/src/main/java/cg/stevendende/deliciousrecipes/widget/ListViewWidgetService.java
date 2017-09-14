package cg.stevendende.deliciousrecipes.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by STEVEN on 10/08/2017.
 */

public class ListViewWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        //Log.i("BALog_service", "action: "+intent.getAction());
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}