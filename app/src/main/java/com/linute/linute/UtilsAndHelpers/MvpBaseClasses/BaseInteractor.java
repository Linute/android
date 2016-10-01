package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;

import android.content.Context;

import java.util.Map;

import okhttp3.Call;

/**
 * Created by QiFeng on 9/27/16.
 */
public abstract class BaseInteractor {

    protected Call mCall;

    public abstract void query(Context context, Map<String, Object> params, OnFinishedRequest onFinishedQuery);

    public void cancelRequest() {
        if (mCall != null) mCall.cancel();
    }
}
