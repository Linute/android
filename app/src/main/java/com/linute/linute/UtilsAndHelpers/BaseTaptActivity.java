package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

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

}
