package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 *
 */
public class EditSaveVideoFragment extends AbstractEditSaveFragment {

    public static final String TAG = EditSaveVideoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    public static final String VIDEO_DIMEN = "video_dimen";

    public static short VS_IDLE = 0;
    public static short VS_PROCESSING = 1;
    public static short VS_SENDING = 2;

    public short mVideoState = 0;

    private CheckBox mAnonSwitch;
    private CheckBox mCommentsAnon;
    private CheckBox mPlaying;
    private View mBottom;

    private String mCollegeId;
    private String mUserId;

    private View mUploadButton;

    private Uri mVideoLink;

    private ProgressDialog mProgressDialog;

    private TextureVideoView mSquareVideoView;
//    private CustomBackPressedEditText mEditText;
//    private TextView mTextView;

//    private CoordinatorLayout mStickerContainer;
//    private RecyclerView mStickerDrawer;

//    private View mFrame;

    private VideoDimen mVideoDimen;

    private int mReturnType;

    private Subscription mVideoProcessSubscription;
    private FFmpeg mFfmpeg;

//    private HasSoftKeySingleton mHasSoftKeySingleton;

    View mOverlays;


    public static Fragment newInstance(Uri imageUri, VideoDimen videoDimen) {
        Fragment fragment = new EditSaveVideoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putParcelable(VIDEO_DIMEN, videoDimen);

        fragment.setArguments(args);
        return fragment;
    }

    public EditSaveVideoFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mReturnType = ((CameraActivity) getActivity()).getReturnType();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");

        //setup VideoView


        final Toolbar t = (Toolbar) view.findViewById(R.id.top);
        t.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoState == VS_IDLE)
                    showConfirmDialog();
            }
        });

 /*       mFrame = view.findViewById(R.id.text_container);
        mFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getVisibility() == View.GONE) {
                    mEditText.setVisibility(View.VISIBLE);
                    mEditText.requestFocus();
                    showKeyboard();
                }
            }
        });*/




        mFfmpeg = FFmpeg.getInstance(getActivity());

        try {
            mFfmpeg.loadBinary(new LoadBinaryResponseHandler() {
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


    @Override
    protected void loadContent(ViewGroup container) {
        mVideoDimen = getArguments().getParcelable(VIDEO_DIMEN);

        View view = getView();

        FrameLayout bottomFL = (FrameLayout)view.findViewById(R.id.bottom);

        mPlaying = new CheckBox(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        mPlaying.setLayoutParams(params);
        mPlaying.setChecked(true);
        mPlaying.setButtonDrawable(R.drawable.play_pause_checkbox);


        mPlaying.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) mSquareVideoView.start();
                else mSquareVideoView.pause();
            }
        });

        bottomFL.addView(mPlaying);



        mVideoLink = getArguments().getParcelable(BITMAP_URI);

        mSquareVideoView = new TextureVideoView(container.getContext());
        if (mVideoDimen.isFrontFacing) mSquareVideoView.setScaleX(-1);

        mSquareVideoView.setVideoURI(mVideoLink);
        mSquareVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mSquareVideoView.start();
            }
        });
        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mPlaying.isChecked()) mSquareVideoView.start();
            }
        });
        container.addView(mSquareVideoView);
    }

    @Override
    protected void uploadContent() {
        processVideo();
    }

    /*

        private void setUpEditText() {
            //when back is pressed
            mEditText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
                @Override
                public void backPressed() {
                    hideKeyboard();

                    mEditText.setVisibility(View.GONE);

                    //if EditText is empty, hide it
                    if (!mEditText.getText().toString().trim().isEmpty()) {
                        mTextView.setText(mEditText.getText().toString());
                        mTextView.setVisibility(View.VISIBLE);
                    }

                }
            });

            //when done is pressed on keyboard
            mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        hideKeyboard();

                        mEditText.setVisibility(View.GONE);
                        //if EditText is empty, hide it
                        if (!mEditText.getText().toString().trim().isEmpty()) {
                            mTextView.setText(mEditText.getText().toString());
                            mTextView.setVisibility(View.VISIBLE);
                        }
                    }
                    return false;
                }
            });

            //movement
            mTextView.setOnTouchListener(new View.OnTouchListener() {
                float prevY;
                float totalMovement;
                int mTextMargin;
                int bottomMargin = -1;
                int topMargin = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            prevY = event.getY();
                            totalMovement = 0;
                            if (bottomMargin == -1) {
                                if (mFrame.getHeight() >= mHasSoftKeySingleton.getSize().y){
                                    bottomMargin = mHasSoftKeySingleton.getBottomPixels();
                                    topMargin = mUploadButton.getBottom();
                                }else {
                                    bottomMargin = 0;
                                    topMargin = 0;
                                }
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            if (totalMovement <= 2) { //tapped and no movement
                                mTextView.setVisibility(View.GONE);
                                mEditText.setVisibility(View.VISIBLE);
                                mEditText.requestFocus(); //open edittext
                                showKeyboard();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int change = (int) (event.getY() - prevY);
                            totalMovement += Math.abs(change);

                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTextView.getLayoutParams();

                            mTextMargin = params.topMargin + change; //new margintop

                            if (mTextMargin <= topMargin) { //over the top edge
                                mTextMargin = topMargin;
                            } else if (mTextMargin > mFrame.getHeight() - bottomMargin - v.getHeight()) { //under the bottom edge
                                mTextMargin = mFrame.getHeight() - bottomMargin - v.getHeight();
                            }

                            params.setMargins(0, mTextMargin, 0, 0); //set new margin
                            mTextView.setLayoutParams(params);

                            break;
                    }
                    return true;
                }
            });

        }
    */
    private void showConfirmDialog() {
        if (getActivity() == null) return;

        if (mEditText.getVisibility() == View.VISIBLE) {
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
            hideKeyboard();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("you sure?")
                .setMessage("would you like to throw away what you have currently?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() == null) return;
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
        if (mVideoDimen.deleteVideoWhenFinished)
            ImageUtility.deleteCachedVideo(mVideoLink);

        super.onDestroy();
    }


    private void processVideo() {
        if (getActivity() == null || mVideoLink == null || mVideoState != VS_IDLE) return;

        if (mEditText.hasFocus()) {
            mEditText.clearFocus();
            hideKeyboard();
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()){
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }

            return;
        }

        mPlaying.setChecked(false);

        if (mReturnType == CameraActivity.SEND_POST && (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected())) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        final String outputFile = ImageUtility.getVideoUri();
        showProgress(true);

        mVideoProcessSubscription = Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(final Subscriber<? super Uri> subscriber) {

                String cmd = "-i " + new File(mVideoLink.getPath()).getAbsolutePath() + " -r 24 "; //input file

                boolean widthIsGreater = mVideoDimen.height < mVideoDimen.width;

                int newWidth;
                int newHeight;
                if (widthIsGreater) {
                    if (mVideoDimen.width > 720) {
                        newWidth = 720;
                        newHeight = ((mVideoDimen.height * newWidth / mVideoDimen.width / 2)) * 2;
                    } else {
                        newWidth = mVideoDimen.width;
                        newHeight = mVideoDimen.height;
                    }
                } else {
                    if (mVideoDimen.height > 720) {
                        newHeight = 720;
                        newWidth = ((newHeight * mVideoDimen.width / mVideoDimen.height / 2)) * 2;
                    } else {
                        newWidth = mVideoDimen.width;
                        newHeight = mVideoDimen.height;
                    }
                }

                Log.i(TAG, "call: new " + newWidth + " " + newHeight);
                Log.i(TAG, "call: old " + mVideoDimen.width + " " + mVideoDimen.height);
                Log.i(TAG, "call: rotation " + mVideoDimen.rotation);


                if (mTextView.getVisibility() == View.GONE) {

                    if (mVideoDimen.isFrontFacing) {
                        cmd += String.format(Locale.US,
                                "-filter_complex [0]scale=%d:%d[scaled];[scaled]hflip ", newWidth, newHeight);
                    } else {
                        cmd += String.format(Locale.US,
                                "-filter_complex scale=%d:%d ", newWidth, newHeight);
                    }
                } else {
                    String overlay = saveViewAsImage(mOverlays);

                    Log.i(TAG, "call:frame  " + mContentContainer.getHeight());
                    Log.i(TAG, "call: " + mTextView.getTop());

                    if (overlay != null) {
                        cmd += "-i " + overlay + " -filter_complex ";
                        //scale vid
                        cmd += String.format(Locale.US,
                                "[0:v]scale=%d:%d[rot];", newWidth, newHeight);

                        if (mVideoDimen.isFrontFacing) {
                            //rotate vid
                            cmd += "[rot]hflip[tran];";
                        }

                        if (isPortrait()) {
                            cmd += String.format(Locale.US,
                                    "[1:v]scale=-1:%d[over];", newHeight);
                        } else {
                            cmd += String.format(Locale.US,
                                    "[1:v]scale=%d:-1[over];", newWidth);
                        }

                        Point coord = new Point(0,0);
                        //overlay
                        cmd += String.format(Locale.US,
                                "%s[over]overlay=%d:%d ", mVideoDimen.isFrontFacing ? "[tran]" : "[rot]", coord.x, coord.y);
                    } else {
                        if (mVideoDimen.isFrontFacing) {
                            cmd += String.format(Locale.US,
                                    "-filter_complex [0]scale=%d:%d[scaled];[scaled]hflip ", newWidth, newHeight);
                        } else {
                            cmd += String.format(Locale.US,
                                    "-filter_complex scale=%d:%d ", newWidth, newHeight);
                        }
                    }
                }

                cmd += "-preset superfast "; //good idea to set threads?
                cmd += String.format(Locale.US,
                        "-metadata:s:v rotate=%d ", mVideoDimen.rotation);
                cmd += "-c:a copy "; //copy instead of re-encoding audio
                cmd += outputFile; //output file;

                try {
                    mFfmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {

                        long startTime = 0;

                        @Override
                        public void onSuccess(String message) {
                            //get first frame in video as bitmap
                            if (getActivity() == null) return;

                            MediaMetadataRetriever media = new MediaMetadataRetriever();
                            media.setDataSource(outputFile);
                            Uri image = ImageUtility.savePictureToCache(getActivity(), media.getFrameAtTime(0));
                            media.release();

                            ImageUtility.broadcastVideo(getActivity(), outputFile); //so gallery app can see video
                            subscriber.onNext(image);
                        }

                        @Override
                        public void onProgress(String message) {
                            //Log.i(TAG, "onProgress: " + message);
                        }

                        @Override
                        public void onFailure(String message) {
                            Log.i(TAG, "onFailure: excute" + message);
                            mVideoState = VS_IDLE;
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onStart() {
                            mVideoState = VS_PROCESSING;
                            startTime = System.currentTimeMillis();
                        }

                        @Override
                        public void onFinish() {
                            Log.i(TAG, "processed video in milliseconds: " + (System.currentTimeMillis() - startTime));
                        }
                    });
                } catch (FFmpegCommandAlreadyRunningException e) {
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Uri>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Uri image) {

                        if (mReturnType == CameraActivity.RETURN_URI) {
                            Intent i = new Intent()
                                    .putExtra("video", Uri.parse(outputFile))
                                    .putExtra("image", image)
                                    .putExtra("type", CameraActivity.VIDEO)
                                    .putExtra("title", mTextView.getText().toString());

                            mProgressDialog.dismiss();
                            getActivity().setResult(Activity.RESULT_OK, i);
                            getActivity().finish();
                        } else {
                            mProgressDialog.setMessage("Uploading video...");

                            new sendVideoAsync().execute(outputFile,
                                    mAnonSwitch.isChecked() ? "1" : "0",
                                    mCommentsAnon.isChecked() ? "0" : "1",
                                    mTextView.getText().toString(),
                                    image.getPath()
                            );
                        }
                    }
                });
    }


    private boolean isPortrait() {
        return mVideoDimen.rotation == 90 || mVideoDimen.rotation == 270;
    }


/*


    private void showProgress(final boolean show) {
        if (getActivity() == null) return;

        mBottom.setVisibility(show ? View.GONE : View.VISIBLE);
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
*/



    private class sendVideoAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            if (getActivity() == null || params[0] == null || params[1] == null
                    || params[2] == null || params[3] == null || params[4] == null)
                return null;

            String outputFile = params[0];

            if (mReturnType == CameraActivity.SEND_POST && (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected())) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoState = VS_IDLE;
                        Utils.showBadConnectionToast(getActivity());
                        showProgress(false);
                    }
                });
                return null;
            }

            mVideoState = VS_SENDING;
            try {
                JSONObject postData = new JSONObject();

                postData.put("college", mCollegeId);
                postData.put("privacy", params[1]);
                postData.put("isAnonymousCommentsDisabled", params[2]);
                postData.put("title", params[3]);
                JSONArray imageArray = new JSONArray();
                imageArray.put(Utils.encodeImageBase64(
                        MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                                , Uri.fromFile(new File(params[4])))));

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

            mVideoState = VS_IDLE;
            return null;
        }
    }


    /*private void showKeyboard() { //show keyboard for EditText
        InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(mEditText, 0);
    }

    private void hideKeyboard() {
        mEditText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFrame.getWindowToken(), 0);
    }
*/

    //save the overlay as png
    public String saveViewAsImage(View view) {
        try {
            File f = ImageUtility.getTempFile(getActivity(), "overlay", ".png");
            FileOutputStream outputStream = new FileOutputStream(f);
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();

            //rotate the png so it can overlay correctly
            Matrix m = new Matrix();
            m.setRotate(360 - mVideoDimen.rotation);

            Bitmap bm = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getWidth(), view.getHeight(), m, true);
            view.destroyDrawingCache();
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static class VideoDimen implements Parcelable {

        int height;                         //vid height
        int width;                          //vid width
        boolean isFrontFacing;              //image was taken with front facing camera
        int rotation;                       //video rotation
        boolean deleteVideoWhenFinished;    //delete cached video


        //typically used when image taken with our camera
        public VideoDimen(int width, int height, boolean isFrontFacing) {
            this.height = height;
            this.width = width;
            this.isFrontFacing = isFrontFacing;
            this.rotation = 90;
            this.deleteVideoWhenFinished = true;
        }

        //when uploading from gallery
        public VideoDimen(int width, int height, int rotation) {
            this.height = height;
            this.width = width;
            this.isFrontFacing = false;
            this.rotation = rotation;

            //should not delete gallery's video
            this.deleteVideoWhenFinished = false;
        }

        protected VideoDimen(Parcel in) {
            height = in.readInt();
            width = in.readInt();
            isFrontFacing = in.readByte() == 1;
            rotation = in.readInt();
            deleteVideoWhenFinished = in.readByte() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(height);
            dest.writeInt(width);
            dest.writeByte((byte) (isFrontFacing ? 1 : 0));
            dest.writeInt(rotation);
            dest.writeByte((byte) (deleteVideoWhenFinished ? 1 : 0));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}