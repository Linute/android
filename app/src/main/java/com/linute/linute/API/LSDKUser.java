package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 11/22/15.
 */
public class LSDKUser {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mToken;

    public LSDKUser(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = mSharedPreferences.getString("userToken","");
    }


    //checks if email is unique
    //@param email - check if email is unique
    //@param callback - what to do if successful response
    public Call isUniqueEmail(String email,
                              Callback callback) {
        //create headers
        Map<String, String> header = new HashMap<>();
        //create post parameters
        Map<String, Object> postParam = new HashMap<>();
        postParam.put("email", email);

        return API_Methods.post("users/is-unique-email", header, postParam, callback);
    }


    //checks if phone is unique
    //@param phone - check if phone is unique
    //@param callback - what to do if successful response
    public Call isUniquePhone(String phone,
                              Callback callback) {
        //create headers
        Map<String, String> header = new HashMap<>();

        //create post parameters
        Map<String, Object> postParam = new HashMap<>();
        postParam.put("phone", phone);

        return API_Methods.post("users/is-unique-phone", header, postParam, callback);
    }

    //creates new account
    //@param userInfo - information to sign up with
    //@param callback - handles response
    public Call createUser(Map<String, Object> userInfo,
                           Callback callback) {

        Map<String, String> header = new HashMap<>();

        return API_Methods.post("users", header, userInfo, callback);
    }


    //login user to Linute
    //@param email - entered email
    //@param password - entered password
    //@param callback - handles reponse
    public Call loginUserWithEmail(String email,
                                   String password,
                                   Callback callback) {

        Map<String, String> header = new HashMap<>();

        Map<String, Object> postParam = new HashMap<>();
        postParam.put("email", email);
        postParam.put("password", password);

        return API_Methods.post("users/authorization-email", header, postParam, callback);
    }


    public Call getUserActivities(String userId, String status, String photo, Callback callback) {

        Map<String, String> params = new HashMap<>();
        params.put("action[0]", status);
        params.put("action[1]", photo);
        params.put("skip", "0");
        params.put("limit", "24");
        params.put("owner", userId);

        Map<String, String> header = API_Methods.getMainHeader(mToken);

        String[] path = {"activities"};

        return API_Methods.get(path, header, params, callback);
    }


    //Returns user Information
    //@param - userID: id of user
    //@param - what to do with result
    public Call getProfileInfo(String userID, Callback callback) {
        String[] url = {"users", userID};

        // /users/id {1235rewt5y52u}
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.get(url, header, null, callback);
    }

    //update information when loggin in using facebook
    //@param newUserInfo - new updated info
    //@param tempEmail - facebook email; e.g 1232313@facebook.com; or new email we want to change to
    //@param callback - handles responses
    public Call updateUserInfo(Map<String, Object> newUserInfo,
                               String tempEmail,
                               Callback callback) {

        Map<String, String> header = API_Methods.getMainHeader(mToken);;

        //get appropriate header
        //if there is a tempEmail (new email we want to change to) we use the temp email as the header

        return API_Methods.put("users/" + mSharedPreferences.getString("userID", ""),
                header,
                newUserInfo,
                callback);
    }

    public Call changePassword(String tempPass, String email, String userId, String newPass, Callback callback){
        Map<String, String> header = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        params.put("password", newPass);

        return API_Methods.put("users/"+userId, header, params, callback);
    }

    //get phone number confirmation code
    //@param phoneNumber - phone number; NOTE: make sure phone unique first
    //@param callback - what to do if success or failure
    public Call getConfirmationCodeForPhone(String phoneNumber,
                                            Callback callback){

        Map<String, String> header = new HashMap<>();

        Map<String, Object> param = new HashMap<>();
        param.put("phone", phoneNumber);

        return API_Methods.post("users/confirm-phone", header, param, callback);
    }



    public Call getConfirmationCodeForEmail(String email, String fName, String lName, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> param = new HashMap<>();
        param.put("email", email);
        param.put("firstName", fName);
        param.put("lastName", lName);
        return API_Methods.post("users/confirm-email", header, param, callback);
    }

    public Call authorizationFacebook(String fbToken, Callback callback ){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> param = new HashMap<>();
        param.put("token", fbToken);

        return API_Methods.post("users/authorization-facebook", header, param, callback);
    }


    public Call updateLocation(Map<String, Object> params, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        return API_Methods.post("geo", header, params, callback);
    }

    public Call resetPassword(String email, Callback callback){
        Map<String, String> header = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("email",email);

        return API_Methods.post("users/reset-password-email", header, params, callback);
    }
}
