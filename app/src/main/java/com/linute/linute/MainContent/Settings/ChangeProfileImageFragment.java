package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.bumptech.glide.util.Util;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.LoginAndSignup.SignUpFragments.SignUpProfilePicture;
import com.linute.linute.ProfileCamera.ProfileCameraActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangeProfileImageFragment extends Fragment {

    public static final String TAG = ChangeProfileImageFragment.class.getSimpleName();

    private boolean mHasChangedImage = false; //won't allow send unless user actually makes a change

    private TextView mEditButton;
    private TextView mSaveButton;
    private View mButtonLayer;

    private ImageView mImageView;
    private SharedPreferences mSharedPreferences;
    private ProgressBar mProgressBar;
    private Uri mImageUri;


    CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_profile_image, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        bindViews(rootView);
        setDefaultValues();
        setUpOnClickListeners();

        ((EditProfileInfoActivity) getActivity()).setTitle("Photo");

        return rootView;
    }

    private void bindViews(View rootView) {
        mSaveButton = (TextView) rootView.findViewById(R.id.changeprofileimage_save_button);
        mEditButton = (TextView) rootView.findViewById(R.id.changeprofileimage_change_button);
        mButtonLayer = rootView.findViewById(R.id.changeprofileimage_buttons);

        mImageView = (ImageView) rootView.findViewById(R.id.changeprofileimage_image);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.changeprofileimage_progressbar);
    }

    private void setUpOnClickListeners() {
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Image Source");
                String[] options = {"Camera", "Photo Gallery", "Import from Facebook"};
                builder.setItems(options, actionListener);
                builder.create().show();
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHasChangedImage || getActivity() == null)
                    return; //no edits to image

                showProgress(true);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        saveImage();
                    }
                });
            }
        });
    }


    //select between camera and photogallery
    DialogInterface.OnClickListener actionListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int type;
            int request;
            switch (which) {
                case 0:
                    type = ProfileCameraActivity.TYPE_CAMERA;
                    request = REQUEST_TAKE_PHOTO;
                    break;
                case 1:
                    type = ProfileCameraActivity.TYPE_GALLERY;
                    request = REQUEST_GALLERY;
                    break;
                case 2:
                    clickedFacebook();
                    return;
                default:
                    return;
            }

            Intent i = new Intent(getActivity(), ProfileCameraActivity.class);
            i.putExtra(ProfileCameraActivity.TYPE_KEY, type);
            startActivityForResult(i, request);
        }
    };


    private void clickedFacebook(){
        if (AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired()){
            loadFbImage(AccessToken.getCurrentAccessToken().getUserId());
        }else {
            setUpFacebookCallback();
            LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("public_profile"));
        }
    }


    private void setUpFacebookCallback(){
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "Facebook access denied", Toast.LENGTH_SHORT).show();

                    return;
                }

                loadFbImage(loginResult.getAccessToken().getUserId());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: "+error.toString());
                if (getActivity() != null)
                    Toast.makeText(getActivity(), "Error communicating with Facebook server", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadFbImage(String userId){
        mHasChangedImage = true;
        mImageUri = Uri.parse(Utils.getFBImage(userId));
        Glide.with(ChangeProfileImageFragment.this)
                .load(mImageUri)
                .asBitmap()
                .placeholder(R.color.seperator_color)
                .into(mImageView);
    }

    private void setDefaultValues() {
        Glide.with(this)
                .load(Utils.getImageUrlOfUser(mSharedPreferences.getString("profileImage", "")))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(mImageView);
    }


    private void saveImage() {

        Map<String, Object> userInfo = new HashMap<>();

        if (mImageUri != null && mImageUri.toString().contains("facebook.com")){
            try {
                userInfo.put("profileImage", Utils.encodeImageBase64(
                        BitmapFactory.decodeFile(
                                Glide.with(this).load(mImageUri).downloadOnly(720, 720).get().getAbsolutePath()
                        )
                ));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }else {
            try {
                userInfo.put("profileImage",
                        Utils.encodeImageBase64(
                                Bitmap.createScaledBitmap(
                                        MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri),
                                        1080, 1080, false)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!userInfo.containsKey("profileImage")){
            Bitmap map = mImageView.getDrawingCache();
            userInfo.put("profileImage", Utils.encodeImageBase64(map));
        }

        if (getActivity() == null)
            return;

        new LSDKUser(getActivity()).updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                        persistData(user);

                        mHasChangedImage = false;

                        final EditProfileInfoActivity activity = (EditProfileInfoActivity) getActivity();
                        if (activity == null) return;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(activity);
                                showProgress(false);

                                activity.setMainActivityNeedsToUpdate(true);
                                getFragmentManager().popBackStack();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                showProgress(false);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            showProgress(false);
                        }
                    });
                }
            }
        });
    }


    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_GALLERY = 111;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_GALLERY)
                && resultCode == Activity.RESULT_OK) { //got response from camera
            mImageUri = data.getData();
            if (mImageUri != null) {
                Glide.with(this)
                        .load(mImageUri)
                        .asBitmap()
                        .placeholder(android.R.color.black)
                        .dontAnimate()
                        .into(mImageView);

                mHasChangedImage = true;
            }
        }else {
            mCallbackManager.onActivityResult(requestCode,resultCode, data);
        }
    }


    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("profileImage", user.getProfileImage()).apply();
        mSharedPreferences.edit().putString("imageSigniture", new Random().nextInt() + "").apply();
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

        setFocusable(!show);
    }

    private void setFocusable(boolean focusable) {
        mImageView.setClickable(focusable);
    }

}

