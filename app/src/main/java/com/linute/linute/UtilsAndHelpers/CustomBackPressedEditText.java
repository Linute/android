package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Created by QiFeng on 2/28/16.
 */
public class CustomBackPressedEditText extends EditText {

    BackButtonAction mBackAction;

    public CustomBackPressedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomBackPressedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBackPressedEditText(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // User has pressed Back key. So hide the keyboard
            mBackAction.backPressed();
        }
        return true;
    }

    public void setBackAction(BackButtonAction action) {
        mBackAction = action;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    public interface BackButtonAction {
        public void backPressed();
    }
}