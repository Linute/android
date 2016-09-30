package com.linute.linute.UtilsAndHelpers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.linute.linute.Socket.TaptSocket;

/**
 * Created by QiFeng on 9/29/16.
 */
public class BaseSocketActivity extends AppCompatActivity {

    protected boolean mNoToken = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            TaptSocket.initSocketConnection(this);
            mNoToken = false;
        }catch (NullPointerException e){
            e.printStackTrace();
            mNoToken = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mNoToken && TaptSocket.getInstance() != null)
            TaptSocket.getInstance().connectSocket();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mNoToken && TaptSocket.getInstance() != null)
            TaptSocket.getInstance().disconnectSocket();
    }
}
