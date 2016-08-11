package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 8/9/16.
 */
public abstract class BaseSignUpFragment extends Fragment implements TextWatcher, TextView.OnEditorActionListener {

    private boolean mButtonActive = false;

    public abstract boolean activateButton();
    public abstract Button getButton();
    public abstract String getButtonText(boolean buttonActive);
    public abstract void onDonePressed();

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE){
            onDonePressed();
        }
        return false;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtonActive = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (activateButton()){
            if (mButtonActive) return;

            mButtonActive = true;

            Button button = getButton();
            button.setBackgroundResource(R.drawable.active_button);
            button.setTextColor(ContextCompat.getColor(button.getContext(), R.color.pure_white));
            button.setText(getButtonText(true));
        }else {
            if (!mButtonActive) return;

            mButtonActive = false;

            Button button = getButton();
            button.setBackgroundResource(R.drawable.inactive_button);
            button.setTextColor(ContextCompat.getColor(button.getContext(), R.color.secondaryColor));
            button.setText(getButtonText(false));
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() == null) return;

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
