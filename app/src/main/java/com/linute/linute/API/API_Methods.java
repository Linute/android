package com.linute.linute.API;


import com.linute.linute.UtilsAndHelpers.Utils;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * Created by QiFeng on 11/21/15.
 */

//<!--<string name="SOCKET_URL">https://api.tapt.io/</string>-->

public class API_Methods {

    public static String TAG = "API_METHODS";

    // API ENPOINT URL
    public static final String SCHEME = "https";
    private static String HOST = "api.tapt.io";
    //public static final String HOST = "devapi2.tapt.io";
    public static final String VERSION = "v1.3.7";

    //JSON TYPE
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");


    public static String getURL(){
        return SCHEME + "://" + HOST + "/";
    }

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

        //if (path == null) return null;


        if (path != null) {
            for (String p : path) {
                if (p != null) {
                    url.addPathSegment(p);
                }
            }
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


    // returns main header containing: Content-Type and authorizationDevice
    public static Map<String, String> getMainHeader(String token) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", Utils.CONTENT_TYPE);
        header.put("authorization", "Basic " + Utils.encode_base64(token));
        return header;
    }

}
