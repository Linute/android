package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by QiFeng on 2/28/16.
 */
public class CustomBackPressedEditText extends EditText {

    BackButtonAction mBackAction;
    EnterButtonAction mEnterAction;

    public CustomBackPressedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUpEditorAction();
    }

    public CustomBackPressedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpEditorAction();
    }

    public CustomBackPressedEditText(Context context) {
        super(context);
        setUpEditorAction();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        super.onKeyPreIme(keyCode, event);
        if (mBackAction != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // user pressed back
                mBackAction.backPressed();
            }
        }
        return false;
    }


    private void setUpEditorAction() {
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mEnterAction != null) {
                    mEnterAction.enterPressed();
                    return true;
                }

                return false;
            }
        });
    }

    public void setBackAction(BackButtonAction action) {
        mBackAction = action;
    }

    public void setEnterAction(EnterButtonAction action) {
        mEnterAction = action;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
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

    public interface EnterButtonAction {
        void enterPressed();
    }
}