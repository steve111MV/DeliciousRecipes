package cg.stevendende.deliciousrecipes.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by STEVEN on 10/08/2017.
 */

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}