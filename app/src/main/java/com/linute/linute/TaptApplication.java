package com.linute.linute;

import android.app.Application;

import com.linute.linute.API.API_Methods;
import com.linute.linute.SquareCamera.ScreenSizeSingleton;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by QiFeng on 8/11/16.
 */
public class TaptApplication extends Application {

    private static final String FILE_NAME = "tapt_data.realm";
    private static final long SCHEMA_VERSION = 1;


    @Override
    public void onCreate() {
        super.onCreate();

        if (API_Methods.DEV){
            API_Methods.HOST = API_Methods.HOST_DEV;
            API_Methods.VERSION = API_Methods.VERSION_DEV;
        }

        RealmConfiguration configuration = new RealmConfiguration.Builder(this).name(FILE_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .migration(new TaptMigration())
                .build();

        Realm.setDefaultConfiguration(configuration);
    }



    //when we add new Realm classes, we need to add it to current db
    //won't be called if file didn't exist

    //more info in migration section : https://realm.io/docs/java/latest/#configuring-a-realm
    private class TaptMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        }
    }
}
