package com.linute.linute;

import android.app.Application;
import android.content.Intent;

import com.linute.linute.MainContent.Chat.ChatService;

/**
 * Created by Arman on 1/18/16.
 */
public class LinuteApplication extends Application {

    private static final String TAG = LinuteApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
//        startService(new Intent(this, ChatService.class));
    }
}
