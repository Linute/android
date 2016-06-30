package com.linute.linute.API;

import android.util.Log;

import com.linute.linute.MainContent.Uploading.CountingRequestBody;
import com.linute.linute.MainContent.Uploading.ProgressRequestBody;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by QiFeng on 11/21/15.
 */

public class API_Methods {

    public static String TAG = "API_METHODS";

    // API ENDPOINT URL
    public static final String SCHEME = "https";

   // private static String HOST = "api.tapt.io";
    public static final String HOST = "devapi2.tapt.io";

    public static final String VERSION = "v1.4.1";

    //JSON TYPE
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");


    public static String getURL(){
        return SCHEME + "://" + HOST + "/";
    }

    // API get method
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
                                            CountingRequestBody.Listener listener) throws IOException{


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
        return header;
    }

}
