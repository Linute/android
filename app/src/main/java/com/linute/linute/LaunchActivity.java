package com.linute.linute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.API.LSDKAnalytics;
import com.linute.linute.API.QuickstartPreferences;
import com.linute.linute.API.RegistrationIntentService;
import com.linute.linute.LoginAndSignup.CollegePickerActivity;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.io.IOException;
import java.util.Random;

import io.fabric.sdk.android.Fabric;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/*
**
** This Activity Checks to see which Activity to launch
**
*/


public class LaunchActivity extends Activity {

    private static final String TAG = "LaunchActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;


    public static final String EXTRA_REPORT_NOTIF_OPENED = "report opened";
    public static final String EXTRA_NOTIF_ID = "notifid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!API_Methods.IS_DEV_BUILD) {
            Fabric.with(this, new Crashlytics());
        }

        Intent intent = getIntent();
        if(intent != null){
            boolean reportOpened = intent.getBooleanExtra(EXTRA_REPORT_NOTIF_OPENED, false);
            if(reportOpened){
                String notifId = intent.getStringExtra(EXTRA_NOTIF_ID);
                new LSDKAnalytics(this).postOpenedNotification(notifId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
            }
        }

        //Initialize sending crash reports to backend. This comes up in the #android-crash channel
        final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                String token = getApplicationContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken", "");
                throwable.printStackTrace();
                API_Methods.sendErrorReport(throwable, token, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, response.body().string());
                    }
                });

                if (oldHandler != null)
                    oldHandler.uncaughtException(
                            thread,
                            throwable
                    ); //Delegates to Android's error handling
                else
                    System.exit(2); //Prevents the service/app from freezing
            }
        });

        AppsFlyerLib.getInstance().setDebugLog(false);
        AppsFlyerLib.getInstance().startTracking(this.getApplication(), "VPnL9y82TinTofd5XRZ6TJ");

        generateNewSigniture();

        //set broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
                String token = sharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null);
////                    requestServices();
                if (token != null) {
                    goToNextActivity();
                }
//                //No token and unsuccessful registration
                else {
                    new AlertDialog.Builder(LaunchActivity.this)
                            .setTitle("Problem With Connection")
                            .setMessage("Make sure you are connected to a network.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {  //retry to connect
                                    runRegistrationIntentService();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        };

        DeviceInfoSingleton.getInstance(this);
    }

    //signiture for profile image
    private void generateNewSigniture() {
        if (Utils.isNetworkAvailable(this)) {
            getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("imageSigniture", "" + new Random().nextInt())
                    .apply();
        }
    }

    //register device
    private void runRegistrationIntentService() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

        //register gcm token
        if (checkPlayServices()) {
            runRegistrationIntentService();
        }

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


    private void goToNextActivity() {

        Class nextActivity; //the next activity we go to

        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        //if user is logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false) && sharedPreferences.getString("userToken", null) != null) {
            //college set, go to college
            if (sharedPreferences.getString("collegeName", null) != null && sharedPreferences.getString("collegeId", null) != null) {

                API_Methods.USER_ID = sharedPreferences.getString("userID", null);

                nextActivity = MainActivity.class;

                //college was not set. go to college picker
            } else {
                nextActivity = CollegePickerActivity.class;
            }
        }

        //user not logged in
        else {
            nextActivity = PreLoginActivity.class;
        }

        Intent i = new Intent(LaunchActivity.this, nextActivity);
        startActivity(i);
        finish();
    }
}
