package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by QiFeng on 6/11/16.
 */
public class NoSuggestionsTextView extends TextView {
    public NoSuggestionsTextView(Context context) {
        super(context);
    }

    public NoSuggestionsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSuggestionsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }
}
