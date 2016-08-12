package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.linute.linute.R;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpNameFragment extends BaseSignUpFragment implements View.OnClickListener {

    public static final String TAG = SignUpNameFragment.class.getSimpleName();

    private SignUpInfo mSignUpInfo;

    private EditText vFirstName;
    private EditText vLastName;

    private Button vButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_name, container, false);

        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();
        vFirstName = (EditText) root.findViewById(R.id.fname_text);
        vLastName = (EditText) root.findViewById(R.id.lname_text);

        vButton = (Button) root.findViewById(R.id.button);
        vButton.setOnClickListener(this);

        vLastName.addTextChangedListener(this);
        vFirstName.addTextChangedListener(this);

        vLastName.setOnEditorActionListener(this);

        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vFirstName.setText(mSignUpInfo.getFirstName());
        vLastName.setText(mSignUpInfo.getLastName());
    }

    @Override
    public void onClick(View v) {
        vFirstName.setError(null);
        vLastName.setError(null);

        boolean error = false;

        String first = vFirstName.getText().toString().trim();
        String last = vLastName.getText().toString().trim();
        if (first.isEmpty()) {
            vFirstName.setError("Required field");
            error = true;
        }

        if (last.isEmpty()) {
            vLastName.setError("Required field");
            error = true;
        }

        if (error) return;

        mSignUpInfo.setFirstName(first);
        mSignUpInfo.setLastName(last);

        ((SignUpParentFragment) getParentFragment()).addFragment(new SignUpCollegeFragment(), SignUpCollegeFragment.TAG);
    }


    @Override
    public boolean activateButton() {
        return !vFirstName.getText().toString().trim().isEmpty() && !vLastName.getText().toString().isEmpty();
    }

    @Override
    public Button getButton() {
        return vButton;
    }

    @Override
    public String getButtonText(boolean buttonActive) {
        return buttonActive ? "Next" : "1 of 4";
    }

    @Override
    public void onDonePressed() {
        onClick(vButton);
    }

}
