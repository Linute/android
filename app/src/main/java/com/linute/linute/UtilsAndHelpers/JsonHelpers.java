package com.linute.linute.UtilsAndHelpers;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 9/17/16.
 */
public class JsonHelpers {

    public static boolean getBoolean(JSONObject o, String key){
        try {
            return o.getBoolean(key);
        }catch (JSONException e){
            return false;
        }
    }


}
