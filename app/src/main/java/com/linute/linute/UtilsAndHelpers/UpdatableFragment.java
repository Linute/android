package com.linute.linute.UtilsAndHelpers;

import android.support.v4.app.Fragment;

import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;

/**
 * Created by QiFeng on 1/22/16.
 */

/*

    This will be a base class for all of our fragments
    in their on resume, we will check if they need updating.
    If they do, we update them.


 */
public class UpdatableFragment extends Fragment {

    private boolean mFragmentNeedsUpdating = true; //when initially created, they need to be updated

    public UpdatableFragment() {

    }

    public void setFragmentNeedUpdating(boolean needsUpdating){
        mFragmentNeedsUpdating = needsUpdating;
    }

    public boolean fragmentNeedsUpdating(){
        return mFragmentNeedsUpdating;
    }


}
