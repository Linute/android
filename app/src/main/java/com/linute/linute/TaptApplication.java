package com.linute.linute;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.linute.linute.API.API_Methods;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by QiFeng on 8/11/16.
 */
public class TaptApplication extends MultiDexApplication {

    private static final String FILE_NAME = "tapt_data.realm";
    private static final long SCHEMA_VERSION = 1;

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        if(API_Methods.IS_DEV_BUILD){
            boolean isLive = sharedPreferences.getBoolean("is_live", true);
            API_Methods.HOST = isLive ? API_Methods.HOST_LIVE : API_Methods.HOST_DEV;
            API_Methods.VERSION = isLive ? API_Methods.VERSION_LIVE : API_Methods.VERSION_DEV;
        }else{
            API_Methods.HOST = API_Methods.HOST_LIVE;
            API_Methods.VERSION = API_Methods.VERSION_LIVE;
        }

        SingleVideoPlaybackManager.initManager(this);

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().name(FILE_NAME)
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
