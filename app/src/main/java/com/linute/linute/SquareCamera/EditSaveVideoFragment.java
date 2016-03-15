package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


/**
 *
 */
public class EditSaveVideoFragment extends Fragment {

    public static final String TAG = EditSaveVideoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    //public static final String ROTATION_KEY = "rotation";
    public static final String IMAGE_INFO = "image_info";
    public static final String MAKE_ANON = "make_anon";


    private View mFrame; //frame where we put edittext and picture

    private CustomBackPressedEditText mText; //text
    private ProgressBar mProgressBar;
    private View mButtonLayer;
    private CheckBox mAnonSwitch;

    private String mCollegeId;
    private String mUserId;

    private View mUploadButton;

    private Uri mVideoLink;

    private TextureVideoView mSquareVideoView;


    public static Fragment newInstance(Uri imageUri, boolean makeAnon) {
        Fragment fragment = new EditSaveVideoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putBoolean(MAKE_ANON, makeAnon);

        fragment.setArguments(args);
        return fragment;
    }

    public EditSaveVideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.square_camera_edit_save_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID","");

        //setup ImageView
        mVideoLink = getArguments().getParcelable(BITMAP_URI);
        mSquareVideoView= (TextureVideoView) view.findViewById(R.id.video_frame);

        mSquareVideoView.setVideoURI(mVideoLink);

        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mSquareVideoView.start();
            }
        });

        view.findViewById(R.id.square_videoFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSquareVideoView.isPlaying()){
                    mSquareVideoView.pause();
                }else {
                    mSquareVideoView.start();
                }
            }
        });

        //shows the text strip when image touched
        mButtonLayer = view.findViewById(R.id.editFragment_button_layer);
        mProgressBar = (ProgressBar) view.findViewById(R.id.editFragment_progress_bar);

        mAnonSwitch = (CheckBox) view.findViewById(R.id.editFragment_switch);
        mAnonSwitch.setChecked(getArguments().getBoolean(MAKE_ANON));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.edit_photo_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Video");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraActivity) getActivity()).clearBackStack();
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
    }


    @Override
    public void onDestroy() {
        ImageUtility.deleteCachedVideo(mVideoLink);
        super.onDestroy();
    }


    private void sendPicture() {
//
//        if (getActivity() == null) return;
//
//        if (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected()){
//            Utils.showBadConnectionToast(getActivity());
//            return;
//        }
//
//
//        showProgress(true);
//
//        if (getActivity() == null) return;
//        try {
//            JSONObject postData = new JSONObject();
//
//            postData.put("college", mCollegeId);
//            postData.put("privacy", (mAnonSwitch.isChecked() ? 1 : 0) + "");
//            postData.put("title", mText.getText().toString());
//            JSONArray imageArray = new JSONArray();
//            imageArray.put(Utils.encodeImageBase64(bitmap));
//            postData.put("images", imageArray);
//            postData.put("type", "1");
//            postData.put("owner", mUserId);
//
//
//            JSONArray coord = new JSONArray();
//            JSONObject jsonObject = new JSONObject();
//            coord.put(0);
//            coord.put(0);
//            jsonObject.put("coordinates", coord);
//
//            postData.put("geo", jsonObject);
//
//            mSocket.emit(API_Methods.VERSION + ":posts:new post", postData);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Utils.showServerErrorToast(getActivity());
//            showProgress(false);
//        }
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

        mSquareVideoView.start();

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
                                    "&api=" + API_Methods.VERSION;
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

            if (getActivity() != null) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("owner", mUserId);
                    obj.put("action", "active");
                    obj.put("screen", "Create");
                    mSocket.emit(API_Methods.VERSION + ":users:tracking", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @Override
    public void onPause() {
        super.onPause();

        mSquareVideoView.stopPlayback();

        if (mSocket != null) {

            if (getActivity() != null) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("owner", mUserId);
                    obj.put("action", "inactive");
                    obj.put("screen", "Create");
                    mSocket.emit(API_Methods.VERSION + ":users:tracking", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

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