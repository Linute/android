package com.linute.linute.API;

import android.os.Build;

import com.linute.linute.MainContent.Uploading.CountingRequestBody;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONObject;

import java.io.IOException;
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
import okhttp3.Response;

/**
 * Created by QiFeng on 11/21/15.
 */

public class API_Methods {

    public static String TAG = "API_METHODS";


    //CHANGE ONLY THIS TO SWITCH TO PROD
    public static final boolean IS_DEV_BUILD = false;

    // API ENDPOINT URL
    public static final String SCHEME = "https";

    //    public static String HOST = "api.tapt.io";
    public static final String HOST_DEV = "devapi.tapt.io";
    public static final String HOST_LIVE = "api.tapt.io";

    public static final String VERSION_DEV = "v1.4.6";
    public static final String VERSION_LIVE = "v1.4.6";

    //set default to live, just in case
    public static String HOST = HOST_LIVE;
    public static String VERSION = VERSION_LIVE;


    //JSON TYPE
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");

    public static String USER_ID;


    public static String getURL() {
        return SCHEME + "://" + HOST + "/";
    }

    // API get method
    public static Call get(String[] path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        OkHttpClient client = new OkHttpClient();
        Headers requestHeaders = Headers.of(headers); //add headers
        HttpUrl.Builder url = new HttpUrl.Builder()   //build url
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(VERSION);

        if (path != null) {
            for (String p : path) {
                if (p != null) {
                    url.addPathSegment(p);
                }
            }
        }

        if (parameters != null) {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) { //add parameters
                url.addQueryParameter(parameter.getKey(), parameter.getValue().toString());
            }
        }

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

    //NOTE: this function is synchronous
    public static Response postWithProgress(String path,
                                            Map<String, String> headers,
                                            Map<String, Object> parameters,
                                            CountingRequestBody.Listener listener) throws IOException {

        JSONObject json = new JSONObject(parameters);

        OkHttpClient client = new OkHttpClient();
        Headers requestHeaders = Headers.of(headers);   //add headers
        String url = getURL(path);

        return client.newCall(
                new Request.Builder()
                        .url(url)
                        .headers(requestHeaders)
                        .post(new CountingRequestBody(json.toString(), listener))
                        .build()).execute();
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
    public static HashMap<String, String> getMainHeader(String token) {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", Utils.CONTENT_TYPE);
        header.put("authorization", "Basic " + Utils.encode_base64(token));
        header.put("User-Agent", "(" + Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL + "; Android " + Build.VERSION.RELEASE + ")/ Ver [" + VERSION + "] UserID [" + USER_ID + "]");
        DeviceInfoSingleton instance = null;
        try {
            instance = DeviceInfoSingleton.getInstance(null);
        }catch(Exception e){
            e.printStackTrace();
        }
        if(instance != null) {
            header.put("UUID", instance.getUdid());
        }else{
            header.put("UUID", "");
        }
        return header;
    }

    public static Call sendErrorReport(Throwable throwable, String token, Callback cb) {
        Map<String, Object> parms = new HashMap<>();
        StringBuilder trace = new StringBuilder();
        for (StackTraceElement s : throwable.getStackTrace()) {
            trace.append(s.toString()).append(" \n ");
        }
        parms.put("stackTrace", trace);
        parms.put("name", throwable.toString());
        parms.put("userAgent", Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL + "; Android " + Build.VERSION.RELEASE + ")/ Ver [" + VERSION + "] UserID [" + USER_ID + "]");
        parms.put("os", "android");
        return API_Methods.post("errors", getMainHeader(token), parms, cb);
    }

}
