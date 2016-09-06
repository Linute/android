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
    EnterButtonAction mEnterAction;

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
        super.onKeyPreIme(keyCode,event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // user pressed back
            mBackAction.backPressed();
        }
        if(keyCode == KeyEvent.KEYCODE_ENTER){
            if(mEnterAction != null){
                mEnterAction.enterPressed();
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER){
            mEnterAction.enterPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setBackAction(BackButtonAction action) {
        mBackAction = action;
    }
    public void setEnterAction(EnterButtonAction action){mEnterAction = action;}

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
        void backPressed();
    }
    public interface EnterButtonAction{
        void enterPressed();
    }
}