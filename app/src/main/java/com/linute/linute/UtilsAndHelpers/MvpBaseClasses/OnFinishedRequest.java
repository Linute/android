package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;


import java.util.ArrayList;

/**
 * Created by QiFeng on 9/24/16.
 */
public interface OnFinishedRequest<T> {

    void onSuccess(ArrayList<T> users, boolean canLoadMore, boolean addToBack);
    void onFailure();
    void onError(String error);
}
