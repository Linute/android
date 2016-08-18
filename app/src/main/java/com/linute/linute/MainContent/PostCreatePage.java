package com.linute.linute.MainContent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.bson.types.ObjectId;

import static android.app.Activity.RESULT_CANCELED;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class PostCreatePage extends BaseFragment implements View.OnClickListener {
    public static final String TAG = PostCreatePage.class.getSimpleName();

    private CustomBackPressedEditText mPostEditText;
    private TextView mTextView;
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

    private int[] mPostBackgroundColors = new int[6];
    private int[] mPostTextColors = new int[6];
    private View[] mPostColorSelectorViews = new View[6];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_new_post_create, container, false);

        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.postContentToolbar);
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
                    showTextView();
                    showConfirmDialog();
                }
            }
        });

        toolbar.setTitle("Status");

        mPostEditText = (CustomBackPressedEditText) root.findViewById(R.id.post_create_text);
        mTextView = (TextView) root.findViewById(R.id.textView);
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

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
                "Veneer.otf");
        mPostEditText.setTypeface(font);
        mTextView.setTypeface(font);

        mPostEditText.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard();
                if (!mPostEditText.getText().toString().isEmpty()){
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

        vAnonComments = (CheckBox) root.findViewById(R.id.anon_comments);
        vAnonPost = (CheckBox) root.findViewById(R.id.anon_post);

        mTextFrame = root.findViewById(R.id.post_create_frame);

        mPostColorSelectorViews[0] = root.findViewById(R.id.post_create_0);
        mPostColorSelectorViews[1] = root.findViewById(R.id.post_create_1);
        mPostColorSelectorViews[2] = root.findViewById(R.id.post_create_2);
        mPostColorSelectorViews[3] = root.findViewById(R.id.post_create_3);
        mPostColorSelectorViews[4] = root.findViewById(R.id.post_create_4);
        mPostColorSelectorViews[5] = root.findViewById(R.id.post_create_5);

        //wont let me generate signed apk if using same id
        SharedPreferences sharedPrefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        setItem(R.id.selector_text0, 0, sharedPrefs, font);
        setItem(R.id.selector_text1, 1, sharedPrefs, font);
        setItem(R.id.selector_text2, 2, sharedPrefs, font);
        setItem(R.id.selector_text3, 3, sharedPrefs, font);
        setItem(R.id.selector_text4, 4, sharedPrefs, font);
        setItem(R.id.selector_text5, 5, sharedPrefs, font);

        onClick(mPostColorSelectorViews[0]);

        return root;
    }

    private void setItem(int res, int index, SharedPreferences preferences, Typeface typeface) {
        mPostTextColors[index] = preferences.getInt("status_color_" + index + "_text", 0xFF000000);
        mPostBackgroundColors[index] = preferences.getInt("status_color_" + index + "_bg", 0xFF000000);

        View postColorSelectorView = mPostColorSelectorViews[index];
        postColorSelectorView.getBackground().setColorFilter(mPostBackgroundColors[index], PorterDuff.Mode.SRC_ATOP);

        TextView text = (TextView) postColorSelectorView.findViewById(res);
        text.setTextColor(mPostTextColors[index]);
        text.setTypeface(typeface);

        postColorSelectorView.setOnClickListener(this);
    }


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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void postContent() {
        if (getActivity() == null ||
                mPostInProgress ||
                mPostEditText.getText().toString().trim().isEmpty())
            return;

        mPostInProgress = true;

        mProgressbar.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.INVISIBLE);


        Uri image = ImageUtility.savePicture(getActivity(), getBitmapFromView(mTextFrame));
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
            Toast.makeText(getActivity(), "Uploading status in background...", Toast.LENGTH_SHORT).show();

            getActivity().setResult(Activity.RESULT_OK, result);

            getActivity().finish();
            mPostInProgress = false;
        } else {
            Toast.makeText(getActivity(), "An error occurred while saving your status", Toast.LENGTH_SHORT).show();
            mProgressbar.setVisibility(View.GONE);
            mPostButton.setVisibility(View.VISIBLE);
            mPostInProgress = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (getActivity() == null) return;

        int viewId = v.getId();

        int backgroundColor;
        int textColor;

        switch (viewId) {
            case R.id.post_create_0:
                backgroundColor = mPostBackgroundColors[0];
                textColor = mPostTextColors[0];
                break;
            case R.id.post_create_1:
                backgroundColor = mPostBackgroundColors[1];
                textColor = mPostTextColors[1];
                break;
            case R.id.post_create_2:
                backgroundColor = mPostBackgroundColors[2];
                textColor = mPostTextColors[2];
                break;
            case R.id.post_create_3:
                backgroundColor = mPostBackgroundColors[3];
                textColor = mPostTextColors[3];
                break;
            case R.id.post_create_4:
                backgroundColor = mPostBackgroundColors[4];
                textColor = mPostTextColors[4];
                break;
            case R.id.post_create_5:
                backgroundColor = mPostBackgroundColors[5];
                textColor = mPostTextColors[5];
                break;
            default:
                backgroundColor = 0xFFFFFFFF;
                textColor = 0xFF000000;
                break;
        }


        mPostEditText.setTextColor(textColor);
        mPostEditText.setHintTextColor(ColorUtils.setAlphaComponent(textColor, 70));
        mTextView.setTextColor(textColor);
        mTextFrame.setBackgroundColor(backgroundColor);
    }


    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPostEditText.getWindowToken(), 0);
        mPostEditText.clearFocus(); //release focus from EditText and hide keyboard
    }

    private void showKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
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

}
