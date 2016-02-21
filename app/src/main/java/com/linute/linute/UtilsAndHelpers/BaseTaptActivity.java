package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 1/25/16.
 */
public class BaseTaptActivity extends AppCompatActivity {


    public void resetToolbar() {

    }


    public void setTitle(String title) {

    }

    public void addFragmentToContainer(final Fragment fragment) {

    }


    public void replaceContainerWithFragment(final Fragment fragment) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void raiseAppBarLayoutElevation() {
        //NOTE MAKE SURE YOU USE THIS IF CLAUSE
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void lowerAppBarElevation() {
        //NOTE MAKE SURE YOU USE THIS IF CLAUSE
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    }


    public void enableBarScrolling(boolean enabled){

    }

    public void setFragmentOfIndexNeedsUpdating(boolean needsUpdating, int index){

    }

    public void setToolbarOnClickListener(View.OnClickListener listener){

    }

    public void connectSocket(String event, Emitter.Listener emitter){

    }

    public void disconnectSocket(String event, Emitter.Listener emitter){

    }

    public void emitSocket(String event, Object arg){

    }


    public void setSocketErrorResponse(SocketErrorResponse error){

    }


    public interface SocketErrorResponse{
        void runSocketError();
    }
}

