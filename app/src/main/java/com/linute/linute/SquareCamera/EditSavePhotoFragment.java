package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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


/**
 *
 */
public class EditSavePhotoFragment extends Fragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    //public static final String ROTATION_KEY = "rotation";
    public static final String IMAGE_INFO = "image_info";
    public static final String MAKE_ANON = "make_anon";


    private View mFrame; //frame where we put edittext and picture

    private CustomBackPressedEditText mText; //text
    private ProgressBar mProgressBar;
    private View mButtonLayer;
    private CheckBox mAnonSwitch;
    private CheckBox mAnonComments;

    private String mCollegeId;
    private String mUserId;

    private View mUploadButton;


    public static Fragment newInstance(Uri imageUri, boolean makeAnon) {
        Fragment fragment = new EditSavePhotoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putBoolean(MAKE_ANON, makeAnon);

        fragment.setArguments(args);
        return fragment;
    }

    public EditSavePhotoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.squarecamera__fragment_edit_save_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID","");

        //setup ImageView
        Uri imageUri = getArguments().getParcelable(BITMAP_URI);
        final ImageView photoImageView = (ImageView) view.findViewById(R.id.photo);

        photoImageView.setImageURI(imageUri);

        //photoImageView.setImageURI(imageUri);

        //shows the text strip when image touched
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mText.getVisibility() == View.GONE) {
                    mText.setVisibility(View.VISIBLE);
                    mText.requestFocus();
                    showKeyboard();
                    mCanMove = false; //can't mvoe strip while in edit
                }
            }
        });

        mFrame = view.findViewById(R.id.frame); //frame where we put edittext and picture
        mText = (CustomBackPressedEditText) view.findViewById(R.id.editFragment_title_text);
        mButtonLayer = view.findViewById(R.id.editFragment_button_layer);
        mProgressBar = (ProgressBar) view.findViewById(R.id.editFragment_progress_bar);

        mAnonSwitch = (CheckBox) view.findViewById(R.id.editFragment_switch);
        mAnonSwitch.setChecked(getArguments().getBoolean(MAKE_ANON));
        mAnonComments = (CheckBox) view.findViewById(R.id.anon_comments);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.edit_photo_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Tap the photo to add text");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });

        mUploadButton = view.findViewById(R.id.save_photo);

        //save button
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPicture();
            }
        });

        setUpEditText();
    }


    private void showConfirmDialog(){
        if (getActivity() == null) return;
        hideKeyboard();
        new AlertDialog.Builder(getActivity())
                .setTitle("you sure?")
                .setMessage("would you like to throw away what you have currently?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((CameraActivity) getActivity()).clearBackStack();
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    //text strip can move or not
    //can't move during edit
    private boolean mCanMove = true;

    private void setUpEditText() {

        //when back is pressed
        mText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard();

                mCanMove = true;

                //if EditText is empty, hide it
                if (mText.getText().toString().trim().isEmpty())
                    mText.setVisibility(View.GONE);
            }
        });

        //when done is pressed on keyboard
        mText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();

                    mCanMove = true;

                    //if EditText is empty, hide it
                    if (mText.getText().toString().trim().isEmpty())
                        mText.setVisibility(View.GONE);
                }
                return false;
            }
        });

        //movement
        mText.setOnTouchListener(new View.OnTouchListener() {
            float prevY;
            private boolean stopped = true;
            float totalMovement;


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!mCanMove) return false; //can't move, so stop

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        prevY = event.getY();
                        stopped = false;
                        totalMovement = 0;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (totalMovement <= 2) { //tapped and no movement
                            mText.requestFocus(); //open edittext
                            showKeyboard();
                            mCanMove = false;
                        }
                        stopped = true;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int change = (int) (event.getY() - prevY);
                        totalMovement += Math.abs(change);
                        if (!stopped) { //move the edittext around

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();

                            int newTop = params.topMargin + change; //new margintop

                            if (newTop < 0) { //over the top edge
                                newTop = 0;
                                stopped = true;
                            } else if (newTop > mFrame.getHeight() - v.getHeight()) { //under the bottom edge
                                newTop = mFrame.getHeight() - v.getHeight();
                                stopped = true;
                            }

                            params.setMargins(0, newTop, 0, 0); //set new margin
                            v.setLayoutParams(params);
                        }
                        break;
                }

                return true;
            }

        });
    }

    private void sendPicture() {

        if (getActivity() == null) return;

        if(mText.getText().toString().trim().isEmpty()){
            mText.setVisibility(View.GONE);
        }

        hideKeyboard();

        mFrame.requestFocus();

        if (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected()){
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        Bitmap bitmap = Bitmap.createScaledBitmap(getBitmapFromView(mFrame), 1080, 1080, true);

        showProgress(true);

        if (getActivity() == null) return;
        try {
            JSONObject postData = new JSONObject();

            postData.put("college", mCollegeId);
            postData.put("privacy", (mAnonSwitch.isChecked() ? 1 : 0) + "");
            postData.put("isAnonymousCommentsDisabled", mAnonComments.isChecked() ? 0 : 1);
            postData.put("title", mText.getText().toString());
            JSONArray imageArray = new JSONArray();
            imageArray.put(Utils.encodeImageBase64(bitmap));
            postData.put("images", imageArray);
            postData.put("type", "1");
            postData.put("owner", mUserId);


            JSONArray coord = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            coord.put(0);
            coord.put(0);
            jsonObject.put("coordinates", coord);

            postData.put("geo", jsonObject);

            mSocket.emit(API_Methods.VERSION + ":posts:new post", postData);

        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(getActivity());
            showProgress(false);
        }
    }

    private void showServerError() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(getActivity());
                showProgress(false);
            }
        });
    }

    private void showKeyboard() { //show keyboard for EditText
        InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(mText, 0);
    }

    private void hideKeyboard() {
        mText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFrame.getWindowToken(), 0);
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


    private void showProgress(final boolean show) {
        mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        mUploadButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private Socket mSocket;
    private boolean mConnecting = false;

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null) return;

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;

            {
                try {
                    IO.Options op = new IO.Options();
                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(getActivity());
                    op.query =
                            "token=" + getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken", "") +
                                    "&deviceToken=" + device.getDeviceToken() +
                                    "&udid=" + device.getUdid() +
                                    "&version=" + device.getVersonName() +
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
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showServerErrorToast(getActivity());
                    showProgress(false);
                }
            });
        }
    };

    //new post was posted; we aren't sure if we're the one that posted it. must check
    private Emitter.Listener newPost = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;

            try {
                String owner = new JSONObject(args[0].toString()).getJSONObject("owner").getString("id");
                if (owner.equals(mUserId)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().setResult(Activity.RESULT_OK);
                            Toast.makeText(getActivity(), "Photo has been posted", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                }
            }catch (JSONException e){
                Log.i(TAG, "call: error in newPost Listener");
            }
        }
    };

}
