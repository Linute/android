package com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters;

import android.os.Bundle;

/**
 * Created by Arman on 12/27/15.
 */
public class MultiChoiceMode implements ChoiceMode {
    private static final String STATE_CHECK_STATES = "checkStates";
    private ParcelableSparseBooleanArray checkStates = new ParcelableSparseBooleanArray();

    @Override
    public void setChecked(int position, boolean isChecked) {
        checkStates.put(position, isChecked);
    }

    @Override
    public boolean isChecked(int position) {
        return checkStates.get(position, false);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(STATE_CHECK_STATES, checkStates);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        checkStates = state.getParcelable(STATE_CHECK_STATES);
    }
}
