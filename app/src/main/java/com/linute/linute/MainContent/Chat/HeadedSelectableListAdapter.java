package com.linute.linute.MainContent.Chat;

/**
 * Created by mikhail on 9/17/16.
 */
public abstract class HeadedSelectableListAdapter extends HeadedListAdapter{

    boolean[] isSelected;
    boolean[] isLocked;

    @Override
    public void onItemSelected(int position, int type, Object item) {

    }

    @Override
    public int getNumLists() {
        return 0;
    }
}
