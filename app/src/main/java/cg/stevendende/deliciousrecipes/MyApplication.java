package cg.stevendende.deliciousrecipes;

import android.app.Application;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;

//import info.androidhive.volleyexamples.volley.utils.LruBitmapCache;

public class MyApplication extends Application {
    private static MyApplication mInstance;
    private RequestQueue mRequestQueue;
    private Context mContext;

    public void onCreate() {
        super.onCreate();
        //Stetho.initializeWithDefaults(this);
    }

    public MyApplication() {
    }

    private MyApplication(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();

        Stetho.initializeWithDefaults(context);
    }

    public static synchronized MyApplication getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyApplication(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}