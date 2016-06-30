package com.linute.linute;

import android.app.Application;

import net.gotev.uploadservice.UploadService;

/**
 * Created by QiFeng on 6/25/16.
 */
public class TaptApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }
}
