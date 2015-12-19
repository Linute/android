package com.linute.linute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.API.QuickstartPreferences;
import com.linute.linute.API.RegistrationIntentService;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/*
**
** This Activity Checks to see which Activity to launch
**
*/


public class LaunchActivity extends Activity {

    private static final String TAG = "LaunchActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //set broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //SharedPreferences sharedPreferences = new SecurePreferences(getApplicationContext(), "BtE3eRHzZq", LinuteConstants.SharedPrefName);

                SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                String token = sharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null);

                //token was sent or we already have token
                //we need a token for this app to work. Will stop app if there is no token available
                if (sentToken || token != null) {

                    Class nextActivity; //the next activity we go to

                    //if user is logged in
                    if (sharedPreferences.getBoolean("isLoggedIn", false) && sharedPreferences.getString("email", "noAuth") != "noAuth") {
                        //if email has been confirmed
                        nextActivity = MainActivity.class; // go to MainActivty
                        //nextActivity = PreLoginActivity.class;
                    }

                    //user not logged in
                    else {
                        nextActivity = PreLoginActivity.class;
                    }

                    Intent i = new Intent(LaunchActivity.this, nextActivity);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    LaunchActivity.this.finish();

                }
                //No token and unsuccessful registration
                else {
                    new AlertDialog.Builder(LaunchActivity.this)
                            .setTitle("Problem With Connection")
                            .setMessage("Make sure you are connected to a network.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {  //retry to connect
                                    runRegistrationIntentService();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() { //exit
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        };
    }

    //register device
    private void runRegistrationIntentService() {
        Log.v(TAG, "Service Verified");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

        //register gcm token
        //FIXME: take out comment when running on actual device
        //if (checkPlayServices()){
        runRegistrationIntentService();
        //}
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
