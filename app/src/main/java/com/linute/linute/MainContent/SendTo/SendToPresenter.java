package com.linute.linute.MainContent.SendTo;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.BaseRequestPresenter;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.RequestCallbackView;

import java.util.Map;

/**
 * Created by QiFeng on 10/20/16.
 */
public class SendToPresenter extends BaseRequestPresenter<SendToItem> {

    private SendToInteractor mSendToInteractor;

    public SendToPresenter(RequestCallbackView<SendToItem> callbackView) {
        super(callbackView);
        mSendToInteractor = new SendToInteractor();
    }

    @Override
    public void request(Context context, Map<String, Object> params, boolean loadMore) {
        mSendToInteractor.query(context, params, loadMore, this);
    }

    @Override
    public void cancelRequest() {
        mSendToInteractor.cancelRequest();
    }
}
