package com.linute.linute.API;


import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 11/25/15.
 */
public class Device {

    public Device(){

    }

    public static Call createDevice(Map<String,String> header,
                                    Map<String,Object> device,
                                    Callback callback)
    {
        return API_Methods.post("devices", header, device, callback);
    }


}
