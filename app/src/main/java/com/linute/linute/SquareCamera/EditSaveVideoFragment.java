package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

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
    public static final String MAKE_ANON = "make_anon";

    public static final String VIDEO_DIMEN = "video_dimen";


    //private View mFrame; //frame where we put edittext and picture

    //private CustomBackPressedEditText mText; //text
    private CheckBox mAnonSwitch;
    private CheckBox mCommentsAnon;

    private String mCollegeId;
    private String mUserId;

    private View mUploadButton;

    private Uri mVideoLink;
    //private String mTempVideoLink;

    private ProgressDialog mProgressDialog;

    private View vPlayIcon;
    private TextureVideoView mSquareVideoView;

    private VideoDimen mVideoDimen;


    public static Fragment newInstance(Uri imageUri, boolean makeAnon, VideoDimen videoDimen) {
        Fragment fragment = new EditSaveVideoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putBoolean(MAKE_ANON, makeAnon);

        args.putParcelable(VIDEO_DIMEN, videoDimen);

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
        mUserId = sharedPreferences.getString("userID", "");

        //setup VideoView
        mVideoLink = getArguments().getParcelable(BITMAP_URI);
        mSquareVideoView = (TextureVideoView) view.findViewById(R.id.video_frame);
        vPlayIcon = view.findViewById(R.id.play_icon);

        mVideoDimen = getArguments().getParcelable(VIDEO_DIMEN);

        mSquareVideoView.setVideoURI(mVideoLink);
        mSquareVideoView.seekTo(0);

        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mSquareVideoView.start();
            }
        });

        view.findViewById(R.id.square_videoFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSquareVideoView.isPlaying()) {
                    mSquareVideoView.pause();
                    vPlayIcon.setVisibility(View.VISIBLE);
                } else {
                    mSquareVideoView.start();
                    vPlayIcon.setVisibility(View.GONE);
                }
            }
        });

        //shows the text strip when image touched

        mCommentsAnon = (CheckBox) view.findViewById(R.id.anon_comments);
        mAnonSwitch = (CheckBox) view.findViewById(R.id.anon_post);
        mAnonSwitch.setChecked(getArguments().getBoolean(MAKE_ANON));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.edit_photo_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Video");
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
                processVideo();
            }
        });

        FFmpeg ffmpeg = FFmpeg.getInstance(getActivity());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    showProgress(true);
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                    showProgress(false);
                }
            });
        } catch (FFmpegNotSupportedException e) {
            //handle
            e.printStackTrace();
            if (getActivity() == null) return;
            new AlertDialog.Builder(getActivity())
                    .setMessage("We're sorry. We're having trouble processing video on your device. Please let the dev team know what device you are using.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            //take them back to camera screen
                            CameraActivity activity = (CameraActivity) getActivity();
                            if (activity != null) {
                                activity.clearBackStack();
                            }
                        }
                    });
        }
    }

    private void showConfirmDialog() {
        if (getActivity() == null) return;
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


    @Override
    public void onDestroy() {
        ImageUtility.deleteCachedVideo(mVideoLink);
        super.onDestroy();
    }


    private void processVideo() {
        if (getActivity() == null || mVideoLink == null) return;

        if (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected()) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        mSquareVideoView.stopPlayback();
        vPlayIcon.setVisibility(View.VISIBLE);

        FFmpeg ffmpeg = FFmpeg.getInstance(getActivity());
        final String outputFile = ImageUtility.getVideoUri();

        showProgress(true);

        String cmd = "-i " + mVideoLink + " "; //input file

        String crop = String.format(Locale.US,
                "-vf crop=%d:%d:%d:0 ",
                mVideoDimen.height,
                mVideoDimen.height,
                (mVideoDimen.isFrontFacing ? mVideoDimen.width - mVideoDimen.height : 0));

        cmd += crop; //crop
        cmd += "-preset superfast ";
        //cmd += "-strict -2 "; //audio
        cmd += "-c:a copy "; //copy instead of re-encoding audio
        cmd += outputFile; //output file;

        try {
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {

                long startTime = 0;

                @Override
                public void onSuccess(String message) {
                    mProgressDialog.setMessage("Sending video...");
                    new sendVideoAsync().execute(outputFile,
                            mAnonSwitch.isChecked() ? "1" : "0",
                            mCommentsAnon.isChecked() ? "0" : "1");
                }

                @Override
                public void onProgress(String message) {
                    //Log.i(TAG, "onProgress: " + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, "onFailure: excute" + message);
                }

                @Override
                public void onStart() {
                    startTime = System.currentTimeMillis();
                }

                @Override
                public void onFinish() {
                    if (getActivity() == null) return;
                    Log.i(TAG, "processed video in milliseconds: " + (System.currentTimeMillis() - startTime));
                    ImageUtility.broadcastVideo(getActivity(), outputFile); //so gallery app can see video
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }


    private void showProgress(final boolean show) {
        if (getActivity() == null) return;
        mAnonSwitch.setVisibility(show ? View.GONE : View.VISIBLE);
        mCommentsAnon.setVisibility(show ? View.GONE : View.VISIBLE);
        mUploadButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);

        if (show) {
            if (mProgressDialog == null) { //none exist yet
                mProgressDialog = ProgressDialog.show(getActivity(), null, "Processing video. This may take a moment.", true);
            } else { //show it
                mProgressDialog.show();
            }
        } else if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
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
                                    "&api=" + API_Methods.VERSION;
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

        mSquareVideoView.pause();
        vPlayIcon.setVisibility(View.VISIBLE);

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

    private Emitter.Listener newPost = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;
            try {
                //if it was your post that got posted, finished this activity
                String owner = new JSONObject(args[0].toString()).getJSONObject("owner").getString("id");
                if (owner.equals(mUserId)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog != null) mProgressDialog.dismiss();
                            getActivity().setResult(Activity.RESULT_OK);
                            Toast.makeText(getActivity(), "Video has been posted", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                }
            } catch (JSONException e) {
                Log.i(TAG, "call: error in newPost Listener");
            }
        }
    };


    private class sendVideoAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            if (getActivity() == null || params[0] == null || params[1] == null || params[2] == null)
                return null;

            String outputFile = params[0];

            if (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                    }
                });
                return null;
            }

            //get first frame in video as bitmap
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(outputFile);
            Bitmap map = Bitmap.createScaledBitmap(media.getFrameAtTime(0), 1080, 1080, true);
            media.release();

            try {
                JSONObject postData = new JSONObject();

                postData.put("college", mCollegeId);
                postData.put("privacy", params[1]);
                postData.put("isAnonymousCommentsDisabled", params[2]);
                postData.put("title", ""); //todo title
                JSONArray imageArray = new JSONArray();
                imageArray.put(Utils.encodeImageBase64(map));

                JSONArray videoArray = new JSONArray();
                videoArray.put(Utils.encodeFileBase64(new File(outputFile)));

                postData.put("images", imageArray);
                postData.put("videos", videoArray);
                postData.put("type", "2"); //0 for status 1 for image and 2 for video
                postData.put("owner", mUserId);

                JSONArray coord = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                coord.put(0);
                coord.put(0);
                jsonObject.put("coordinates", coord);

                postData.put("geo", jsonObject);
                mSocket.emit(API_Methods.VERSION + ":posts:new post", postData);

            } catch (JSONException | IOException e) {
                e.printStackTrace();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            showProgress(false);
                        }
                    });
                }
            }
            return null;
        }
    }

    public static class VideoDimen implements Parcelable {

        int height; //480 or less
        int width;
        boolean isFrontFacing;


        public VideoDimen(int height, int width, boolean isFrontFacing) {
            this.height = height;
            this.width = width;
            this.isFrontFacing = isFrontFacing;
        }

        protected VideoDimen(Parcel in) {
            height = in.readInt();
            width = in.readInt();
            isFrontFacing = in.readByte() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(height);
            dest.writeInt(width);
            dest.writeByte((byte) (isFrontFacing ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<VideoDimen> CREATOR = new Creator<VideoDimen>() {
            @Override
            public VideoDimen createFromParcel(Parcel in) {
                return new VideoDimen(in);
            }

            @Override
            public VideoDimen[] newArray(int size) {
                return new VideoDimen[size];
            }
        };
    }

}