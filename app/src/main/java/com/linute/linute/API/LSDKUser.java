package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;


import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 11/22/15.
 */
public class LSDKUser {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;

    public LSDKUser(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }


    //checks if email is unique
    //@param email - check if email is unique
    //@param callback - what to do if successful response
    public Call isUniqueEmail(String email,
                              Callback callback) {
        //create headers
        Map<String, String> header = API_Methods.getMainHeader(mEncodedToken);
        //create post parameters
        Map<String, String> postParam = new HashMap<>();
        postParam.put("email", email);

        return API_Methods.post("users/is-unique-email", header, postParam, callback);
    }


    //checks if phone is unique
    //@param phone - check if phone is unique
    //@param callback - what to do if successful response
    public Call isUniquePhone(String phone,
                              Callback callback) {
        //create headers
        Map<String, String> header = API_Methods.getMainHeader(mEncodedToken);

        //create post parameters
        Map<String, String> postParam = new HashMap<>();
        postParam.put("phone", phone);

        return API_Methods.post("users/is-unique-phone", header, postParam, callback);
    }

    //creates new account
    //@param userInfo - information to sign up with
    //@param callback - handles response
    public Call createUser(Map<String, String> userInfo,
                           Callback callback) {

        Map<String, String> header = API_Methods.getMainHeader(mEncodedToken);

        return API_Methods.post("users", header, userInfo, callback);
    }


    //login user to Linute
    //@param email - entered email
    //@param password - entered password
    //@param callback - handles reponse
    public Call loginUserWithEmail(String email,
                                   String password,
                                   Callback callback) {

        Map<String, String> header = API_Methods.getMainHeader(mEncodedToken);

        Map<String, String> postParam = new HashMap<>();
        postParam.put("email", email);
        postParam.put("password", password);

        return API_Methods.post("users/authorization-email", header, postParam, callback);
    }


    public Call getUserActivities(Callback callback) {

        Map<String, String> params = new HashMap<>();
        params.put("action[0]", "blasted");
        params.put("action[1]", "host");
        params.put("action[2]", "attend");
        params.put("skip", "0");
        params.put("limit", "25");
        params.put("owner", mSharedPreferences.getString("userID", null));

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"activities"};

        return API_Methods.get(path, header, params, callback);
    }


    //Returns user Information
    //@param - userID: id of user
    //@param - what to do with result
    public Call getProfileInfo(String userID, Callback callback) {
        String[] url = {"users", userID};

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.get(url, header, null, callback);
    }

    //update information when loggin in using facebook
    //@param newUserInfo - new updated info
    //@param tempEmail - facebook email; e.g 1232313@facebook.com; or new email we want to change to
    //@param callback - handles responses
    public Call updateUserInfo(Map<String, String> newUserInfo,
                               String tempEmail,
                               Callback callback) {

        Map<String, String> header;

        //get appropriate header
        //if there is a tempEmail (new email we want to change to) we use the temp email as the header
        header = API_Methods.getHeaderWithAuthUser(
                tempEmail == null ? mSharedPreferences.getString("email", "") : tempEmail,
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        return API_Methods.put("users/" + mSharedPreferences.getString("userID", ""),
                header,
                newUserInfo,
                callback);
    }



    //get phone number confirmation code
    //@param phoneNumber - phone number; NOTE: make sure phone unique first
    //@param callback - what to do if success or failure
    public Call getConfirmationCodeForPhone(String phoneNumber,
                                            Callback callback){

        Map<String, String> header = API_Methods.getMainHeader(mEncodedToken);

        Map<String, String> param = new HashMap<>();
        param.put("phone", phoneNumber);

        return API_Methods.post("/users/confirm-phone", header, param, callback);
    }

    /*TODO: Still needs to be implemented :
        authFacebook


     */

}
