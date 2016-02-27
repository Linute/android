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

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


public class PostCreatePage extends AppCompatActivity {
    private static final String TAG = PostCreatePage.class.getSimpleName();

    private EditText textContent;
    private Switch anonymousSwitch;

    private View mPostButton;
    private View mProgressbar;

    private boolean mPostInProgress = false;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_content_page);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

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
                postContent();
            }
        });


        findViewById(R.id.postContent_edit_text_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContent.requestFocusFromTouch();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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


//    private void postStatus() {
//        if (mPostInProgress) return;
//
//        if (textContent.getText().toString().trim().equals("")) return;
//
//        mPostInProgress = true;
//
//        mProgressbar.setVisibility(View.VISIBLE);
//        mPostButton.setVisibility(View.INVISIBLE);




//        Map<String, Object> postData = new HashMap<>();
//
//        JSONArray coord = new JSONArray();
//        JSONObject jsonObject = new JSONObject();
//        try {
//            coord.put(0);
//            coord.put(0);
//            jsonObject.put("coordinates", coord);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
////                Log.d(TAG, "" + jsonObject.toString());
//
//        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        postData.put("college", sharedPreferences.getString("collegeId", ""));
//        postData.put("privacy", (anonymousSwitch.isChecked() ? 1 : 0) + "");
//        postData.put("title", textContent.getText().toString());
//        JSONArray emptyArray = new JSONArray();
//        postData.put("images", emptyArray);
//        postData.put("type", "0");
//        postData.put("geo", jsonObject);
//
//        new LSDKEvents(this).postEvent(postData, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Utils.showBadConnectionToast(PostCreatePage.this);
//                        mPostButton.setVisibility(View.VISIBLE);
//                        mProgressbar.setVisibility(View.GONE);
//                        mPostInProgress = false;
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.d(TAG, "onResponseNotSuccessful" + response.body().string());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.showServerErrorToast(PostCreatePage.this);
//                            mPostButton.setVisibility(View.VISIBLE);
//                            mProgressbar.setVisibility(View.GONE);
//                            mPostInProgress = false;
//                        }
//                    });
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mPostInProgress = false;
//                            Toast.makeText(PostCreatePage.this, "Posted status", Toast.LENGTH_SHORT).show();
//                            setResult(RESULT_OK);
//                            finish();
//                        }
//                    });
//                }
//            }
//        });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private Socket mSocket;
    private boolean mConnecting = false;


    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;

            {
                try {
                    IO.Options op = new IO.Options();
                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(this);
                    op.query =
                            "token=" + mSharedPreferences.getString("userToken", "") +
                                    "&deviceToken="+device.getDeviceToken() +
                                    "&udid="+device.getUdid()+
                                    "&version="+device.getVersonName()+
                                    "&build="+device.getVersionCode()+
                                    "&os="+device.getOS()+
                                    "&type="+device.getType()
                    ;
                    op.reconnectionDelay = 5;
                    op.secure = true;


                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(getString(R.string.SOCKET_URL), op);/*R.string.DEV_SOCKET_URL*/
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on(Socket.EVENT_ERROR, eventError);
            mSocket.on("new post", newPost);
            mSocket.connect();
            mConnecting = false;

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID",""));
                obj.put("action", "active");
                obj.put("screen", "Create");
                mSocket.emit(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSocket != null) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID",""));
                obj.put("action", "inactive");
                obj.put("screen", "Create");
                mSocket.emit(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.off(Socket.EVENT_ERROR, eventError);
            mSocket.off("new post", newPost);

        }
    }

    private void postContent() {

        if (mPostInProgress) return;

        //SHIT !!
        if (textContent.getText().toString().trim().equals("")) return;

        if (!Utils.isNetworkAvailable(this) || !mSocket.connected()){
            Utils.showBadConnectionToast(this);
            return;
        }

        mPostInProgress = true;

        mProgressbar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);

        try {
            JSONObject postData = new JSONObject();
            postData.put("college", mSharedPreferences.getString("collegeId", ""));
            postData.put("privacy", (anonymousSwitch.isChecked() ? 1 : 0) + "");
            postData.put("title", textContent.getText().toString());
            JSONArray emptyArray = new JSONArray();
            postData.put("images", emptyArray);
            postData.put("type", "0");
            postData.put("owner", mSharedPreferences.getString("userID", ""));


            JSONArray coord = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            coord.put(0);
            coord.put(0);
            jsonObject.put("coordinates", coord);

            postData.put("geo", jsonObject);

            mSocket.emit(API_Methods.VERSION+":posts:new post", postData);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(this);
            mProgressbar.setVisibility(View.GONE);
            mPostButton.setVisibility(View.VISIBLE);
        }
    }


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: failed socket connection");
        }
    };


    //event ERROR
    private Emitter.Listener eventError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "call: " + args[0]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showServerErrorToast(PostCreatePage.this);
                    mPostInProgress = false;
                    mProgressbar.setVisibility(View.GONE);
                    mPostButton.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    private Emitter.Listener newPost = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject t = (JSONObject) args[0];
            if (t != null){
                try {
                    if (t.getJSONObject("owner").getString("id").equals(mSharedPreferences.getString("userID", "1"))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setResult(RESULT_OK);
                                Toast.makeText(PostCreatePage.this, "Status has been posted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };
}
