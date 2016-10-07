package com.linute.linute.MainContent.Settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivate_account);

        findViewById(R.id.button_deactivate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(DeactivateAccountActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("")
                        .setPositiveButton("Deactivate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deactivateAccount();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        });

    }


    public void deactivateAccount() {
        final Activity activity = this;

        new LSDKUser(this).deactivateAccount(new Callback() {
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
                                }
                            });
                        }
                });

            }
        });
    }

}
