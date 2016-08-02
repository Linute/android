package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.linute.linute.R;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpNameFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = SignUpNameFragment.class.getSimpleName();

    private SignUpInfo mSignUpInfo;

    private EditText vFirstName;
    private EditText vLastName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_name, container, false);

        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();
        vFirstName = (EditText) root.findViewById(R.id.fname_text);
        vLastName = (EditText) root.findViewById(R.id.lname_text);
        root.findViewById(R.id.button).setOnClickListener(this);

        vFirstName.setText(mSignUpInfo.getFirstName());
        vLastName.setText(mSignUpInfo.getLastName());

        return root;
    }


    @Override
    public void onClick(View v) {
        vFirstName.setError(null);
        vLastName.setError(null);

        boolean error = false;

        String first = vFirstName.getText().toString().trim();
        String last = vLastName.getText().toString().trim();
        if (first.isEmpty()){
            vFirstName.setError("Required field");
            error = true;
        }

        if (last.isEmpty()){
            vLastName.setError("Required field");
            error = true;
        }

        if (error) return;

        mSignUpInfo.setFirstName(first);
        mSignUpInfo.setLastName(last);

        ((SignUpParentFragment) getParentFragment()).addFragment(new SignUpCollegeFragment(), SignUpCollegeFragment.TAG);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() == null) return;

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
