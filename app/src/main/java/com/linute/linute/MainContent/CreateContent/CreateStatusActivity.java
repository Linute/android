package com.linute.linute.MainContent.CreateContent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.bson.types.ObjectId;

public class CreateStatusActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = CreateStatusActivity.class.getSimpleName();

    private CustomBackPressedEditText mPostEditText;
    private TextView mTextView;
    private View mTextFrame;

    private View mPostButton;
    private View mProgressbar;

    private boolean mPostInProgress = false;

    private SwitchCompat vAnonPost;
    private SwitchCompat vAnonComments;

    private SharedPreferences mSharedPreferences;

    private int[] mPostBackgroundColors = new int[6];
    private int[] mPostTextColors = new int[6];
    private View[] mPostColorSelectorViews = new View[6];

    private int mCurrentlySelected = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_status);
        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.postContentToolbar);
        mPostButton = toolbar.findViewById(R.id.create_page_post_button);
        mProgressbar = toolbar.findViewById(R.id.create_page_progress_bar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProgressbar.getVisibility() == View.VISIBLE) return;
                hideKeyboard();
                if (mPostEditText.getText().toString().isEmpty()) {
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    showTextView();
                    showConfirmDialog();
                }
            }
        });

        toolbar.setTitle("Status");

        mPostEditText = (CustomBackPressedEditText) findViewById(R.id.post_create_text);
        mTextView = (TextView) findViewById(R.id.textView);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostEditText.hasFocus()) {
                    hideKeyboard();
                    mPostEditText.clearFocus();
                    if (!mPostEditText.getText().toString().isEmpty())
                        showTextView();
                } else {
                    postContent();
                }
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setVisibility(View.GONE);
                mPostEditText.setVisibility(View.VISIBLE);
                mPostEditText.requestFocus();
                showKeyboard();
            }
        });

        Typeface font = Typeface.createFromAsset(getAssets(), "Veneer.otf");
        mPostEditText.setTypeface(font);
        mTextView.setTypeface(font);

        mPostEditText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard();
                if (!mPostEditText.getText().toString().isEmpty()) {
                    showTextView();
                }
            }
        });

        mPostEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    if (!mPostEditText.getText().toString().isEmpty())
                        showTextView();
                }
                return false;
            }
        });

        mPostEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                toolbar.setVisibility(hasFocus ? View.GONE : View.VISIBLE);
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

        vAnonComments = (SwitchCompat) findViewById(R.id.anon_comments);
        vAnonPost = (SwitchCompat) findViewById(R.id.anon_post);

        mTextFrame = findViewById(R.id.post_create_frame);

        mPostColorSelectorViews[0] = findViewById(R.id.post_create_0);
        mPostColorSelectorViews[1] = findViewById(R.id.post_create_1);
        mPostColorSelectorViews[2] = findViewById(R.id.post_create_2);
        mPostColorSelectorViews[3] = findViewById(R.id.post_create_3);
        mPostColorSelectorViews[4] = findViewById(R.id.post_create_4);
        mPostColorSelectorViews[5] = findViewById(R.id.post_create_5);

        //wont let me generate signed apk if using same id
        SharedPreferences sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        setItem(R.id.selector_text0, 0, sharedPrefs, font);
        setItem(R.id.selector_text1, 1, sharedPrefs, font);
        setItem(R.id.selector_text2, 2, sharedPrefs, font);
        setItem(R.id.selector_text3, 3, sharedPrefs, font);
        setItem(R.id.selector_text4, 4, sharedPrefs, font);
        setItem(R.id.selector_text5, 5, sharedPrefs, font);


        //set to first color
        onClick(mPostColorSelectorViews[0]);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkPermission();
    }


    private void setItem(int res, int index, SharedPreferences preferences, Typeface typeface) {
        mPostTextColors[index] = preferences.getInt("status_color_" + index + "_text", 0xFF000000);
        mPostBackgroundColors[index] = preferences.getInt("status_color_" + index + "_bg", 0xFF000000);

        View postColorSelectorView = mPostColorSelectorViews[index];

        View view = postColorSelectorView.findViewById(res);
        view.getBackground().setColorFilter(mPostBackgroundColors[index], PorterDuff.Mode.SRC_ATOP);

        postColorSelectorView.setOnClickListener(this);
    }


    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("you sure?")
                .setMessage("would you like to throw away what you have currently?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    private void postContent() {
        if (mPostInProgress ||
                mPostEditText.getText().toString().trim().isEmpty())
            return;

        mPostInProgress = true;

        mProgressbar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);


        //save file to external if we have permission to access it
        //else save to cache
        Uri image = hasWritePermission() ?
                ImageUtility.savePicture(this, getBitmapFromView(mTextFrame)) :
                ImageUtility.savePictureToCache(this, getBitmapFromView(mTextFrame));


        if (image != null) {
            PendingUploadPost post =
                    new PendingUploadPost(
                            ObjectId.get().toString(),
                            mSharedPreferences.getString("collegeId", ""),
                            (vAnonPost.isChecked() ? 1 : 0),
                            vAnonComments.isChecked() ? 0 : 1,
                            mPostEditText.getText().toString(),
                            0,
                            image.toString(),
                            null,
                            mSharedPreferences.getString("userID", ""),
                            mSharedPreferences.getString("userToken", "")
                    );


            Intent result = new Intent();
            result.putExtra(PendingUploadPost.PENDING_POST_KEY, post);
            Toast.makeText(this, "Uploading status in background...", Toast.LENGTH_SHORT).show();

            setResult(Activity.RESULT_OK, result);

            finish();
            mPostInProgress = false;
        } else {
            Toast.makeText(this, "An error occurred while saving your status", Toast.LENGTH_SHORT).show();
            mProgressbar.setVisibility(View.GONE);
            mPostButton.setVisibility(View.VISIBLE);
            mPostInProgress = false;
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        int backgroundColor;
        int textColor;

        int selected = 0;
        switch (viewId) {
            case R.id.post_create_0:
                backgroundColor = mPostBackgroundColors[0];
                textColor = mPostTextColors[0];
                selected = 0;
                break;
            case R.id.post_create_1:
                backgroundColor = mPostBackgroundColors[1];
                textColor = mPostTextColors[1];
                selected = 1;
                break;
            case R.id.post_create_2:
                backgroundColor = mPostBackgroundColors[2];
                textColor = mPostTextColors[2];
                selected = 2;
                break;
            case R.id.post_create_3:
                backgroundColor = mPostBackgroundColors[3];
                textColor = mPostTextColors[3];
                selected = 3;
                break;
            case R.id.post_create_4:
                backgroundColor = mPostBackgroundColors[4];
                textColor = mPostTextColors[4];
                selected = 4;
                break;
            case R.id.post_create_5:
                backgroundColor = mPostBackgroundColors[5];
                textColor = mPostTextColors[5];
                selected = 5;
                break;
            default:
                backgroundColor = 0xFFFFFFFF;
                textColor = 0xFF000000;
                break;
        }

        //change text view and edit text colors
        mPostEditText.setTextColor(textColor);
        mPostEditText.setHintTextColor(ColorUtils.setAlphaComponent(textColor, 70)); //hint will be 70% of text color
        mTextView.setTextColor(textColor);
        mTextFrame.setBackgroundColor(backgroundColor);

        //set selected background
        mPostColorSelectorViews[mCurrentlySelected].setBackground(null);
        mPostColorSelectorViews[selected].setBackgroundResource(R.drawable.post_background);
        mCurrentlySelected = selected;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPostEditText.clearFocus();
        if (!mPostEditText.getText().toString().isEmpty())
            showTextView();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPostEditText.getWindowToken(), 0);
        mPostEditText.clearFocus(); //release focus from EditText and hide keyboard
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPostEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showTextView() {
        mTextView.setText(mPostEditText.getText().toString());
        mTextView.setVisibility(View.VISIBLE);
        mPostEditText.setVisibility(View.GONE);
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

        return Bitmap.createScaledBitmap(returnedBitmap, 720, 720, false);
    }


    public void checkPermission() {
        if (!hasWritePermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0
            );
        }
    }

    public boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

}
