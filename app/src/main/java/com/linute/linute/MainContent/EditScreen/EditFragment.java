package com.linute.linute.MainContent.EditScreen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by mikhail on 8/22/16.
 */
public class EditFragment extends BaseFragment {

    public static final String TAG = EditFragment.class.getSimpleName();

    private static final String ARG_URI = "content_uri";
    private static final String ARG_CONTENT_TYPE = "content_type";
    private static final String ARG_RETURN_TYPE = "return_type";
    private static final String ARG_DIMEN = "dimen";
    private static final String ARG_CAMERA_TYPE = "camera_type";


    private ViewGroup mContentContainer;
    private View mFinalContentView;
    private ViewGroup mToolOptionsView;
    private int mSelectedTool;
    private ToolHolder[] toolHolders;
    private ViewGroup mOverlaysContainer;
    private FFmpeg mFfmpeg;

    public enum ContentType {
        Photo, Video, UploadedPhoto, UploadedVideo
    }

    private Uri mUri;
    private ContentType mContentType;
    private Dimens mDimens;

    private EditContentTool[] mTools;
    private View[] mToolViews;

    private int mReturnType;
//    private int mCameraType;

    Subscription mSubscription;

    private String mCollegeId;
    private String mUserId;
    private String mUserToken;

    View mContentView;


    public static EditFragment newInstance(Uri uri, ContentType contentType, int returnType, Dimens dimens/*, int cameraType*/) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putInt(ARG_CONTENT_TYPE, contentType.ordinal());
        args.putInt(ARG_RETURN_TYPE, returnType);
        args.putParcelable(ARG_DIMEN, dimens);
//        args.putInt(ARG_CAMERA_TYPE, cameraType);
        EditFragment fragment = new EditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUri = args.getParcelable(ARG_URI);
        mContentType = ContentType.values()[args.getInt(ARG_CONTENT_TYPE)];
//        mCameraType = args.getInt(ARG_CAMERA_TYPE);
        mReturnType = args.getInt(ARG_RETURN_TYPE);
        mDimens = args.getParcelable(ARG_DIMEN);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");
        mUserToken = sharedPreferences.getString("userToken", "");

        if(mContentType == ContentType.Video || mContentType == ContentType.UploadedVideo) {
            mFfmpeg = FFmpeg.getInstance(getContext());
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
                        .setMessage("We're sorry. We can't process video on your device. Please let the dev team know what device you are using and we'll find a way.")
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
                        }).create().show();
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        View decorView = getActivity().getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_edit_content, container, false);

        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_cancel);
        toolbar.inflateMenu(R.menu.menu_fragment_edit);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        getActivity().getSupportFragmentManager().popBackStack();
                        return true;
                    case R.id.menu_item_done:
                        onDoneButtonPress();
                        return true;
                }
                return false;
            }
        });

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int height = mDimens.height * displayWidth / mDimens.width;


        mFinalContentView = root.findViewById(R.id.final_content);
        mContentContainer = (ViewGroup) root.findViewById(R.id.base_content);
        setupMainContent(mUri, mContentType);

        mFinalContentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

        mToolOptionsView = (ViewGroup) root.findViewById(R.id.layout_tools_menu);

        mOverlaysContainer = (ViewGroup) root.findViewById(R.id.overlays);

        mTools = setupTools(mOverlaysContainer);
        mToolViews = new View[mTools.length];

        //Set up adapter that controls tool selection
        LinearLayout toolsListRV = (LinearLayout) root.findViewById(R.id.list_tools);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("AAA", "onclick "+view.getTag());
                onToolSelected((Integer)view.getTag());
            }
        };

        toolHolders = new ToolHolder[mTools.length];

        for (int i = 0; i < mTools.length; i++) {
            EditContentTool tool = mTools[i];
            View toolView = inflater.inflate(R.layout.list_item_tool, toolsListRV, false);
            toolHolders[i] = new ToolHolder(toolView);
            toolHolders[i].bind(tool);
            toolView.setTag(i);
            toolView.setOnClickListener(onClickListener);
            toolsListRV.addView(toolView);
        }

        onToolSelected(0);


        return root;
    }


    protected void onToolSelected(int i) {
        int oldSelectedTool = mSelectedTool;
        mSelectedTool = i;

        toolHolders[oldSelectedTool].setSelected(false);
        toolHolders[mSelectedTool].setSelected(true);

        mTools[oldSelectedTool].onClose();
        mTools[mSelectedTool].onOpen();

        mToolOptionsView.removeAllViews();
        if (mToolViews[i] == null) {
            mToolViews[i] = mTools[i].createToolOptionsView(LayoutInflater.from(mToolOptionsView.getContext()), mToolOptionsView);
        }
        mToolOptionsView.addView(mToolViews[i]);
    }


    private void setupMainContent(Uri uri, ContentType contentType) {
        switch (contentType) {
            case Photo:
            case UploadedPhoto:
                MoveZoomImageView imageView = new MoveZoomImageView(getContext());
                imageView.leftBound = 0;
                imageView.rightBound = getContext().getResources().getDisplayMetrics().widthPixels;
                imageView.topBound = 0;
                imageView.botBound = -2;
                imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mContentContainer.addView(imageView);

                imageView.setImageBitmap(BitmapFactory.decodeFile(uri.getPath()));
                imageView.setActive(false);

                mContentView = imageView;
//                imageView.setImageURI(uri);
                break;
            case Video:
            case UploadedVideo:

                final CheckBox mPlaying = new CheckBox(getContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

                mPlaying.setLayoutParams(params);
                mPlaying.setChecked(true);
                mPlaying.setButtonDrawable(R.drawable.play_pause_checkbox);
                final TextureVideoView mVideoView = new TextureVideoView(getContext());

                mPlaying.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) mVideoView.start();
                        else mVideoView.pause();
                    }
                });

//                vBottom.addView(mPlaying);

                mVideoView.setBackgroundResource(R.color.pure_black);

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                if (mDimens.isFrontFacing) mVideoView.setScaleX(-1);

                mVideoView.setVideoURI(mUri);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });

                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mPlaying.isChecked()) mVideoView.start();
                    }
                });
                mContentView = mVideoView;
                mContentContainer.addView(mContentView);
                break;
        }
    }

    private EditContentTool[] setupTools(ViewGroup overlay) {

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int height = mDimens.height * displayWidth / mDimens.width;


        //tools created in reverse priority order
        //(Crop appears above Text, which appears above Overlays, etc)
        PrivacySettingTool privacySettingTool = new PrivacySettingTool(mUri, mContentType, overlay);
        StickersTool stickersTool = new StickersTool(mUri, mContentType, overlay);
        OverlaysTool overlaysTool = new OverlaysTool(mUri, mContentType, overlay);
        TextTool textTool = new TextTool(mUri, mContentType, overlay, mDimens);
        CropTool cropTool;
        cropTool = new CropTool(mUri, mContentType, overlay, (mContentView instanceof Activatable ? (Activatable)mContentView: null));
        cropTool.MAX_SIZE = height;
        cropTool.MIN_SIZE = displayWidth/16 * 9;

        return new EditContentTool[]{
                privacySettingTool,
                cropTool,
                textTool,
                stickersTool,
                overlaysTool
        };

    }

    private void onDoneButtonPress() {
        ProcessingOptions options = new ProcessingOptions();
        for(EditContentTool tool : mTools){
            tool.onClose();
        }

        for (EditContentTool tool : mTools) {
            tool.processContent(mUri, mContentType, options);
        }



        beginUpload(options);
    }

    private void beginUpload(ProcessingOptions options) {
        switch (mContentType) {
            case Video:
            case UploadedVideo:
                processVideo(options);
                return;
            case Photo:
            case UploadedPhoto:
                processPhoto(options);
                return;
        }
    }

    private void showProgress(boolean show) {
    }

    private void processPhoto(final ProcessingOptions options) {
        if (getActivity() == null) return;

        /*if (mEditText.getVisibility() == View.VISIBLE) {
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
            hideKeyboard();
            return;
        }
        */


        showProgress(true);
        if (getActivity() == null) return;


        Bitmap bitmapFromView = ImageUtility.getBitmapFromView(mFinalContentView);
        Bitmap bitmap;
        if(options.topInset != 0 || options.bottomInset != 0){
            bitmap = Bitmap.createBitmap(bitmapFromView, 0, options.topInset, bitmapFromView.getWidth(), bitmapFromView.getHeight()-options.topInset-options.bottomInset);
            bitmapFromView.recycle();
        }else{
            bitmap = bitmapFromView;
        }


        if (mReturnType != CameraActivity.SEND_POST) {
            mSubscription = Observable.just(ImageUtility.savePictureToCache(getActivity(), bitmap))
                    .observeOn(io())
                    .subscribeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {
                                Intent i = new Intent()
                                        .putExtra("image", uri)
                                        .putExtra("type", CameraActivity.IMAGE)
                                        .putExtra("privacy", options.postAsAnon)
                                        .putExtra("title", options.text);
                                getActivity().setResult(Activity.RESULT_OK, i);
                                getActivity().finish();
                            } else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), "An error occured while saving the image", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }
                        }
                    });
        } else {
            mSubscription = Observable.just(ImageUtility.savePicture(getActivity(), bitmap))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {

                                PendingUploadPost pendingUploadPost =
                                        new PendingUploadPost(
                                                ObjectId.get().toString(),
                                                mCollegeId,
                                                (options.postAsAnon ? 1 : 0),
                                                options.allowAnonComments ? 0 : 1,
                                                options.text,
                                                1,
                                                uri.toString(),
                                                null,
                                                mUserId,
                                                mUserToken
                                        );

                                Intent result = new Intent();
                                result.putExtra(PendingUploadPost.PENDING_POST_KEY, pendingUploadPost);
                                Toast.makeText(getActivity(), "Uploading photo in background...", Toast.LENGTH_SHORT).show();

                                getActivity().setResult(Activity.RESULT_OK, result);
                                getActivity().finish();
                            } else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), "An error occured while saving the image", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }
                        }
                    });
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public static class ToolHolder extends RecyclerView.ViewHolder {

        public ImageView vIcon;
        public TextView vLabel;

        public ToolHolder(View itemView) {
            super(itemView);
            vIcon = (ImageView) itemView.findViewById(R.id.image_icon);
            vLabel = (TextView) itemView.findViewById(R.id.text_label);

        }

        public void bind(EditContentTool tool) {
            vLabel.setText(tool.getName());
            vIcon.setImageResource(tool.getDrawable());
        }

        public void setSelected(boolean isSelected) {
            if (isSelected) {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.secondaryColor));
                vIcon.setColorFilter(new
                        PorterDuffColorFilter(vIcon.getResources().getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
            } else {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.pure_white));
                vIcon.setColorFilter(null);
            }
        }


    }

    @Override
    public void onDestroy() {
        if (mContentType == ContentType.Video && mDimens.deleteVideoWhenFinished)
            ImageUtility.deleteCachedVideo(mUri);

        super.onDestroy();
    }

    interface Activatable{
        void setActive(boolean active);
    }

   /* protected void hideKeyboard() {
        mEditText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContentContainer.getWindowToken(), 0);
    }*/


    Subscription mVideoProcessSubscription;
    
    private void processVideo(final ProcessingOptions options) {
//        if (getActivity() == null || mVideoLink == null || mVideoState != VS_IDLE) return;

        /*if (mEditText.hasFocus()) {
            mEditText.clearFocus();
            hideKeyboard();
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }

            return;
        }*/

//        mPlaying.setChecked(false);


        ((TextureVideoView)mContentView).stopPlayback();



        final String outputFile = ImageUtility.getVideoUri();
        showProgress(true);

        mVideoProcessSubscription = Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(final Subscriber<? super Uri> subscriber) {
                String cmd = " -i " + new File(mUri.getPath()).getAbsolutePath() + " -r 24 "; //input file

                boolean widthIsGreater = mDimens.height < mDimens.width;
                String overlay;

                int newWidth;
                int newHeight;
                if (widthIsGreater) {
                    if (mDimens.width > 720) {
                        newWidth = 720;
                        newHeight = ((mDimens.height * newWidth / mDimens.width / 2)) * 2;
                    } else {
                        newWidth = mDimens.width;
                        newHeight = mDimens.height;
                    }
                } else {
                    if (mDimens.height > 720) {
                        newHeight = 720;
                        newWidth = ((newHeight * mDimens.width / mDimens.height / 2)) * 2;
                    } else {
                        newWidth = mDimens.width;
                        newHeight = mDimens.height;
                    }
                }

                //Log.i(TAG, "call: new " + newWidth + " " + newHeight);
                //Log.i(TAG, "call: old " + mDimens.width + " " + mDimens.height);
                //Log.i(TAG, "call: rotation " + mDimens.rotation);

                 overlay = saveViewAsImage(mOverlaysContainer);

                //Log.i(TAG, "call:frame  " + mContentContainer.getHeight());
                //Log.i(TAG, "call: " + mTextView.getTop());

                if (overlay != null) {
                    cmd += "-i " + overlay + " -filter_complex ";
                    //scale vid
                    cmd += String.format(Locale.US,
                            "[0:v]scale=%d:%d[rot];", newWidth, newHeight);

                    if (mDimens.isFrontFacing) {
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

                    Point coord = new Point(0, 0);
                    //overlay
                    cmd += String.format(Locale.US,
                            "%s[over]overlay=%d:%d ", mDimens.isFrontFacing ? "[tran]" : "[rot]", coord.x, coord.y);
                } else {
                    if (mDimens.isFrontFacing) {
                        cmd += String.format(Locale.US,
                                "-filter_complex [0]scale=%d:%d[scaled];[scaled]hflip ", newWidth, newHeight);
                    } else {
                        cmd += String.format(Locale.US,
                                "-filter_complex scale=%d:%d ", newWidth, newHeight);
                    }
                }
                //}

                cmd += "-preset superfast "; //good idea to set threads?
                cmd += String.format(Locale.US,
                        "-metadata:s:v rotate=%d ", mDimens.rotation);
                cmd += "-c:a copy "; //copy instead of re-encoding audio
                cmd += outputFile; //output file;

                Log.i(TAG, "ffmped call");


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
                            Log.i(TAG, "onProgress: " + message);
                        }

                        @Override
                        public void onFailure(String message) {
                            Log.i(TAG, "onFailure: excute" + message);
//                            mVideoState = VS_IDLE;
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onStart() {
                            Log.i(TAG, "start vp");

//                            mVideoState = VS_PROCESSING;
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
                        if (mReturnType != CameraActivity.SEND_POST) {
                            Intent i = new Intent()
                                    .putExtra("video", Uri.parse(outputFile))
                                    .putExtra("image", image)
                                    .putExtra("privacy", options.postAsAnon)
                                    .putExtra("type", CameraActivity.VIDEO)
                                    /*.putExtra("title", mTextView.getText().toString())*/;

//                            mProgressDialog.dismiss();
                            getActivity().setResult(Activity.RESULT_OK, i);
                            getActivity().finish();
                        } else {
                            uploadVideo(image.toString(), outputFile, options);
                        }
                    }
                });
    }
    public String saveViewAsImage(View view) {
        try {
            File f = ImageUtility.getTempFile(getContext(), "overlay", ".png");
            FileOutputStream outputStream = new FileOutputStream(f);
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();

            //rotate the png so it can overlay correctly
            Matrix m = new Matrix();
            m.setRotate(360 - mDimens.rotation);

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


    private boolean isPortrait() {
        return mDimens.rotation == 90 || mDimens.rotation == 270;
    }


    private void uploadVideo(String imagepath, String videopath, ProcessingOptions options) {


        PendingUploadPost post = new PendingUploadPost(
                ObjectId.get().toString(),
                mCollegeId,
                options.postAsAnon ? 1 : 0,
                options.allowAnonComments ? 0 : 1,
//                mTextView.getText().toString(),
                "",
                2,
                imagepath,
                videopath,
                mUserId,
                mUserToken
        );

        showProgress(false);
        Intent result = new Intent();
        result.putExtra(PendingUploadPost.PENDING_POST_KEY, post);
        Toast.makeText(getActivity(), "Uploading video in background...", Toast.LENGTH_SHORT).show();

        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }


}
