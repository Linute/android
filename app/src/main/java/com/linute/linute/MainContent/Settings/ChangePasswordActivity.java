package com.linute.linute.MainContent.Settings;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangePasswordActivity extends BaseSocketActivity implements View.OnClickListener {

    private static final String TAG = ChangePasswordActivity.class.getSimpleName();
    private View vProgressbar;
    private EditText vPasswordText;
    private Button vButton;

    private String mEmail;
    private VerificationStatus mState = VerificationStatus.NeedVerificationCode;

    public enum VerificationStatus {
        NeedVerificationCode,
        VerificationSent,
        ChangingPassword
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        mEmail = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).getString("email", null);

        vPasswordText = (EditText) findViewById(R.id.edit_text);
        vPasswordText.setFocusable(false);
        vPasswordText.setFocusableInTouchMode(false);

        vProgressbar = findViewById(R.id.progress);

        vButton = (Button) findViewById(R.id.button);
        vButton.setOnClickListener(this);
        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePasswordActivity.this.finish();
            }
        });
    }


    private void showProgress(boolean show) {
        if (show) {
            vProgressbar.setVisibility(View.VISIBLE);
            vButton.setVisibility(View.INVISIBLE);
        } else {
            vProgressbar.setVisibility(View.INVISIBLE);
            vButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (mState) {
            case NeedVerificationCode:
                showProgress(true);
                sendPincode();
                return;
            case VerificationSent:
                showProgress(true);
                checkPin();
                return;
            case ChangingPassword:
                showProgress(true);
                changePassword();
        }

    }


    private void sendPincode() {
        if (mEmail == null) return;
        new LSDKUser(this).resetPassword(mEmail, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Utils.showBadConnectionToast(ChangePasswordActivity.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pinEntry();
                                showProgress(false);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Utils.showServerErrorToast(ChangePasswordActivity.this);
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "onResponse: "+response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Utils.showServerErrorToast(ChangePasswordActivity.this);
                        }
                    });
                }
            }
        });
    }

    private void pinEntry(){
        vPasswordText.setFocusableInTouchMode(true);
        vPasswordText.setFocusable(true);
        mState = VerificationStatus.VerificationSent;
        vPasswordText.setFocusable(true);
        vPasswordText.setHint("Enter pin code");
        vPasswordText.setInputType(InputType.TYPE_CLASS_NUMBER);
        showProgress(false);
        vButton.setText("Check pin");
    }

    private void checkPin() {
        //// TODO: 10/25/16  // FIXME: 10/25/16
        String pincode = vPasswordText.getText().toString();
        if (pincode.equals(vPasswordText.getText().toString())){
            passwordEntry();
        }else {
            vPasswordText.setError("Incorrect pin code");
            showProgress(false);
        }
    }

    private void passwordEntry(){
        mState = VerificationStatus.ChangingPassword;
        vPasswordText.setHint("New password");
        vPasswordText.setError(null);
        vPasswordText.setText("");
        vButton.setText("Change password");
    }

    private boolean isValidPassword(String password){
        return password.length() >= 6 && !password.contains(" ");
    }

    private void changePassword() {
        String pass = vPasswordText.getText().toString();
        if (isValidPassword(pass)){
            SharedPreferences preferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
            String token = preferences.getString("userToken", null);
            String userId = preferences.getString("userID", null);
            if (token == null || userId == null) return;

            new LSDKUser(this).changePassword(token, userId, pass, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Utils.showBadConnectionToast(ChangePasswordActivity.this);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()){
                        response.body().close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChangePasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                                ChangePasswordActivity.this.finish();
                            }
                        });
                    }else {
                        Log.d(TAG, "onResponse: "+response.body().string());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Utils.showServerErrorToast(ChangePasswordActivity.this);
                            }
                        });
                    }
                }
            });
        }else {
            vPasswordText.setError("Passwords must be at least 6 characters");
            vPasswordText.requestFocus();
        }
    }
}
