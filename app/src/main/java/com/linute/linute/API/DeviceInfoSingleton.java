package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

/**
 * Created by QiFeng on 2/24/16.
 */
public class DeviceInfoSingleton {

    private static DeviceInfoSingleton mDeviceInfoSingleton;

    private String mVersonName;
    private String mVersionCode;
    private String mDeviceToken;
    private String mOS;
    private String mType;
    private String mUdid;
    private String mModel;

    public static DeviceInfoSingleton getInstance(Context context){
        if (mDeviceInfoSingleton == null){
            mDeviceInfoSingleton = new DeviceInfoSingleton(context);
        }
        return mDeviceInfoSingleton;
    }

    private DeviceInfoSingleton(Context mContext){

        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            mVersonName = pInfo.versionName;
            mVersionCode = pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mVersonName = "";
            mVersionCode = "";
        }

        mDeviceToken = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString(QuickstartPreferences.OUR_TOKEN, "");
        mOS = Build.VERSION.SDK_INT+"";
        mModel = Build.MODEL;
        mType = "android";

        mUdid = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getVersonName() {
        return mVersonName;
    }

    public String getVersionCode() {
        return mVersionCode;
    }

    public String getDeviceToken() {
        return mDeviceToken;
    }

    public String getOS() {
        return mOS;
    }

    public String getType() {
        return mType;
    }

    public String getUdid(){
        return mUdid;
    }

    public String getModel(){
        return mModel;
    }
}
