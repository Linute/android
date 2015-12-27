package com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Arman on 12/27/15.
 */
abstract public class
        ChoiceCapableAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private final ChoiceMode mChoiceModes;

    public ChoiceCapableAdapter(ChoiceMode choiceMode) {
        super();
        mChoiceModes = choiceMode;
    }

    public void onChecked(int position, boolean isChecked) {
        mChoiceModes.setChecked(position, isChecked);
    }

    public boolean isChecked(int position) {
        return(mChoiceModes.isChecked(position));
    }

    public void onSaveInstanceState(Bundle state) {
        mChoiceModes.onSaveInstanceState(state);
    }

    public void onRestoreInstanceState(Bundle state) {
        mChoiceModes.onRestoreInstanceState(state);
    }


}
