package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by QiFeng on 7/30/16.
 */
public class NoSuggestionEditText extends EditText {
    public NoSuggestionEditText(Context context) {
        super(context);
    }

    public NoSuggestionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSuggestionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoSuggestionEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }
}
