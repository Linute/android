package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.LoginAndSignup.College;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 7/29/16.
 */
public class SignUpEmailFragment extends BaseSignUpFragment {

    public static final String TAG = SignUpEmailFragment.class.getSimpleName();
    public static final String COLLEGE_KEY = "email_frag_college_key";

    private EditText vEmail;
    private EditText vPassword;
    private Button vButton;
    private View vProgress;

    private College mCollege;

    private SignUpInfo mSignUpInfo;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.edu", Pattern.CASE_INSENSITIVE);

    public static SignUpEmailFragment newInstance(College college) {
        SignUpEmailFragment fragment = new SignUpEmailFragment();
        Bundle args = new Bundle();
        args.putParcelable(COLLEGE_KEY, college);
        fragment.setArguments(args);
        return fragment;
    }


    public SignUpEmailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mCollege = getArguments().getParcelable(COLLEGE_KEY);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_email, container, false);
        View vSuggestionsText = root.findViewById(R.id.suggestions);

        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();

        vEmail = (EditText) root.findViewById(R.id.email);
        vPassword = (EditText) root.findViewById(R.id.password);


        vEmail.addTextChangedListener(this);
        vEmail.setOnEditorActionListener(this);

        if (mSignUpInfo instanceof FBSignUpInfo) {
            vPassword.setVisibility(View.GONE);
            vEmail.setImeOptions(EditorInfo.IME_ACTION_DONE);
        } else {
            vPassword.setVisibility(View.VISIBLE);
            vPassword.setOnEditorActionListener(this);
            vPassword.addTextChangedListener(this);
            vEmail.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }

        if (mCollege.getIncludedEmails().isEmpty()) {
            vSuggestionsText.setVisibility(View.GONE);
        } else {
            vSuggestionsText.setVisibility(View.VISIBLE);
            vSuggestionsText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEmailOptions();
                }
            });
            vEmail.setHint(mCollege.getIncludedEmails().get(0));
        }

        vButton = (Button) root.findViewById(R.id.button);
        vButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vEmail.setError(null);
                vPassword.setError(null);
                String email = vEmail.getText().toString().trim().toLowerCase();

                if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
                    vEmail.setError("Invalid edu email");
                    vEmail.requestFocus();
                    return;
                }


                for (String suf : mCollege.getExcludedEmails()) {
                    if (email.endsWith(suf.toLowerCase())) {
                        vEmail.setError("This email is blocked");
                        vEmail.requestFocus();
                        return;
                    }
                }

                String password;

                if (mSignUpInfo instanceof FBSignUpInfo) {
                    password = ((FBSignUpInfo) mSignUpInfo).getSocialFB();
                } else {
                    password = vPassword.getText().toString();
                    if (!isValidPassword(password)) {
                        vPassword.setError("Passwords must be at least 6 characters");
                        vPassword.requestFocus();
                        return;
                    }
                }

                showConfirm(email, password);

            }
        });

        vProgress = root.findViewById(R.id.progress);

        return root;
    }


    @Override
    public boolean activateButton() {
        boolean password = vPassword.getVisibility() != View.VISIBLE || isValidPassword(vPassword.getText().toString());
        return !vEmail.getText().toString().trim().isEmpty() && password;
    }

    @Override
    public Button getButton() {
        return vButton;
    }

    @Override
    public String getButtonText(boolean buttonActive) {
        return buttonActive ? "Next" : "3 of 4";
    }

    @Override
    public void onDonePressed() {
        vButton.callOnClick();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vEmail.setText(mSignUpInfo.getEmail());
    }

    private boolean isValidPassword(String pass) {
        return pass.length() >= 6 && !pass.contains(" ");
    }


    private void showEmailOptions() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Suggested Emails")
                .setItems(mCollege.getIncludedEmails().toArray(new String[mCollege.getIncludedEmails().size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emailSelected(mCollege.getIncludedEmails().get(which));
                            }
                        })
                .show();
    }


    private void emailSelected(String email) {
        String text = vEmail.getText().toString().trim();

        if (text.contains("@"))
            text = text.split("@")[0];

        text += email;
        vEmail.setText(text);
    }


    private void showConfirm(final String email, final String password) {
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm")
                    .setMessage("A pin code will be sent to " + email + ". Is this the correct email address?")
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkEmail(email, password);
                            //goToNext(email, password);
                        }
                    }).show();
        }
    }

    public void checkEmail(final String email, final String password) {
        if (getActivity() != null) {

            showProgress(true);
            new LSDKUser(getActivity()).isUniqueEmail(email, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getContext());
                                showProgress(false);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) { //email was good
                        response.body().close();
                        mSignUpInfo.setEmail(email);
                        mSignUpInfo.setPassword(password);
                        getPinCode();
                    } else if (response.code() == 404) { //another error
                        response.body().close();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    vEmail.setError("Email has already been taken");
                                    vEmail.requestFocus();
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "onResponse: " + response.body().string());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Utils.showServerErrorToast(getContext());
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    public void getPinCode() {
        new LSDKUser(getActivity()).getConfirmationCodeForEmail(mSignUpInfo.getEmail(), mSignUpInfo.getFirstName(), mSignUpInfo.getLastName(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getContext());
                            showProgress(false);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String stringResp = response.body().string();
                        final String pin = (new JSONObject(stringResp).getString("pinCode"));
                        //Log.d(TAG, "onResponse: " + stringResp);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
                                if (frag != null) {
                                    frag.addFragment(SignUpPinFragment.newInstance(pin), SignUpPinFragment.TAG);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getContext());
                                    showProgress(false);
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getContext());
                                showProgress(false);
                            }
                        });
                    }
                }
            }
        });
    }


    private void showProgress(boolean show) {
        if (show) {
            vProgress.setVisibility(View.VISIBLE);
            vButton.setVisibility(View.INVISIBLE);
        } else {
            vButton.setVisibility(View.VISIBLE);
            vProgress.setVisibility(View.GONE);
        }
    }


//    //test code
//
//    private void goToNext(String email, String password) {
//        mSignUpInfo.setEmail(email);
//        mSignUpInfo.setPassword(password);
//
//        SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
//        if (frag != null) {
//            frag.addFragment(SignUpPinFragment.newInstance("1234"), SignUpPinFragment.TAG);
//        }
//    }

}
