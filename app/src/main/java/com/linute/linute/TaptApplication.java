package com.linute.linute;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by QiFeng on 7/12/16.
 */
public class TaptApplication extends Application {

    public static RefWatcher getRefWatcher(Context context){
        TaptApplication application = (TaptApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

    RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mRefWatcher = LeakCanary.install(this);
    }
}
