package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;

import java.util.ArrayList;

/**
 * Created by QiFeng on 9/24/16.
 */
public interface RequestCallbackView<T> {
    void onSuccess(ArrayList<T> list, boolean canLoadMore, boolean addToBack);
    void onError(String response);
    void onFailure();
}
