package com.linute.linute.MainContent.FindFriends;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.BaseInteractor;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.OnFinishedRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by QiFeng on 9/27/16.
 */
public abstract class BaseFindFriendsInteratctor extends BaseInteractor {

    protected ArrayList<FriendSearchUser> mUnfilteredList;
    protected boolean mInitialListLoaded = false;
    protected String mQuery;

    protected abstract void search(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery);

    protected abstract ArrayList<FriendSearchUser> parseJson(JSONObject object) throws JSONException;
}
