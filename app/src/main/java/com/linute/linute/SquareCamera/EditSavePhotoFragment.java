package com.linute.linute.SquareCamera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class EditSavePhotoFragment extends Fragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    //public static final String ROTATION_KEY = "rotation";
    public static final String IMAGE_INFO = "image_info";


    private View mFrame; //frame where we put edittext and picture

    private EditSaveEditText mText; //text
    private ProgressBar mProgressBar;
    private View mButtonLayer;
    private Switch mAnonSwitch;


    public static Fragment newInstance(Uri imageUri,
                                       @NonNull ImageParameters parameters) {
        Fragment fragment = new EditSavePhotoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putParcelable(IMAGE_INFO, parameters);

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

        ImageParameters imageParameters = getArguments().getParcelable(IMAGE_INFO);
        if (imageParameters == null) {
            return;
        }

        //setup ImageView
        Uri imageUri = getArguments().getParcelable(BITMAP_URI);
        final ImageView photoImageView = (ImageView) view.findViewById(R.id.photo);
        
        photoImageView.setImageURI(imageUri);

        imageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //shows the text strip when image touched
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mText.getVisibility() == View.GONE){
                    mText.setVisibility(View.VISIBLE);
                    mText.requestFocus();
                    showKeyboard();
                    mCanMove = false; //can't mvoe strip while in edit
                }
            }
        });

        //so image is consistent with camera view
        final View topView = view.findViewById(R.id.topView);
        if (imageParameters.mIsPortrait) {
            topView.getLayoutParams().height = imageParameters.mCoverHeight;
        } else {
            topView.getLayoutParams().width = imageParameters.mCoverWidth;
        }

        mFrame = view.findViewById(R.id.frame); //frame where we put edittext and picture
        mText = (EditSaveEditText) view.findViewById(R.id.editFragment_title_text);
        mButtonLayer = view.findViewById(R.id.editFragment_button_layer);
        mProgressBar = (ProgressBar) view.findViewById(R.id.editFragment_progress_bar);
        mAnonSwitch = (Switch) view.findViewById(R.id.editFragment_switch);

        //save button
        view.findViewById(R.id.save_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePicture();
            }
        });

        view.findViewById((R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraActivity)getActivity()).clearBackStack();
            }
        });

        setUpEditText();
    }

    //text strip can move or not
    //can't move during edit
    private boolean mCanMove = true;

    private void setUpEditText(){

        //when back is pressed
        mText.setBackAction(new BackButtonAction() {
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
                            }

                            else if (newTop >  mFrame.getHeight() - v.getHeight()){ //under the bottom edge
                                newTop = mFrame.getHeight() - v.getHeight();
                                stopped = true;
                            }

                            params.setMargins(0, newTop,0,0); //set new margin
                            v.setLayoutParams(params);
                        }
                        break;
                }

                return true;
            }

        });
    }

    private void savePicture() {

        Bitmap bitmap = Bitmap.createScaledBitmap(getBitmapFromView(mFrame), 1080, 1080, true);

        JSONArray coord = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONArray images = new JSONArray();
        try {
            coord.put(0);
            coord.put(0);
            jsonObject.put("coordinates", coord);
            images.put(Utils.encodeImageBase64(bitmap));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Map<String, Object> postData = new HashMap<>();
        postData.put("college", "564a46ff8ac4a559174247af"); //TODO: FIX COLLEGE
        postData.put("privacy", (mAnonSwitch.isChecked() ? 1 : 0) + "");
        postData.put("images", images);
        postData.put("title", mText.getText().toString()); //TODO: What if empty?
        postData.put("geo", jsonObject);

        showProgress(true);

        new LSDKEvents(getActivity()).postEvent(postData, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                if (response.isSuccessful()){
                    getActivity().finish();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Picture Posted", Toast.LENGTH_SHORT).show();
                            try {
                                Log.i(TAG, "run: "+response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    showServerError();
                    Log.e(TAG, "onResponse: "+response.code()+" : "+response.body().string() );
                }
            }
        });

    }

    private void showServerError(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(getActivity());
                showProgress(false);
            }
        });
    }

    private void showKeyboard(){ //show keyboard for EditText
        InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(mText, 0);
    }

    private void hideKeyboard(){
        mText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFrame.getWindowToken(), 0);
    }

    //cuts a bitmap from our RelativeLayout
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }


    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        mButtonLayer.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    public static class EditSaveEditText extends EditText{

        BackButtonAction mBackAction;

        public EditSaveEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // User has pressed Back key. So hide the keyboard
                mBackAction.backPressed();
            }
            return false;
        }

        public void setBackAction(BackButtonAction action) {
            mBackAction = action;
        }
    }

    interface BackButtonAction {
        public void backPressed();
    }
}
