package com.linute.linute.MainContent;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
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


public class PostCreatePage extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PostCreatePage.class.getSimpleName();

    private CustomBackPressedEditText mPostEditText;
    private View mTextFrame;

    private boolean mSwitchOn = false;

    private View mPostButton;
    private View mProgressbar;

    private boolean mPostInProgress = false;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post_create);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.postContentToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);

        mPostButton = toolbar.findViewById(R.id.create_page_post_button);
        mProgressbar = toolbar.findViewById(R.id.create_page_progress_bar);

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                postContent();
            }
        });
        mPostEditText = (CustomBackPressedEditText) findViewById(R.id.post_create_text);
        mPostEditText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard();
            }
        });

        mPostEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                }
                return false;
            }
        });

        final ImageView mAnonymousSwitch = (ImageView) findViewById(R.id.post_create_anon_switch);
        mAnonymousSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitchOn){
                    mSwitchOn = false;
                    mAnonymousSwitch.setImageResource(R.drawable.ic_anon_off);
                }else {
                    mSwitchOn = true;
                    mAnonymousSwitch.setImageResource(R.drawable.ic_anon_on);
                }
            }
        });

        mTextFrame = findViewById(R.id.post_create_frame);

        findViewById(R.id.post_create_0).setOnClickListener(this);
        findViewById(R.id.post_create_1).setOnClickListener(this);
        findViewById(R.id.post_create_2).setOnClickListener(this);
        findViewById(R.id.post_create_3).setOnClickListener(this);
        findViewById(R.id.post_create_4).setOnClickListener(this);
        findViewById(R.id.post_create_5).setOnClickListener(this);
    }

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
        if (mPostEditText.getText().toString().trim().equals("")) return;

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
            postData.put("privacy", (mSwitchOn ? 1 : 0) + "");
            postData.put("title", mPostEditText.getText().toString());

            JSONArray coord = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            coord.put(0);
            coord.put(0);
            jsonObject.put("coordinates", coord);

            postData.put("geo", jsonObject);
            postData.put("type", "0");
            postData.put("owner", mSharedPreferences.getString("userID", ""));

            JSONArray imageArray = new JSONArray();
            imageArray.put(Utils.encodeImageBase64(Bitmap.createScaledBitmap(getBitmapFromView(mTextFrame), 1080, 1080, true)));
            postData.put("images", imageArray);

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

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        int backgroundColor;
        int textColor;

        switch (viewId){
            case R.id.post_create_0:
                backgroundColor = R.color.post_color_0;
                textColor = R.color.pure_black;
                break;
            case R.id.post_create_1:
                backgroundColor = R.color.post_color_1;
                textColor = R.color.pure_white;
                break;
            case R.id.post_create_2:
                backgroundColor = R.color.post_color_2;
                textColor = R.color.pure_white;
                break;
            case R.id.post_create_3:
                backgroundColor = R.color.post_color_3;
                textColor = R.color.pure_white;
                break;
            case R.id.post_create_4:
                backgroundColor = R.color.post_color_4;
                textColor = R.color.pure_white;
                break;
            case R.id.post_create_5:
                backgroundColor = R.color.post_color_5;
                textColor = R.color.pure_white;
                break;
            default:
                backgroundColor = R.color.post_color_0;
                textColor = R.color.pure_black;
                break;
        }

        mPostEditText.setTextColor(ContextCompat.getColor(PostCreatePage.this, textColor));
        mTextFrame.setBackgroundColor(ContextCompat.getColor(PostCreatePage.this, backgroundColor));
    }


    private void hideKeyboard() {
        mPostEditText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPostEditText.getWindowToken(), 0);
    }


    //cuts a bitmap from our RelativeLayout
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

}
