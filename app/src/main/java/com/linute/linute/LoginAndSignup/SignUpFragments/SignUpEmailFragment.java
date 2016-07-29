package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
class SignUpEmailFragment extends Fragment {

    public static final String TAG = SignUpEmailFragment.class.getSimpleName();
    public static final String COLLEGE_KEY = "email_frag_college_key";

    private EditText vEditText;
    private View vButton;
    private View vProgress;

    private College mCollege;

    private SignUpInfo mSignUpInfo;
    private SignUpEmailAdapter mSignUpEmailAdapter;

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

        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_email, container, false);
        View vSuggestionsText = root.findViewById(R.id.suggest);
        RecyclerView vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        vEditText = (EditText) root.findViewById(R.id.email);
        vEditText.setText(mSignUpInfo.getEmail());

        if (mSignUpEmailAdapter == null) {
            mSignUpEmailAdapter = new SignUpEmailAdapter(mCollege.getIncludedEmails(), new SignUpEmailAdapter.EmailSelected() {
                @Override
                public void onEmailSelected(String email) {
                    String text = vEditText.getText().toString().trim();
                    if (text.contains("@")) {
                        text = text.split("@")[0];
                    }
                    text += email;
                    vEditText.setText(text);
                }
            });
        }

        vRecyclerView.setAdapter(mSignUpEmailAdapter);

        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && vEditText.hasFocus())
                    hideKeyboard();
            }
        });

        if (mCollege.getIncludedEmails().isEmpty()) {
            vRecyclerView.setVisibility(View.GONE);
            vSuggestionsText.setVisibility(View.GONE);
        } else {
            vRecyclerView.setVisibility(View.VISIBLE);
            vSuggestionsText.setVisibility(View.VISIBLE);
        }

        vButton = root.findViewById(R.id.button);
        vButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vEditText.setError(null);
                String email = vEditText.getText().toString().trim().toLowerCase();

                if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
                    vEditText.setError("Invalid edu email");
                    return;
                }


                for (String suf : mCollege.getExcludedEmails()) {
                    if (email.endsWith(suf)) {
                        vEditText.setError("This email is blocked");
                        return;
                    }
                }

                showConfirm(email);

            }
        });

        vProgress = root.findViewById(R.id.progress);

        return root;
    }


    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();
    }


    private void showConfirm(final String email){
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm")
                    .setMessage("A pin code will be sent to "+email+". Is this the correct email address?")
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkEmail(email);
                        }
                    }).show();
        }
    }

    public void checkEmail(final String email) {
        if (getActivity() != null) {
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
                        getPinCode();
                    } else if (response.code() == 404) { //another error
                        response.body().close();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    vEditText.setError("Email has already been taken");
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
                        //Log.i(TAG, "onResponse: " + stringResp);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
                                if (frag != null){
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

    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vEditText.getWindowToken(), 0);
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
}
