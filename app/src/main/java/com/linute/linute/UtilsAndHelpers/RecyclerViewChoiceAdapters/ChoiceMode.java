package com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters;

import android.os.Bundle;

/**
 * Created by Arman on 12/27/15.
 */
public interface ChoiceMode {
    void setChecked(int position, boolean isChecked);

    boolean isChecked(int position);

    void onSaveInstanceState(Bundle state);

    void onRestoreInstanceState(Bundle state);
}
