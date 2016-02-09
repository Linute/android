package com.linute.linute.API;


import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by QiFeng on 11/21/15.
 */
public class API_Methods {

    public static String TAG = "API_METHODS";

    // API ENPOINT URL
    private static String SCHEME = "https";
    //private static String HOST = "api.linute.com";
    private static String HOST = "devapi.linute.com";
    private static String VERSION = "v1.3";

    //JSON TYPE
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");


    // API get method
    // Throw exception?
    public static Call get(String[] path,
                           Map<String, String> headers,
                           Map<String, String> parameters,
                           Callback callback) {

        OkHttpClient client = new OkHttpClient();

        Headers requestHeaders = Headers.of(headers); //add headers


        HttpUrl.Builder url = new HttpUrl.Builder()   //build url
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(VERSION);

        for (String p : path) {
            url.addPathSegment(p);
        }

        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) { //add parameters
                url.addQueryParameter(parameter.getKey(), parameter.getValue());
            }
        }

        //Log.i(TAG, "get: "+url.toString());

        HttpUrl request = url.build();

        Call call = client.newCall(new Request.Builder().url(request).headers(requestHeaders).build());


        call.enqueue(callback);

        return call;
    }

    //API POST
    public static Call post(String path,
                            Map<String, String> headers,
                            Map<String, Object> parameters,
                            Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //get requestbody

        OkHttpClient client = new OkHttpClient();

        Headers requestHeaders = Headers.of(headers);   //add headers

        String url = getURL(path);

        Call call = client.newCall(new Request.Builder().url(url).headers(requestHeaders).method("POST", body).build());

        call.enqueue(callback);

        return call;
    }

    //API PUT
    public static Call put(String path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json

        OkHttpClient client = new OkHttpClient();

        Headers requestHeaders = Headers.of(headers); //add headers

        String url = getURL(path);

        Call call = client.newCall(new Request.Builder().url(url).method("PUT", body).headers(requestHeaders).build());

        call.enqueue(callback);

        return call;
    }


    //API DELETE
    public static Call delete(String path,
                              Map<String, String> headers,
                              Map<String, Object> params,
                              Callback callback) {

        OkHttpClient client = new OkHttpClient();

        Headers requestHeaders = Headers.of(headers); //add headers

        JSONObject json = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json

        String url = getURL(path);

        Call call = client.newCall(new Request.Builder().url(url).method("DELETE", body).headers(requestHeaders).build());
        call.enqueue(callback);

        return call;
    }

    public static String getURL(String path) {
        return SCHEME + "://" + HOST + "/" + VERSION + "/" + path + "/";
    }


    public static Map<String, String> getHeaderWithAuthUser(String email, String password, String authDeviceToken) {
        Map<String, String> header = getMainHeader(authDeviceToken);

        String encodedUserInfo = Utils.encode_base64(email + ":" +
                password);

        header.put("authorizationUser", "Basic " + encodedUserInfo);

        return header;
    }

    // returns main header containing: Content-Type and authorizationDevice
    public static Map<String, String> getMainHeader(String authDeviceToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", Utils.CONTENT_TYPE);
        header.put("authorizationDevice", "Basic " + authDeviceToken);
        return header;
    }

}
