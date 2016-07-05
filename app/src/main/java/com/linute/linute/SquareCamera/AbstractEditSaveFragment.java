package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.overlay.ManipulableImageView;
import com.linute.linute.SquareCamera.overlay.OverlayWipeAdapter;
import com.linute.linute.SquareCamera.overlay.StickerDrawerAdapter;
import com.linute.linute.SquareCamera.overlay.WipeViewPager;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

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
public abstract class AbstractEditSaveFragment extends Fragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    public static final String FROM_GALLERY = "from_gallery";

    protected ViewGroup mContentContainer;
    protected Toolbar mToolbar;

    protected CustomBackPressedEditText mEditText; //the edit text
    protected TextView mTextView;
    protected ProgressBar mProgressBar;
    protected CheckBox mAnonSwitch;
    protected CheckBox mAnonComments;

    protected String mCollegeId;
    protected String mUserId;

    protected View mUploadButton;

    protected CoordinatorLayout mStickerContainer;
    protected RecyclerView mStickerDrawer;

    protected int mReturnType;

    protected View vBottom;
    protected HasSoftKeySingleton mHasSoftKeySingleton;
    protected OverlayWipeAdapter mFilterAdapter;

    public static Fragment newInstance(Uri imageUri, boolean fromGallery) {
        Fragment fragment = new EditSavePhotoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putBoolean(FROM_GALLERY, fromGallery);

        fragment.setArguments(args);
        return fragment;
    }

    public AbstractEditSaveFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mReturnType = ((CameraActivity) getActivity()).getReturnType();
        mHasSoftKeySingleton = HasSoftKeySingleton.getmSoftKeySingleton(getActivity().getWindowManager());
        return inflater.inflate(R.layout.square_camera_edit_save, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");


        mContentContainer = (ViewGroup)view.findViewById(R.id.main_content);

        //shows the text strip when image touched

        mToolbar = (Toolbar) view.findViewById(R.id.top);
        mProgressBar = (ProgressBar) mToolbar.findViewById(R.id.editFragment_progress_bar);
        mUploadButton = mToolbar.findViewById(R.id.save_photo);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProgressBar.getVisibility() != View.VISIBLE)
                    showConfirmDialog();
            }
        });

        View stickerDrawerHandle = mToolbar.findViewById(R.id.sticker_drawer_handle);
        stickerDrawerHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStickerDrawer();
            }
        });



        mEditText = (CustomBackPressedEditText) view.findViewById(R.id.editFragment_title_text);
        mTextView = (TextView) view.findViewById(R.id.textView);

        vBottom = view.findViewById(R.id.bottom);
        mAnonComments = (CheckBox) vBottom.findViewById(R.id.anon_comments);
        mAnonSwitch = (CheckBox) vBottom.findViewById(R.id.editFragment_switch);
        if (mReturnType == CameraActivity.SEND_POST) {
            vBottom.findViewById(R.id.comments).setVisibility(View.VISIBLE);
            vBottom.findViewById(R.id.anon).setVisibility(View.VISIBLE);
        } else {
            vBottom.findViewById(R.id.anon).setVisibility(View.INVISIBLE);
            vBottom.findViewById(R.id.comments).setVisibility(View.INVISIBLE);
        }

        if (mHasSoftKeySingleton.getHasNavigation()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) vBottom.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin + mHasSoftKeySingleton.getBottomPixels());
            vBottom.setLayoutParams(params);
        }

        //save button
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadContent();
            }
        });



        //setup memes drawer
        mStickerContainer = (CoordinatorLayout)view.findViewById(R.id.stickers_container);

        mStickerDrawer = (RecyclerView)view.findViewById(R.id.sticker_drawer);
        mStickerDrawer.setLayoutManager(new GridLayoutManager(getContext(), 4));
        final StickerDrawerAdapter stickerDrawerAdapter = new StickerDrawerAdapter();
        mStickerDrawer.setAdapter(stickerDrawerAdapter);

        final ImageView stickerTrashCan = (ImageView)view.findViewById(R.id.sticker_trash);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();


        stickerDrawerAdapter.setStickerListener(new StickerDrawerAdapter.StickerListener() {
            @Override
            public void onStickerSelected(final Bitmap sticker) {
                ManipulableImageView stickerIV = new ManipulableImageView(getContext());
                stickerIV.setImageBitmap(sticker);
                stickerIV.setX(metrics.widthPixels/10);
                stickerIV.setY(metrics.heightPixels/10);


                stickerIV.setManipulationListener(new ManipulableImageView.ViewManipulationListener() {
                    @Override
                    public void onViewPickedUp(View me) {
                        mToolbar.setVisibility(View.GONE);
                        stickerTrashCan.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onViewDropped(View me) {
                        mToolbar.setVisibility(View.VISIBLE);
                        stickerTrashCan.setVisibility(View.GONE);
                    }

                    @Override
                    public void onViewCollision(View me) {
//                        stickerTrashCan.set
                    }

                    @Override
                    public void onViewDropCollision(View me) {
                        mStickerContainer.removeView(me);
                    }

                    @Override
                    public View getCollisionSensor() {
                        return stickerTrashCan;
                    }
                });


                mStickerContainer.addView(stickerIV);
                closeStickerDrawer();
            }
        });


        //setup Filters pager and adapter
        WipeViewPager filterPager = (WipeViewPager) view.findViewById(R.id.filter_overlay);
        mFilterAdapter = new OverlayWipeAdapter();

      /*  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap og = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    ColorMatrix bw = new ColorMatrix();
                    bw.setSaturation(0);
                    overlayAdapter.add(ImageUtility.applyFilter(og, bw));
                    ColorMatrix sat = new ColorMatrix();
                    sat.setSaturation(3);
                    overlayAdapter.add(ImageUtility.applyFilter(og, sat));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/

        filterPager.setWipeAdapter(mFilterAdapter);

        filterPager.setOnTouchListener(new View.OnTouchListener() {
            long timeDown = 0;
            int x, y;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    timeDown = System.currentTimeMillis();
                    x = (int) motionEvent.getRawX();
                    y = (int) motionEvent.getRawY();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - timeDown < 1500 && Math.abs(motionEvent.getRawX() - x) < 10 && Math.abs(motionEvent.getRawY() - y) < 10) {
                        if (mEditText.getVisibility() == View.GONE && mTextView.getVisibility() == View.GONE) {
                            mEditText.setVisibility(View.VISIBLE);
                            mEditText.requestFocus();
                            showKeyboard();
                            //mCanMove = false; //can't mvoe strip while in edit
                        }
                    }
                }
                return false;
            }
        });

        //load filters async
        final File filterDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filters/");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options measureOptions = new BitmapFactory.Options();
                measureOptions.inJustDecodeBounds = true;
                final BitmapFactory.Options options = new BitmapFactory.Options();
                for(File f: filterDir.listFiles()) {
                    Log.i("AAA", f.getAbsolutePath());
                    Bitmap b = null;
                    do {
                        b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                    }while(b == null);
                    float scale = (float)metrics.widthPixels/b.getWidth();

                    mFilterAdapter.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false),-1);
                    b.recycle();
                    Log.i("AAA", mFilterAdapter.getCount()+"");
                }
            }
        }).start();


        //load memes async
        final File memeDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                for(File f: memeDir.listFiles()) {
                    stickerDrawerAdapter.add(BitmapFactory.decodeFile(f.getAbsolutePath(), options));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stickerDrawerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();

        loadContent(mContentContainer);

        setUpEditText();
    }



    private void showConfirmDialog() {
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

    private void setUpEditText() {

        //when back is pressed
        mEditText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard();
                //if EditText is empty, hide it
                mEditText.setVisibility(View.GONE);
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
            int topMargin = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        prevY = event.getY();
                        totalMovement = 0;
                        if (bottomMargin == -1) {
                            if (mContentContainer.getHeight() >= mHasSoftKeySingleton.getSize().y) {
                                bottomMargin = mHasSoftKeySingleton.getBottomPixels();
                                topMargin = mUploadButton.getBottom();
                            } else {
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
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();

                        mTextMargin = params.topMargin + change; //new margintop

                        if (mTextMargin <= topMargin) { //over the top edge
                            mTextMargin = topMargin;
                        } else if (mTextMargin > mContentContainer.getHeight() - bottomMargin - v.getHeight()) { //under the bottom edge
                            mTextMargin = mContentContainer.getHeight() - bottomMargin - v.getHeight();
                        }

                        params.setMargins(0, mTextMargin, 0, 0); //set new margin
                        v.setLayoutParams(params);

                        break;
                }
                return true;
            }
        });
    }

    protected abstract void loadContent(ViewGroup container);

    protected abstract void uploadContent();

    protected void toggleStickerDrawer(){
        if(mStickerDrawer.isAnimating()) return;
        mStickerDrawer.setVisibility(mStickerDrawer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    protected void closeStickerDrawer(){
        mStickerDrawer.setVisibility(View.GONE);
    }



    protected void showKeyboard() { //show keyboard for EditText
        InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(mEditText, 0);
    }

    protected void hideKeyboard() {
        mEditText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContentContainer.getWindowToken(), 0);
    }


    protected void showProgress(final boolean show) {
        if (mReturnType == CameraActivity.SEND_POST) {
            vBottom.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
        mUploadButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    protected Socket mSocket;
    protected boolean mConnecting = false;

    @Override
    public void onResume() {
        super.onResume();

        if (mReturnType != CameraActivity.RETURN_URI) { //don't connect if we don't have to
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
                                        "&version=" + device.getVersionName() +
                                        "&build=" + device.getVersionCode() +
                                        "&os=" + device.getOS() +
                                        "&platform=" + device.getType() +
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFilterAdapter.destroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mReturnType != CameraActivity.RETURN_URI) {
            if (mSocket != null) {

                mSocket.disconnect();
                mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
                mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                mSocket.off(Socket.EVENT_ERROR, eventError);
                mSocket.off("new post", newPost);
            }
        }

        if (mEditText.getVisibility() == View.VISIBLE) {
            hideKeyboard();
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
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
            } catch (JSONException e) {
                Log.i(TAG, "call: error in newPost Listener");
            }
        }
    };

}
