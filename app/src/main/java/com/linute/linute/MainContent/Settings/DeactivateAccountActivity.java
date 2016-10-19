package com.linute.linute.MainContent.Settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.Database.TaptUser;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeactivateAccountActivity extends BaseSocketActivity {

    private EditText mFeedBackET;
    private Button deactivateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivate_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle("Deactivate Account");

        deactivateButton = (Button) findViewById(R.id.button_deactivate);
        deactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFeedBackET.setError(null);
                if (mFeedBackET.getText().toString().trim().isEmpty()) {
                    mFeedBackET.setError("Please give us some feedback");
                } else {
                    new AlertDialog.Builder(DeactivateAccountActivity.this)
                            .setMessage("Are you sure?")
                            .setPositiveButton("Deactivate", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sendFeedBackAndDeactivate();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        });


        mFeedBackET = (EditText) findViewById(R.id.input_feedback);


    }


    public void sendFeedBackAndDeactivate() {
        String feedback = "User Deactivated Their Account \n" + mFeedBackET.getText();

        final LSDKUser lsdkUser = new LSDKUser(this);
        lsdkUser.sendFeedback(feedback, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                deactivateAccount(DeactivateAccountActivity.this, lsdkUser);
            }
        });
    }

    private void deactivateAccount(final Activity activity, LSDKUser lsdkUser) {
        lsdkUser.deactivateAccount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:logout", new JSONObject());
                Utils.resetUserInformation(activity
                        .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE));
                Utils.deleteTempSharedPreference(activity
                        .getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE));

                if (AccessToken.getCurrentAccessToken() != null) //log out facebook if logged in
                    LoginManager.getInstance().logOut();

                TaptSocket.getInstance().forceDisconnect();
                TaptSocket.clear();

                Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.delete(TaptUser.class);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(activity, PreLoginActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //don't let them come back
                                startActivity(i);
                                activity.finish();
                                Toast.makeText(activity, "Your account has been deactivated", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

            }
        });
    }

}
