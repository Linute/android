package com.linute.linute.SquareCamera;

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
import android.support.v4.app.FragmentActivity;
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

import com.linute.linute.R;
import com.linute.linute.SquareCamera.overlay.ManipulableImageView;
import com.linute.linute.SquareCamera.overlay.OverlayWipeAdapter;
import com.linute.linute.SquareCamera.overlay.StickerDrawerAdapter;
import com.linute.linute.SquareCamera.overlay.WipeViewPager;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.io.File;

/**
 *
 */
public abstract class AbstractEditSaveFragment extends Fragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    public static final String FROM_GALLERY = "from_gallery";

    protected View mAllContent;
    protected ViewGroup mContentContainer;
    protected Toolbar mToolbar;

    protected CustomBackPressedEditText mEditText; //the edit text
    protected TextView mTextView;
    protected ProgressBar mProgressBar;
    protected CheckBox mAnonSwitch;
    protected CheckBox mAnonComments;

    protected String mCollegeId;
    protected String mUserId;
    protected String mUserToken;

    protected View mUploadButton;
    protected View mStickerHandle;

    protected CoordinatorLayout mStickerContainer;
    protected RecyclerView mStickerDrawer;

    protected int mReturnType;

    protected View mOverlays;
    protected ViewGroup vBottom;
    protected HasSoftKeySingleton mHasSoftKeySingleton;
    protected OverlayWipeAdapter mFilterAdapter;
    private StickerDrawerAdapter mStickerDrawerAdapter;
    private WipeViewPager mFilterPager;

    protected boolean mFromGallery;

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
        mUserToken = sharedPreferences.getString("userToken", "");

        mAllContent = view.findViewById(R.id.final_content);
        mContentContainer = (ViewGroup) view.findViewById(R.id.base_content);
        mOverlays = view.findViewById(R.id.overlays);

        //shows the text strip when image touched

        mToolbar = (Toolbar) view.findViewById(R.id.top);
        mProgressBar = (ProgressBar) mToolbar.findViewById(R.id.editFragment_progress_bar);
        mUploadButton = mToolbar.findViewById(R.id.save_photo);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProgressBar.getVisibility() != View.VISIBLE)
                    backPressed();
            }
        });

        mStickerHandle = mToolbar.findViewById(R.id.sticker_drawer_handle);
        if (mFromGallery) {
            mStickerHandle.setVisibility(View.GONE);
        } else {
            mStickerHandle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isStickerDrawerOpen())
                        toggleStickerDrawer();
                }
            });
        }


        mEditText = (CustomBackPressedEditText) view.findViewById(R.id.editFragment_title_text);
        mTextView = (TextView) view.findViewById(R.id.textView);

        vBottom = (ViewGroup) view.findViewById(R.id.bottom);
        mAnonComments = (CheckBox) vBottom.findViewById(R.id.anon_comments);
        mAnonSwitch = (CheckBox) vBottom.findViewById(R.id.editFragment_switch);

        if (mReturnType == CameraActivity.SEND_POST) {
            vBottom.findViewById(R.id.comments).setVisibility(View.VISIBLE);
            vBottom.findViewById(R.id.anon).setVisibility(View.VISIBLE);
        } else if (mReturnType == CameraActivity.RETURN_URI_AND_PRIVACY) {
            vBottom.findViewById(R.id.comments).setVisibility(View.INVISIBLE);
        } else {
            vBottom.findViewById(R.id.anon).setVisibility(View.INVISIBLE);
            vBottom.findViewById(R.id.comments).setVisibility(View.INVISIBLE);
        }

        if (mAnonSwitch.getVisibility() == View.VISIBLE && getActivity() != null){
            mAnonSwitch.setChecked(getActivity().getIntent().getBooleanExtra(CameraActivity.ANON_KEY, false));
        }


        if (mHasSoftKeySingleton.getHasNavigation()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) vBottom.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin + mHasSoftKeySingleton.getBottomPixels());
            vBottom.setLayoutParams(params);
        }

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStickerDrawerOpen()) {
                    uploadContent();
                }
            }
        });

        //setup memes drawer
        mStickerContainer = (CoordinatorLayout) view.findViewById(R.id.stickers_container);

        mStickerDrawer = (RecyclerView) view.findViewById(R.id.sticker_drawer);
        mStickerDrawer.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mStickerDrawerAdapter = new StickerDrawerAdapter();
        mStickerDrawer.setAdapter(mStickerDrawerAdapter);

        final ImageView stickerTrashCan = (ImageView) view.findViewById(R.id.sticker_trash);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        //setup Filters pager and adapter
        mFilterPager = (WipeViewPager) view.findViewById(R.id.filter_overlay);

        if (!mFromGallery) {
            mStickerDrawerAdapter.setStickerListener(new StickerDrawerAdapter.StickerListener() {
                @Override
                public void onStickerSelected(final Bitmap sticker) {
                    ManipulableImageView stickerLayout = new ManipulableImageView(getContext());
                    stickerLayout.setImageBitmap(sticker);
                    stickerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                    stickerLayout.setX(metrics.widthPixels / 10);
//                    stickerLayout.setY(metrics.heightPixels / 10);


                    stickerLayout.setManipulationListener(new ManipulableImageView.ViewManipulationListener() {
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
                        public void onViewCollisionBegin(View me) {
                            stickerTrashCan.setImageResource(R.drawable.trash_can_open);
                        }

                        @Override
                        public void onViewCollisionEnd(View me) {
                            stickerTrashCan.setImageResource(R.drawable.trash_can_closed);
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


                    mStickerContainer.addView(stickerLayout);
                    closeStickerDrawer();
                }
            });


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

            mFilterPager.setWipeAdapter(mFilterAdapter);

            mFilterPager.setOnTouchListener(new View.OnTouchListener() {
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
                            toggleEditText();
                        }
                    }
                    return false;
                }
            });

        } else {
            mFilterPager.setVisibility(View.GONE);

            mAllContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleEditText();
                }
            });
        }

        if (!overlaysLoaded && !mFromGallery)
            loadOverlays();

        loadContent(mContentContainer);
        setUpEditText();
    }


    public void toggleEditText() {
        if (mEditText.getVisibility() == View.GONE && mTextView.getVisibility() == View.GONE) {
            mEditText.setVisibility(View.VISIBLE);
            mEditText.requestFocus();
            showKeyboard();
            //mCanMove = false; //can't mvoe strip while in edit
        } else if (mEditText.getVisibility() == View.VISIBLE) {
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
            mToolbar.setVisibility(View.VISIBLE);
            hideKeyboard();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFromGallery = getArguments().getBoolean(FROM_GALLERY);
        }

        //load filters async
        if (!overlaysLoaded && !mFromGallery)
            loadOverlays();
    }

    private boolean overlaysLoaded;

    private void loadOverlays() {
        final FragmentActivity activity = getActivity();
        if (mStickerDrawerAdapter == null || mFilterAdapter == null || activity == null) return;

        overlaysLoaded = true;

        final DisplayMetrics metrics = getResources().getDisplayMetrics();


        final File filterDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filters/");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options measureOptions = new BitmapFactory.Options();
                measureOptions.inJustDecodeBounds = true;
                final BitmapFactory.Options options = new BitmapFactory.Options();
                File[] filters = filterDir.listFiles();
                if (filters != null)
                    for (File f : filters) {
                        Bitmap b = null;
                        Bitmap scaled = null;
                        b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                        if (b != null) {
                            float scale = (float) metrics.widthPixels / b.getWidth();

                            scaled = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * scale), (int) (b.getHeight() * scale), false);
                            try {
                                mFilterAdapter.add(scaled);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            } catch (NullPointerException np) {
                                np.printStackTrace();
                            } finally {
                                //It turns out the original image may be passed back as an optimisation,
                                // if the width/height of the resize match the original image
                                if (b != scaled) {
                                    b.recycle();
                                }
                            }

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mFilterPager.invalidate();
                                }
                            });
                        }
                    }

            }
        }).start();


        //load memes async
        final File memeDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                File[] memes = memeDir.listFiles();
                if (memes != null)
                    for (File f : memes) {
                        try {
                            mStickerDrawerAdapter.add(BitmapFactory.decodeFile(f.getAbsolutePath(), options));
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mStickerDrawerAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                        } catch (NullPointerException np) {
                            np.printStackTrace();
                        }
                    }
            }

        }).start();
    }

    protected void showConfirmDialog() {
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


        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mToolbar.setVisibility(hasFocus ? View.INVISIBLE : View.VISIBLE);
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
                        //Log.i(TAG, "onTouch: " + mAllContent.getHeight() + " " + mAllContent.getWidth());
                        prevY = event.getY();
                        totalMovement = 0;
                        if (bottomMargin == -1) {
                            if (mContentContainer.getHeight() >= mHasSoftKeySingleton.getSize().y) {
                                bottomMargin = mHasSoftKeySingleton.getBottomPixels();
                                topMargin = mToolbar.getBottom();
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
                        } else if (mTextMargin > mContentContainer.getHeight() + mContentContainer.getTop() - bottomMargin - v.getHeight()) { //under the bottom edge
                            mTextMargin = mContentContainer.getHeight() + mContentContainer.getTop() - bottomMargin - v.getHeight();
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

    protected abstract void showProgress(boolean show);

    protected abstract void backPressed();

    public boolean isStickerDrawerOpen() {
        return mStickerDrawer.getVisibility() == View.VISIBLE;
    }

    protected void toggleStickerDrawer() {
        if (mStickerDrawer.isAnimating()) return;
        if (isStickerDrawerOpen()) {
            closeStickerDrawer();
        } else {
            openStickerDrawer();
        }
//        mStickerDrawer.setVisibility(mStickerDrawer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    protected void openStickerDrawer() {
        if (mFromGallery) return;
        mStickerDrawer.setVisibility(View.VISIBLE);
        mUploadButton.setAlpha(.3f);
        if (!mFromGallery) mStickerHandle.setAlpha(.3f);
    }

    protected void closeStickerDrawer() {
        mStickerDrawer.setVisibility(View.GONE);
        mUploadButton.setAlpha(1);
        if (!mFromGallery) mStickerHandle.setAlpha(1);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFilterAdapter != null) mFilterAdapter.destroy();
        if (mStickerDrawerAdapter != null) mStickerDrawerAdapter.destroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mEditText.getVisibility() == View.VISIBLE) {
            hideKeyboard();
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}
