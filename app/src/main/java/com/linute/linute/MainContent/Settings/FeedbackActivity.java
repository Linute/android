package com.linute.linute.MainContent.Settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FeedbackActivity extends AppCompatActivity {

    private EditText mFeedBackET;
    private Button mDeactivateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle("Send Feedback");

        mDeactivateButton = (Button) findViewById(R.id.button_deactivate);
        mDeactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFeedBackET.setError(null);
                if (mFeedBackET.getText().toString().trim().isEmpty()) {
                    mFeedBackET.setError("Please give us some feedback");
                } else {
                    sendFeedback();
                }
            }
        });

        mFeedBackET = (EditText) findViewById(R.id.input_feedback);
    }


    public void sendFeedback() {
        String feedback = "User Deactivated Their Account \n" + mFeedBackET.getText();

        final LSDKUser lsdkUser = new LSDKUser(this);
        lsdkUser.sendFeedback(feedback, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showServerErrorToast(FeedbackActivity.this);
                        mFeedBackET.setEnabled(true);
                        mDeactivateButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //TODO hide progress bars, give an A-OK
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FeedbackActivity.this, "Thank you for the feedback. TaptHQ will take a look at it soon", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        });

        mFeedBackET.setEnabled(false);
        mDeactivateButton.setEnabled(false);
    }
}
