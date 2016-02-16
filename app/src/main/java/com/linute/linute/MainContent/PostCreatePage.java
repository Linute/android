package com.linute.linute.MainContent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class PostCreatePage extends AppCompatActivity {
    private static final String TAG = PostCreatePage.class.getSimpleName();

    private EditText textContent;
    private Switch anonymousSwitch;

    private View mPostButton;
    private View mProgressbar;

    private boolean mPostInProgress = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_content_page);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.postContentToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Post Status");
        setSupportActionBar(toolbar);

        mPostButton = toolbar.findViewById(R.id.create_page_post_button);
        mProgressbar = toolbar.findViewById(R.id.create_page_progress_bar);

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postStatus();
            }
        });


        findViewById(R.id.postContent_edit_text_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContent.requestFocusFromTouch();

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textContent, 0);
            }
        });

        textContent = (EditText) findViewById(R.id.postContentPageEditText);
        textContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(240)}); //set char limit
        textContent.addTextChangedListener(new TextWatcher() {
            TextView textView = (TextView) PostCreatePage.this.findViewById(R.id.postContent_character_counter);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textView.setText(240 - s.length() + "");
            }
        });


        anonymousSwitch = (Switch) findViewById(R.id.anonymous);
    }


    private void postStatus(){
        if (mPostInProgress) return;

        //SHIT !!
        if(textContent.getText().toString().trim().equals("")) return;

        mPostInProgress = true;

        mProgressbar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);
        Map<String, Object> postData = new HashMap<>();

        JSONArray coord = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            coord.put(0);
            coord.put(0);
            jsonObject.put("coordinates", coord);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//                Log.d(TAG, "" + jsonObject.toString());

        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        postData.put("college", sharedPreferences.getString("collegeId", ""));
        postData.put("privacy", (anonymousSwitch.isChecked() ? 1 : 0) + "");
        postData.put("title", textContent.getText().toString());
        JSONArray emptyArray = new JSONArray();
        postData.put("images", emptyArray);
        postData.put("type", "0");
        postData.put("geo", jsonObject);

        new LSDKEvents(this).postEvent(postData, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(PostCreatePage.this);
                        mPostButton.setVisibility(View.VISIBLE);
                        mProgressbar.setVisibility(View.GONE);
                        mPostInProgress = false;
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponseNotSuccessful" + response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(PostCreatePage.this);
                            mPostButton.setVisibility(View.VISIBLE);
                            mProgressbar.setVisibility(View.GONE);
                            mPostInProgress = false;
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPostInProgress = false;
                            Toast.makeText(PostCreatePage.this, "Posted status", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
