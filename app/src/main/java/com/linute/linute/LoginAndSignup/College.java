package com.linute.linute.LoginAndSignup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/12/16.
 */
public class College {
    private String mCollegeName;
    private String mCollegeId;

    public College (JSONObject json){

        //TODO: FIX KEYS
        mCollegeName = getStringFromJson(json, "name");
        mCollegeId = getStringFromJson(json, "id");
    }

    public static String getStringFromJson(JSONObject json, String key) {
        try {
            return json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public String getCollegeId() {
        return mCollegeId;
    }
}
