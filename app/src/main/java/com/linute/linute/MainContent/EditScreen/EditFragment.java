package com.linute.linute.MainContent.EditScreen;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.linute.linute.MainContent.EditScreen.PostOptions.ContentType;
import com.linute.linute.MainContent.EditScreen.Tools.CommentPrivacyTool;
import com.linute.linute.MainContent.EditScreen.Tools.CropTool;
import com.linute.linute.MainContent.EditScreen.Tools.EditContentTool;
import com.linute.linute.MainContent.EditScreen.Tools.OverlaysTool;
import com.linute.linute.MainContent.EditScreen.Tools.PrivacySettingTool;
import com.linute.linute.MainContent.EditScreen.Tools.StickersTool;
import com.linute.linute.MainContent.EditScreen.Tools.TextTool;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CustomFrameLayout;
import com.linute.linute.SquareCamera.CustomView;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.SquareCamera.ScreenSizeSingleton;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

/**
 * So what we need to account for because this is what iOS did:
 * VIDEO :
 * - CameraFragment returns video of ratio 4:3. We can use a view to cover the bottom section of the
 * video to make it look like it's 6:5 (or 1:1 on smaller phones)
 * - GalleryActivity can return videos of all sizes.
 * - video is landscape, we don't crop to 6:5.
 * - video is portrait:
 * - ratio bigger than 6:5, we crop
 * - ratio less than 6:5, we don't crop
 * <p/>
 * So yea, it's going to be a pain in the ass
 */
public class EditFragment extends BaseFragment {

    public static final String TAG = EditFragment.class.getSimpleName();

    private static final String ARG_URI = "content_uri";
    private static final String ARG_CONTENT_TYPE = "content_type";
    private static final String ARG_CONTENT_SUB_TYPE = "content_sub_type";
    private static final String ARG_RETURN_TYPE = "return_type";
    private static final String ARG_DIMEN = "dimen";
    private static final String ARG_CAMERA_TYPE = "camera_type";
    public static final int REQUEST_LOCATION = 23;
    public static final String ARG_POST_OPTIONS = "arg_post_options";


    private ViewGroup mContentContainer;
    private View mFinalContentView;
    private ViewGroup mToolOptionsView;


    private int mSelectedTool = 1;
    private ToolHolder[] toolHolders;
    private ViewGroup mOverlaysContainer;
    private FFmpeg mFfmpeg;
    private ProgressDialog mProcessingDialog;
    private Menu mMenu;
    private Toolbar mToolbar;
    private Bitmap mContentBitmap;



    private Uri mUri;
    private PostOptions mPostOptions;
    private Dimens mDimens;

    private EditContentTool[] mTools;
    private View[] mToolViews;
    boolean[] mIsDisabled;


    private int mReturnType;
//    private int mCameraType;

    Subscription mSubscription;

    private String mCollegeId;
    private String mUserId;
    private String mUserToken;

    View mContentView;


    /*public static EditFragment newInstance(Uri uri, ContentType contentType, ContentSubType contentSubType, int returnType, Dimens dimens*//*, int cameraType*//*) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putInt(ARG_CONTENT_TYPE, contentType.ordinal());
        args.putSerializable(ARG_CONTENT_SUB_TYPE, contentSubType);
        args.putInt(ARG_RETURN_TYPE, returnType);
        args.putParcelable(ARG_DIMEN, dimens);
//        args.putInt(ARG_CAMERA_TYPE, cameraType);
        EditFragment fragment = new EditFragment();
        fragment.setArguments(args);
        return fragment;
    }*/

    public static EditFragment newInstance(Uri uri,int returnType, Dimens dimens, PostOptions options) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putParcelable(ARG_POST_OPTIONS, options);
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
        ScreenSizeSingleton.init(getActivity().getWindowManager());

        mProcessingDialog = new ProgressDialog(getContext());
        mProcessingDialog.setIndeterminate(true);
        mProcessingDialog.setTitle("Processing Video");

        Bundle args = getArguments();
        mUri = args.getParcelable(ARG_URI);
        mPostOptions = args.getParcelable(ARG_POST_OPTIONS);

//        mCameraType = args.getInt(ARG_CAMERA_TYPE);
        mReturnType = args.getInt(ARG_RETURN_TYPE);
        mDimens = args.getParcelable(ARG_DIMEN);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");
        mUserToken = sharedPreferences.getString("userToken", "");

        if (mPostOptions.type == ContentType.Video || mPostOptions.type == ContentType.UploadedVideo) {
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(mDimens.needsCropping ? R.layout.fragment_edit_content_need_crop : R.layout.fragment_edit_content,
                container, false);

        mToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu_fragment_edit);
        mMenu = mToolbar.getMenu();

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_done:
                        onDoneButtonPress();
                        return true;
                }
                return false;
            }
        });
        String menuTitle = null;
        switch (mPostOptions.subType) {
            case Comment:
                menuTitle = null;
                break;
            case Post:
                menuTitle = "POST";
                break;
            case Chat:
                menuTitle = null;
                break;
        }

        if (menuTitle == null) {
            mMenu.findItem(R.id.menu_item_done).setIcon(R.drawable.ic_action_action_done);
        } else {
            mMenu.findItem(R.id.menu_item_done).setTitle(menuTitle);
        }

        mFinalContentView = root.findViewById(R.id.final_content);

        mContentContainer = (ViewGroup) root.findViewById(R.id.base_content);
        /*mContentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTools != null)
                    for (int i = 0; i < mTools.length; i++) {
                        if (mTools[i] instanceof TextTool) {
                            TextTool mTool = (TextTool) mTools[i];
                            if (!mTool.hasText()) {
                                onToolSelected(i);
                                mTool.selectTextMode(TextTool.MID_TEXT_INDEX);
                                break;
                            }
                        }
                    }
            }
        });*/

        final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        try {
            mToolOptionsView = (ViewGroup) root.findViewById(R.id.layout_tools_menu);
            mOverlaysContainer = (ViewGroup) root.findViewById(R.id.overlays);

            setupMainContent(mUri, mPostOptions.type, metrics);

            mTools = setupTools(mOverlaysContainer);
            mIsDisabled = new boolean[mTools.length];
            mToolViews = new View[mTools.length];
            toolHolders = new ToolHolder[mTools.length];

            ScreenSizeSingleton screenSingleton = ScreenSizeSingleton.getSingleton();
            //horrible hack
            if (mDimens.needsCropping && !screenSingleton.mHasRatioRequirement) {
                //screen was too small for 6:5 ratio, the video or image is a square, relayout accordingly
                CustomView view = (CustomView) root.findViewById(R.id.spacer);
                if (view != null) {
                    view.setMakeSquare(true);
                }

                CustomFrameLayout frame = (CustomFrameLayout) mOverlaysContainer;
                if (frame != null) {
                    frame.setMakeSquare(true);
                }

                root.requestLayout();

            } else if (!mDimens.needsCropping) { //need to check if item is smaller than 6:5

                //resize the overlays to match landscape image sizes
                //won't resize if image is bigger than 1.2f ratio
                /*FrameLayout inner = (FrameLayout) mFinalContentView.findViewById(R.id.inner_content);
                if (inner != null && overlaysNeedResizing(screenSingleton.mHasRatioRequirement ? ScreenSizeSingleton.MIN_RATIO : 1f)) {
                    int displayWidth = metrics.widthPixels;
                    int height;
                    if (isPortrait()) {
                        height = mDimens.width * displayWidth / mDimens.height;
                    } else {
                        height = mDimens.height * displayWidth / mDimens.width;
                    }
//                    inner.setLayoutParams(new FrameLayout.LayoutParams(displayWidth, height, Gravity.CENTER));
                }
*/
                //have to make it square
                if (!screenSingleton.mHasRatioRequirement && mFinalContentView instanceof CustomFrameLayout) {
                    ((CustomFrameLayout) mFinalContentView).setMakeSquare(true);
                    root.requestLayout();
                }

            }

            //Set up adapter that controls tool selection
            LinearLayout toolsListRV = (LinearLayout) root.findViewById(R.id.list_tools);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onToolSelected((Integer) view.getTag());
                }
            };

            for (int i = 0; i < mTools.length; i++) {
                EditContentTool tool = mTools[i];
                View toolView = inflater.inflate(R.layout.list_item_tool, toolsListRV, false);
                toolHolders[i] = new ToolHolder(toolView);
                toolHolders[i].bind(tool);
                toolHolders[i].setSelected(false, mIsDisabled[i]);

                toolView.setTag(i);
                toolView.setOnClickListener(onClickListener);
                toolsListRV.addView(toolView);
            }

            //for photos, mToolOptionView needs to load before making toolOptionView, so that crop icons can properly be measured
            if (mPostOptions.type == ContentType.Photo | mPostOptions.type == ContentType.UploadedPhoto) {
                mToolOptionsView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                        if (v.getHeight() > 0) {
                            if (mTools.length > 0)
                                onToolSelected(0);
                            v.removeOnLayoutChangeListener(this);
                        }
                    }
                });
            } else { //for video, toolOptionViews will load with width and height 0 unless made immediately. No idea why. This if-else works for now.
                onToolSelected(0);
            }

            showProgress(false);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return root;
    }

    private boolean overlaysNeedResizing(float minRatio) {
        if (isPortrait()) {
            return (float) mDimens.width / mDimens.height < minRatio;
        }

        return (float) mDimens.height / mDimens.width < minRatio;

    }


    public EditContentTool selectTool(EditContentTool tool) {
        for (int i = 0; i < mTools.length; i++) {
            if (mTools[i] == tool) {
                onToolSelected(i);
                return tool;
            }
        }
        return null;
    }

    public EditContentTool selectTool(Class<? extends EditContentTool> tool) {
        for (int i = 0; i < mTools.length; i++) {
            if (mTools[i].getClass() == tool) {
                onToolSelected(i);
                return mTools[i];
            }
        }
        return null;
    }

    protected void onToolSelected(int i) {
        if (mIsDisabled[i] || mSelectedTool == i) return;

        //called so that cropper view updates bounds
        mContentView.invalidate();

        int oldSelectedTool = mSelectedTool;
        mSelectedTool = i;

        toolHolders[oldSelectedTool].setSelected(false, mIsDisabled[oldSelectedTool]);
        toolHolders[mSelectedTool].setSelected(true, mIsDisabled[mSelectedTool]);

        mTools[oldSelectedTool].onClose();
        mTools[mSelectedTool].onOpen();

        //if (mTools[mSelectedTool] instanceof OverlaysTool) {
//            mContentView.setDrawingCacheEnabled(true);
//            mContentView.buildDrawingCache();
//
//            Bitmap bm = Bitmap.createBitmap(mContentView.getDrawingCache(), 0, 0, mContentView.getWidth(), mContentView.getHeight());
//            mContentView.destroyDrawingCache();
//            ((OverlaysTool) mTools[mSelectedTool]).setBackingBitmap(bm);
        // }

        if (mToolbar != null) {
            mToolbar.setTitle(mTools[mSelectedTool].getName());
        }

        mToolOptionsView.removeAllViewsInLayout();
        if (mToolViews[i] == null) {
            mToolViews[i] = mTools[i].createToolOptionsView(LayoutInflater.from(mToolOptionsView.getContext()), mToolOptionsView);
        }
        mToolOptionsView.addView(mToolViews[i]);
        mToolOptionsView.requestLayout();

        if(mTools[mSelectedTool] instanceof OverlaysTool && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }


    private void setupMainContent(final Uri uri, ContentType contentType, final DisplayMetrics metrics) {
        switch (contentType) {
            case Photo:
            case UploadedPhoto:
                final MoveZoomImageView imageView = new MoveZoomImageView(getContext());
                imageView.leftBound = 0;
                imageView.rightBound = getContext().getResources().getDisplayMetrics().widthPixels;
                imageView.topBound = -3;
                imageView.botBound = -3;
                imageView.topStickyBound = 0;
                imageView.botStickyBound = -2;
                imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mContentContainer.addView(imageView);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(uri.getPath(), opts);

                        //if image has rotation, we'll have to use height as it's width
                        final int imageWidth = isPortrait() ? opts.outHeight : opts.outWidth;

                        if (imageWidth > metrics.widthPixels)
                            opts.inSampleSize = imageWidth / metrics.widthPixels + 2;

                        opts.inJustDecodeBounds = false;

                        final Bitmap image = BitmapFactory.decodeFile(uri.getPath(), opts);
                        int testWidth = isPortrait() ? image.getHeight() : image.getWidth();

                        if (testWidth < metrics.widthPixels) {
                            final int scalewidth;
                            final int scaleheight;
                            if (isPortrait()) { //need to swap height and width
                                scaleheight = metrics.widthPixels;
                                scalewidth = (int) ((float) image.getWidth() * scaleheight / image.getHeight());
                            } else {
                                scalewidth = metrics.widthPixels;
                                scaleheight = (int) ((float) image.getHeight() * scalewidth / image.getWidth());

                            }

                            mDimens.height = scaleheight;
                            mDimens.width = scalewidth;

                            // Log.i(TAG, "run: "+image.getWidth() + " "+image.getHeight());
                            //Log.i(TAG, "run: "+scalewidth + " " + scaleheight);

                            Matrix m = new Matrix();
                            m.postScale((float) scalewidth / image.getWidth(), (float) scaleheight / image.getHeight());
                            m.postRotate(mDimens.rotation);

                            //scale image
                            final Bitmap scaled = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);

                            //i believe some phones will modify 'image'. don't want to recycle if that is the case
                            if (scaled != image) image.recycle();

                            //set image on main thread
                            new Handler(Looper.getMainLooper()).post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            imageView.setImageBitmap(scaled);
                                            imageView.invalidate();
                                            imageView.centerImage();
                                        }
                                    });
                            mContentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                //terrible hack. Listener will remove itself after 2 passes to keep from centering image everytime
                                int layoutPasses = 0;

                                @Override
                                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                    if (!mDimens.needsCropping && scaleheight / 5 != scalewidth / 6) {
                                        requestDisableToolListener.requestDisable(OverlaysTool.class, true);
                                    }
                                    imageView.centerImage();

                                    layoutPasses++;
                                    if (layoutPasses >= 3)
                                        mContentView.removeOnLayoutChangeListener(this);
                                }
                            });

                        } else {
                            if (mDimens.rotation != 0) {
                                Matrix m = new Matrix();
                                m.postRotate(mDimens.rotation);
                                final Bitmap rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
                                if (rotated != image) image.recycle();

                                setImage(rotated, imageView);
                            } else {
                                setImage(image, imageView);
                            }
                        }

                    }
                }).start();

                imageView.setActive(false);
                mContentView = imageView;

                break;
            case Video:
            case UploadedVideo:
                final CheckBox mPlaying = new CheckBox(getContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

                mPlaying.setLayoutParams(params);
                mPlaying.setChecked(true);
                mPlaying.setButtonDrawable(R.drawable.play_pause_checkbox);
                final TextureVideoView mVideoView = new TextureVideoView(getContext());

                //set videovew size, otherwise video will look squeezed
                //if (mDimens.needsCropping) {
                int width = mDimens.width;
                int height = mDimens.height;

                if (isPortrait()) {
                    width = mDimens.height;
                    height = mDimens.width;
                }

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(
                        metrics.widthPixels, (int) ((float) metrics.widthPixels * height / width), mDimens.needsCropping ? Gravity.NO_GRAVITY : Gravity.CENTER));


                mPlaying.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) mVideoView.start();
                        else mVideoView.pause();
                    }
                });

//                vBottom.addView(mPlaying);

                mVideoView.setBackgroundResource(R.color.pure_black);

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

                final int finalheight = height;
                final int finalwidth = width;


                mContentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    //terrible hack. Listener will remove itself after 2 passes to keep from centering image everytime

                    @Override
                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                        if (!mDimens.needsCropping && finalheight * 5 != finalwidth * 6) {
                            requestDisableToolListener.requestDisable(OverlaysTool.class, true);
                        }

                        mContentView.removeOnLayoutChangeListener(this);
                    }
                });
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setImage(final Bitmap image, final MoveZoomImageView imageView) {
        mContentBitmap = image;
        mDimens.height = image.getHeight();
        mDimens.width = image.getWidth();
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(image);
                        imageView.invalidate();
                        imageView.centerImage();
                    }
                });
    }


    RequestDisableToolListener requestDisableToolListener = new RequestDisableToolListener() {
        @Override
        public void requestDisable(Class<? extends EditContentTool> tool, boolean disable) {
            for (int i = 0; i < mTools.length; i++) {
                EditContentTool t = mTools[i];
                if (t.getClass() == tool) {
                    mIsDisabled[i] = disable;
                    if (disable) {
                        t.onDisable();
                    } else {
                        t.onEnable();
                    }
                }

                if (toolHolders[i] != null)
                    toolHolders[i].setSelected(mSelectedTool == i, mIsDisabled[i]);

            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            View v = getActivity().getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.clearFocus();
                mFinalContentView.requestFocus();
            }

            if (mTools != null) {
                for (EditContentTool tool : mTools) {
                    if (tool != null) tool.onPause();
                }
            }
        }
    }

    private EditContentTool[] setupTools(ViewGroup overlay) {

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int height = mDimens.height * displayWidth / mDimens.width;

        //tools created in reverse priority order
        //(Crop appears above Text, which appears above Overlays, etc)

        //MediaMetadataRetriever retriever;
        PrivacySettingTool privacySettingTool;
        CommentPrivacyTool commentPrivacyTool;
        CropTool cropTool;
        OverlaysTool overlaysTool;
        StickersTool stickersTool;
        TextTool textTool;


        switch (mPostOptions.type) {
            case UploadedPhoto:
            case Photo:
                switch (mPostOptions.subType) {
                    case Post:
                        overlaysTool = new OverlaysTool(mUri, mPostOptions.type, overlay, Glide.with(this));
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        cropTool = new CropTool(mUri, mPostOptions.type, overlay, (MoveZoomImageView) mContentView, mDimens, requestDisableToolListener, mContentView);
                        privacySettingTool = new PrivacySettingTool(mUri, mPostOptions.type, overlay, this);
                        return new EditContentTool[]{
                                privacySettingTool,
                                cropTool,
                                textTool,
                                stickersTool,
                                overlaysTool
                        };
                    case Chat:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        cropTool = new CropTool(mUri, mPostOptions.type, overlay, (MoveZoomImageView) mContentView, mDimens, requestDisableToolListener, mContentView);
                        return new EditContentTool[]{
                                textTool,
                                cropTool,
                                stickersTool
                        };
                    case Comment:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        cropTool = new CropTool(mUri, mPostOptions.type, overlay, (MoveZoomImageView) mContentView, mDimens, requestDisableToolListener, mContentView);
                        commentPrivacyTool = new CommentPrivacyTool(mUri, mPostOptions.type, overlay, this);
                        return new EditContentTool[]{
                                commentPrivacyTool,
                                cropTool,
                                textTool,
                                stickersTool
                        };
                    case Comment_No_Anon:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        cropTool = new CropTool(mUri, mPostOptions.type, overlay, (MoveZoomImageView) mContentView, mDimens, requestDisableToolListener, mContentView);
                        return new EditContentTool[]{
                                cropTool,
                                textTool,
                                stickersTool
                        };
                }
            case UploadedVideo:
            case Video:
                //retriever = new MediaMetadataRetriever();
                //retriever.setDataSource(mUri.getPath());
                switch (mPostOptions.subType) {
                    case Post:
                        overlaysTool = new OverlaysTool(mUri, mPostOptions.type, overlay, Glide.with(this));
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        return new EditContentTool[]{
                                new PrivacySettingTool(mUri, mPostOptions.type, overlay, this),
                                textTool,
                                stickersTool,
                                overlaysTool
                        };
                    case Chat:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        return new EditContentTool[]{
                                textTool,
                                stickersTool
                        };
                    case Comment:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        commentPrivacyTool = new CommentPrivacyTool(mUri, mPostOptions.type, overlay, this);
                        return new EditContentTool[]{
                                commentPrivacyTool,
                                textTool,
                                stickersTool
                        };
                    case Comment_No_Anon:
                        stickersTool = new StickersTool(mUri, mPostOptions.type, overlay, (ImageView) mToolbar.findViewById(R.id.image_sticker_trash));
                        textTool = new TextTool(mUri, mPostOptions.type, overlay, mDimens, this);
                        return new EditContentTool[]{
                                textTool,
                                stickersTool
                        };
                }
        }
        return new EditContentTool[0];
    }


    private void onDoneButtonPress() {
        ProcessingOptions options = new ProcessingOptions();
        for (EditContentTool tool : mTools) {
            tool.onClose();
        }

        for (EditContentTool tool : mTools) {
            tool.processContent(mUri, mPostOptions.type, options);
        }


        beginUpload(options);
    }

    private void beginUpload(ProcessingOptions options) {
        switch (mPostOptions.type) {
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

        if (show) {
            ProgressBar loaderView = new ProgressBar(getContext());
            mMenu.findItem(R.id.menu_item_done).setActionView(loaderView);

        } else {
            mMenu.findItem(R.id.menu_item_done).setActionView(null);

        }
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

        View inner = mFinalContentView.findViewById(R.id.inner_content);
        Bitmap bitmapFromView = ImageUtility.getBitmapFromView(inner == null ? mFinalContentView : inner);
        Bitmap bitmap;
        if (options.topInset != 0 || options.bottomInset != 0) {
            bitmap = Bitmap.createBitmap(bitmapFromView, 0, options.topInset, bitmapFromView.getWidth(), bitmapFromView.getHeight() - options.topInset - options.bottomInset);
            bitmapFromView.recycle();
        } else {
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
                                        .putExtra("isAnonymousCommentsDisabled", options.isAnonCommentsDisabled)
                                        .putExtra("title", options.text)
                                        .putExtra("stickers", options.stickers)
                                        .putExtra("filters", options.filters);
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
                                                options.isAnonCommentsDisabled ? 0 : 1,
                                                options.text,
                                                1,
                                                uri.toString(),
                                                null,
                                                options.stickers,
                                                options.filters,
                                                mUserId,
                                                mUserToken,
                                                mPostOptions.trendId
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

        if (mContentView != null && mContentView instanceof TextureVideoView) {
            ((TextureVideoView) mContentView).stopPlayback();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mFfmpeg != null) {
            mFfmpeg.killRunningProcesses();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            View focused = getActivity().getCurrentFocus();
            if (focused != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        }

        if (mTools != null) {
            for (EditContentTool tool : mTools)
                tool.onDestroy();
        }

        if (mContentBitmap != null) {
            mContentBitmap.recycle();
        }


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

        public void setSelected(boolean isSelected, boolean isDisabled) {
            if (isSelected) {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.secondaryColor));
                vIcon.setColorFilter(new
                        PorterDuffColorFilter(vIcon.getResources().getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
            } else if (isDisabled) {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.fifty_black));
                vIcon.setColorFilter(new
                        PorterDuffColorFilter(vIcon.getResources().getColor(R.color.fifty_black), PorterDuff.Mode.MULTIPLY));

            } else {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.edit_unselected));
                vIcon.setColorFilter(vIcon.getResources().getColor(R.color.edit_unselected));
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mPostOptions.type == ContentType.Video && mDimens.deleteVideoWhenFinished)
            ImageUtility.deleteCachedVideo(mUri);

        super.onDestroy();
    }

    interface Activatable {
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


        ((TextureVideoView) mContentView).stopPlayback();


        final String outputFile = ImageUtility.getVideoUri();
        showProgress(true);
        for (int i = 0; i < mIsDisabled.length; i++) {
            mIsDisabled[i] = true;
            toolHolders[i].setSelected(false, true);
        }

        //adds view to soak up all input on tool view
        View coverView = new View(getContext());
        coverView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        coverView.setBackgroundColor(0x44000000);
        coverView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mToolOptionsView.addView(coverView);


        mVideoProcessSubscription = Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(final Subscriber<? super Uri> subscriber) {
                ArrayList<String> cmd = new ArrayList<String>();
                cmd.add("-i");
                cmd.add(new File(mUri.getPath()).getAbsolutePath());
                cmd.add("-r");
                cmd.add("24");

                boolean widthIsGreater = mDimens.height < mDimens.width;
                String overlay;

                //TODO: smallest side will be 640
                //scale video, we don't want to process huge videos
                int newWidth;
                int newHeight;
                if (widthIsGreater) {
                    if (mDimens.height > 640) {
                        newHeight = 640;
                        newWidth = ((int) ((float) mDimens.width * newHeight / mDimens.height / 2)) * 2;
                    } else {
                        newWidth = mDimens.width;
                        newHeight = mDimens.height;
                    }
                } else {
                    if (mDimens.width > 640) {
                        newWidth = 640;
                        newHeight = ((int) ((float) newWidth * mDimens.height / mDimens.width / 2)) * 2;
                    } else {
                        newWidth = mDimens.width;
                        newHeight = mDimens.height;
                    }
                }


                // ffmpeg takes rotation into account now, so if the video has 90 degrees rotation
                // the videos width will be used as it's height and video height is it's width
                if (isPortrait()) {
                    int temp = newWidth;
                    newWidth = newHeight;
                    newHeight = temp;
                }

                // Log.i(TAG, "call: new " + newWidth + " " + newHeight);
                //Log.i(TAG, "call: old " + mDimens.width + " " + mDimens.height);
                //Log.i(TAG, "call: rotation " + mDimens.rotation);

                overlay = saveViewAsImage(mOverlaysContainer);

                //Log.i(TAG, "call:frame  " + mContentContainer.getHeight());
                //Log.i(TAG, "call: " + mTextView.getTop());

                if (overlay != null) {
                    cmd.add("-i");
                    cmd.add(overlay);
                    cmd.add("-filter_complex");

                    String temp = "";

                    temp += String.format(Locale.US,
                            "[0:v]scale=%d:%d[rot];", newWidth, newHeight);

                    if (mDimens.isFrontFacing) {
                        //rotate vid
                        temp += "[rot]hflip[tran];";
                    }

                    temp += String.format(Locale.US,
                            "[1:v]scale=%d:-1[over];", newWidth);

                    if (mDimens.needsCropping) {
                        if (!ScreenSizeSingleton.getSingleton().mHasRatioRequirement) {
                            //if was square, set dimen to square
                            if (newWidth > newHeight)
                                newWidth = newHeight;
                            else
                                newHeight = newWidth;
                        } else {
                            //set to 6:5 ratio
                            newHeight = (int) (newWidth * 1.2);
                        }

                        Log.i(TAG, "call: cropp" + newWidth + " " + newHeight);

                        temp += String.format(Locale.US,
                                "%scrop=%d:%d:0:0[crop1];",
                                mDimens.isFrontFacing ? "[tran]" : "[rot]",
                                newWidth, newHeight);

                        temp += "[crop1][over]overlay=0:0";
                    } else {
//                    //overlay
                        temp += String.format(Locale.US,
                                "%s[over]overlay=0:0", mDimens.isFrontFacing ? "[tran]" : "[rot]");
                    }

                    cmd.add(temp);
                } else {

                    if (mDimens.isFrontFacing) {
                        cmd.add("-filter_complex");
                        cmd.add(String.format(Locale.US,
                                "[0]scale=%d:%d[scaled];[scaled]hflip", newWidth, newHeight));//newWidth, newHeight));
                    } else {
                        cmd.add("-filter_complex");
                        cmd.add(String.format(Locale.US,
                                "scale=%d:%d", newWidth, newHeight)); //newWidth, newHeight));
                    }
                }
                //}

                cmd.add("-preset");
                cmd.add("superfast"); //ultrafast
                cmd.add("-c:a");
                cmd.add("copy");
                cmd.add(outputFile);

                try {
                    mFfmpeg.execute(cmd.toArray(new String[cmd.size()]), new FFmpegExecuteResponseHandler() {

                        long startTime = 0;

                        @Override
                        public void onSuccess(String message) {
                            //get first frame in video as thumbnail
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
                                    .putExtra("title", options.text)
                                    .putExtra("stickers", options.stickers)
                                    .putExtra("filters", options.filters);


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

//            //rotate the png so it can overlay correctly
//            Matrix m = new Matrix();
//            m.setRotate(360 - mDimens.rotation);

            Bitmap bm = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getWidth(), view.getHeight());
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
                options.isAnonCommentsDisabled ? 0 : 1,
//                mTextView.getText().toString(),
                options.text,
                2,
                imagepath,
                videopath,
                options.stickers,
                options.filters,
                mUserId,
                mUserToken,
                mPostOptions.trendId
        );

        showProgress(false);
        Intent result = new Intent();
        result.putExtra(PendingUploadPost.PENDING_POST_KEY, post);
        Toast.makeText(getActivity(), "Uploading video in background...", Toast.LENGTH_SHORT).show();

        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }

    public static interface RequestDisableToolListener {
        void requestDisable(Class<? extends EditContentTool> tool, boolean disable);
    }

}
