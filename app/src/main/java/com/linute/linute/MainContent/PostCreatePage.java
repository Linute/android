package com.linute.linute.MainContent;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
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


import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PostCreatePage extends BaseFragment implements View.OnClickListener {
    public static final String TAG = PostCreatePage.class.getSimpleName();

    private CustomBackPressedEditText mPostEditText;
    private View mTextFrame;

    private View mPostButton;
    private View mProgressbar;

    private boolean mPostInProgress = false;

    private CheckBox vAnonPost;
    private CheckBox vAnonComments;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_new_post_create, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.postContentToolbar);
        mPostButton = toolbar.findViewById(R.id.create_page_post_button);
        mProgressbar = toolbar.findViewById(R.id.create_page_progress_bar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || mProgressbar.getVisibility() == View.VISIBLE) return;
                hideKeyboard();
                if (mPostEditText.getText().toString().isEmpty()) {
                    getActivity().setResult(RESULT_CANCELED);
                    getActivity().finish();
                } else {
                    showConfirmDialog();
                }
            }
        });

        toolbar.setTitle("Status");

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                postContent();
            }
        });
        mPostEditText = (CustomBackPressedEditText) root.findViewById(R.id.post_create_text);

        mPostEditText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
                "Lato-LightItalic.ttf"));
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

        mPostEditText.addTextChangedListener(new TextWatcher() {
            String beforeText;
            final int maxLines = 8;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPostEditText.getLineCount() > maxLines) {
                    mPostEditText.setText(beforeText);
                    mPostEditText.setSelection(mPostEditText.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        vAnonComments = (CheckBox) root.findViewById(R.id.anon_comments);
        vAnonPost = (CheckBox) root.findViewById(R.id.anon_post);

        mTextFrame = root.findViewById(R.id.post_create_frame);

        root.findViewById(R.id.post_create_0).setOnClickListener(this);
        root.findViewById(R.id.post_create_1).setOnClickListener(this);
        root.findViewById(R.id.post_create_2).setOnClickListener(this);
        root.findViewById(R.id.post_create_3).setOnClickListener(this);
        root.findViewById(R.id.post_create_4).setOnClickListener(this);
        root.findViewById(R.id.post_create_5).setOnClickListener(this);

        return root;
    }

    private Socket mSocket;
    private boolean mConnecting = false;

    private void showConfirmDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("you sure?")
                .setMessage("would you like to throw away what you have currently?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() == null) return;
                        getActivity().setResult(RESULT_CANCELED);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;

            {
                try {
                    IO.Options op = new IO.Options();
                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(getActivity());
                    op.query =
                            "token=" + mSharedPreferences.getString("userToken", "") +
                                    "&deviceToken=" + device.getDeviceToken() +
                                    "&udid=" + device.getUdid() +
                                    "&version=" + device.getVersionName() +
                                    "&build=" + device.getVersionCode() +
                                    "&os=" + device.getOS() +
                                    "&type=" + device.getType() +
                                    "&api=" + API_Methods.VERSION +
                                    "&model=" + device.getModel();
                    op.reconnectionDelay = 5;
                    op.secure = true;


                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/
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
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.off(Socket.EVENT_ERROR, eventError);
            mSocket.off("new post", newPost);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void postContent() {

        if (getActivity() == null || mPostInProgress || mPostEditText.getText().toString().trim().equals("")) return;

        if (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected()) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        mPostInProgress = true;

        mProgressbar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);

        try {
            JSONObject postData = new JSONObject();
            postData.put("college", mSharedPreferences.getString("collegeId", ""));
            postData.put("privacy", (vAnonPost.isChecked() ? 1 : 0) + "");
            postData.put("isAnonymousCommentsDisabled", vAnonComments.isChecked() ? 0 : 1);
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
            imageArray.put(Utils.encodeImageBase64(Bitmap.createScaledBitmap(getBitmapFromView(mTextFrame), 720, 720, true)));
            postData.put("images", imageArray);

            mSocket.emit(API_Methods.VERSION + ":posts:new post", postData);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(getActivity());
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
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showServerErrorToast(getActivity());
                        mPostInProgress = false;
                        mProgressbar.setVisibility(View.GONE);
                        mPostButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };

    private Emitter.Listener newPost = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject t = (JSONObject) args[0];
            if (getActivity() == null) return;
            if (t != null) {
                try {
                    if (t.getJSONObject("owner").getString("id").equals(mSharedPreferences.getString("userID", "1"))) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().setResult(RESULT_OK);
                                Toast.makeText(getActivity(), "Status has been posted", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (getActivity() == null) return;

        int viewId = v.getId();

        int backgroundColor;
        int textColor;

        switch (viewId) {
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

        mPostEditText.setTextColor(ContextCompat.getColor(getActivity(), textColor));
        mTextFrame.setBackgroundColor(ContextCompat.getColor(getActivity(), backgroundColor));
    }


    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPostEditText.getWindowToken(), 0);
        mPostEditText.clearFocus(); //release focus from EditText and hide keyboard
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
