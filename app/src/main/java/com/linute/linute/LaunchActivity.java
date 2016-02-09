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

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.linute.linute.API.QuickstartPreferences;
import com.linute.linute.API.RegistrationIntentService;
import com.linute.linute.LoginAndSignup.CollegePickerActivity;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import io.fabric.sdk.android.Fabric;
import java.util.Random;


/*
**
** This Activity Checks to see which Activity to launch
**
*/


public class LaunchActivity extends Activity {

    private static final String TAG = "LaunchActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launch);

        generateNewSigniture();

//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //set broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                String token = sharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null);

                //token was sent or we already have token
                //we need a token for this app to work. Will stop app if there is no token available
                if (sentToken || token != null) {
//                    requestServices();
                    goToNextActivity();
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


//    public void requestServices() {
//        if (!Utils.isNetworkAvailable(this)) {
//            goToNextActivity();
//        }
//
//        else {
//            if (ContextCompat.checkSelfPermission(this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                requestLocation();
//            } else {
//                goToNextActivity();
//            }
//        }
//    }


//    private Handler mStopGettingLocationHandler;
//    private Runnable mStopGettingLocation = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                mLocationManager.removeUpdates(mLocationListener);
//            } catch (SecurityException e) {
//                e.printStackTrace();
//            }
//            Log.i(TAG, "run: ");
//            goToNextActivity();
//        }
//    };

//    private void requestLocation() throws SecurityException {
//
//        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
//
//        mStopGettingLocationHandler = new Handler();
//        //mStopGettingLocationHandler.postDelayed(mStopGettingLocation, 2000); //if taking longer than 1.5 seconds to get location, stop it
//    }

//    LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            Log.i(TAG, "onLocationChanged: " + location.toString());
//            mStopGettingLocationHandler.removeCallbacks(mStopGettingLocation);
//
//            SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
//
//            sharedPreferences.edit()
//                    .putString("geoLongitude", location.getLongitude() + "") //will be saved as String. Cant store double
//                    .putString("geoLatitude", location.getLatitude() + "")
//                    .apply();
//
//
//            if (sharedPreferences.getBoolean("isLoggedIn", false)) {
//                JSONArray coord = new JSONArray();
//                try {
//                    coord.put(location.getLatitude());
//                    coord.put(location.getLongitude());
//
//                    JSONObject coordinates = new JSONObject();
//                    coordinates.put("geo", coord);
//
//                    Map<String, Object> params = new HashMap<>();
//                    params.put("coordinates", coordinates);
//
//                    new LSDKUser(LaunchActivity.this).updateLocation(params, new Callback() {
//                        @Override
//                        public void onFailure(Request request, IOException e) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    goToNextActivity();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onResponse(Response response) throws IOException {
//                            Log.i(TAG, "onResponse: " + response.code() + response.body().string());
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    goToNextActivity();
//                                }
//                            });
//                        }
//                    });
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    goToNextActivity();
//                }
//            } else {
//                goToNextActivity();
//            }
//
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            mStopGettingLocationHandler.removeCallbacks(mStopGettingLocation);
//            goToNextActivity();
//        }
//    };

    private void goToNextActivity() {

        Class nextActivity; //the next activity we go to

        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        //if user is logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false) && !sharedPreferences.getString("email", "noAuth").equals("noAuth")) {
            //college set, go to college
            if (sharedPreferences.getString("collegeName", null) != null && sharedPreferences.getString("collegeId", null) != null)
                nextActivity = MainActivity.class;

                //college was not set. go to college picker
            else
                nextActivity = CollegePickerActivity.class;
        }

        //user not logged in
        else {
            nextActivity = PreLoginActivity.class;
        }

        Intent i = new Intent(LaunchActivity.this, nextActivity);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); //no transition effects FIXME
        LaunchActivity.this.finish();
    }
}
