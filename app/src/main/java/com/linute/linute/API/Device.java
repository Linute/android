package com.linute.linute.API;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import java.util.Map;

/**
 * Created by QiFeng on 11/25/15.
 */
public class Device {

    public Device(){

    }

    public static Call createDevice( Map<String,String> header,
                                     Map<String,String> device,
                                     Callback callback)
    {
        return API_Methods.post("devices", header, device, callback);
    }

}
