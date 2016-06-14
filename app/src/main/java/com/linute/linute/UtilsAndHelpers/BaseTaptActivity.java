package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 1/25/16.
 */
public abstract class BaseTaptActivity extends AppCompatActivity {

    /* MainActivity has tabs of Updatable fragments
     *  use this function to tell tab it needs to reload
     */
    public void setFragmentOfIndexNeedsUpdating(BaseFragment.FragmentState state, int index){
    }


    /*setting toolbar title*/
    //public abstract void setTitle(String title);

    /* fragment stuff */
    public abstract void addFragmentToContainer(final Fragment fragment);
    public abstract void addFragmentToContainer(final Fragment fragment, String tag);
    public abstract void replaceContainerWithFragment(final Fragment fragment);
    public abstract void addFragmentOnTop(Fragment fragment);


    /* action when toolbar is pressed */
    //public abstract void setToolbarOnClickListener(View.OnClickListener listener);

    /* socket stuff */
    public abstract void connectSocket(String event, Emitter.Listener emitter);
    public abstract void disconnectSocket(String event, Emitter.Listener emitter);
    public abstract void emitSocket(String event, Object arg);
    public abstract void setSocketErrorResponse(SocketErrorResponse error);

    public boolean socketConnected(){
        return false;
    }

    public interface SocketErrorResponse{
        void runSocketError();
    }
}

