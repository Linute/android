package com.linute.linute.UtilsAndHelpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;


/**
 * Created by QiFeng on 1/22/16.
 */

/*

    This will be a base class for all of our fragments
    in their on resume, we will check if they need updating.
    If they do, we update them.


 */
public class BaseFragment extends Fragment {

    private boolean mFragmentNeedsUpdating = true; //when initially created, they need to be updated

    public BaseFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("NEEDS_UPDATING", mFragmentNeedsUpdating);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mFragmentNeedsUpdating = savedInstanceState.getBoolean("NEEDS_UPDATING");
    }

    public void setFragmentNeedUpdating(boolean needsUpdating){
        mFragmentNeedsUpdating = needsUpdating;
    }

    public boolean fragmentNeedsUpdating(){
        return mFragmentNeedsUpdating;
    }
}
