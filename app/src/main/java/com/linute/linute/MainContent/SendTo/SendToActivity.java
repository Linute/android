package com.linute.linute.MainContent.SendTo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by mikhail on 11/11/16.
 */

public class SendToActivity extends BaseTaptActivity{


    public static final String EXTRA_POST_ID = "post_id";
    public static final String FRAG_BACKSTACK_NAME = "sendTo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String postId = null;

        Intent intent = getIntent();
        if(intent == null){
            finish();
        }else{
            postId = intent.getStringExtra(EXTRA_POST_ID);
        }

        if(!TaptSocket.getInstance().socketConnected()){
            TaptSocket.getInstance().connectSocket();
        }

        final FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(android.R.id.content, SendToFragment.newInstance(postId)).addToBackStack(FRAG_BACKSTACK_NAME).commit();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(fm.getBackStackEntryCount() == 0 || !FRAG_BACKSTACK_NAME.equals(fm.getBackStackEntryAt(0).getName())) {
                    finish();
                    fm.removeOnBackStackChangedListener(this);
                }
            }
        });
    }

    @Override
    public void addFragmentToContainer(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).addToBackStack(null).commit();

    }

    @Override
    public void addFragmentToContainer(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, tag).addToBackStack(null).commit();

    }

    @Override
    public void replaceContainerWithFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public void addFragmentOnTop(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).addToBackStack(null).commit();
    }

    @Override
    public void setSocketErrorResponse(SocketErrorResponse error) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaptSocket.getInstance().disconnectSocket();
    }
}
