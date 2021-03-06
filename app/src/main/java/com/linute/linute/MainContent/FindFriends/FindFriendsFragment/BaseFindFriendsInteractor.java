package com.linute.linute.MainContent.FindFriends.FindFriendsFragment;

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
public abstract class BaseFindFriendsInteractor extends BaseInteractor<FriendSearchUser> {

    protected ArrayList<FriendSearchUser> mUnfilteredList;
    protected boolean mInitialListLoaded = false;
    protected String mQuery;

    protected abstract void search(Context context, Map<String, Object> params, boolean loadMore, final OnFinishedRequest<FriendSearchUser> onFinishedQuery);

    protected abstract ArrayList<FriendSearchUser> parseJson(JSONObject object) throws JSONException;
}
