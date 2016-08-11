package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.ProfileCamera.ProfileCameraActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 7/31/16.
 */
public class SignUpProfilePicture extends Fragment implements DialogInterface.OnClickListener {

    public static final String TAG = SignUpProfilePicture.class.getSimpleName();
    private ImageView vProfileImage;
    private SignUpInfo mSignUpInfo;
    private Button vButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_profile, container, false);

        vProfileImage = (ImageView) root.findViewById(R.id.profile_image);
        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();

        vButton = (Button) root.findViewById(R.id.create);

        if (mSignUpInfo.getImage() != null) {
            Glide.with(this)
                    .load(mSignUpInfo.getImage())
                    .placeholder(R.color.seperator_color)
                    .into(vProfileImage);

            activateButton(true);
        } else {
            activateButton(false);
        }

        vProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraChoices();
            }
        });

        root.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraChoices();
            }
        });

        vButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        return root;
    }


    private void startCameraChoices() {
        if (getActivity() == null) return;
        String[] options = {"Camera", "Photo Gallery", "Import from Facebook"};
        new AlertDialog.Builder(getActivity())
                .setItems(options, this)
                .show();
    }

    private static final int REQUEST_PHOTO = 34;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                startCamera(ProfileCameraActivity.TYPE_CAMERA);
                break;
            case 1:
                startCamera(ProfileCameraActivity.TYPE_GALLERY);
                break;
            case 2:
                getFacebookProfileImage();
                break;
            default:
                break;
        }
    }


    private void startCamera(int type) {
        Intent i = new Intent(getActivity(), ProfileCameraActivity.class);
        i.putExtra(ProfileCameraActivity.TYPE_KEY, type);
        startActivityForResult(i, REQUEST_PHOTO);
    }

    private void getFacebookProfileImage() {
        PreLoginActivity activity = (PreLoginActivity) getActivity();
        if (activity != null) {
            activity.getFBProfileImage(new PreLoginActivity.GetPhotoCallback() {
                @Override
                public void success(String id) {
                    Uri image = Uri.parse(Utils.getFBImage(id));
                    mSignUpInfo.setImage(image);

                    Glide.with(SignUpProfilePicture.this)
                            .load(image)
                            .asBitmap()
                            .placeholder(R.color.seperator_color)
                            .into(vProfileImage);

                    activateButton(true);
                }

                @Override
                public void error() {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error retrieving Facebook photo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void createAccount() {
        if (getContext() == null) return;

        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "Creating account...", true, false);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sendInfoToServer(getParams(), dialog);
            }
        });
    }


    private HashMap<String, Object> getParams() {
        HashMap<String, Object> info = new HashMap<>();

        info.put("email", mSignUpInfo.getEmail());
        info.put("password", mSignUpInfo.getPassword());
        info.put("firstName", mSignUpInfo.getFirstName());
        info.put("lastName", mSignUpInfo.getLastName());
        info.put("timeZone", Utils.getTimeZone());
        info.put("college", mSignUpInfo.getCollege().getCollegeId());

        if (mSignUpInfo instanceof FBSignUpInfo) {
            FBSignUpInfo fbSignUpInfo = (FBSignUpInfo) mSignUpInfo;
            info.put("socialFacebook", fbSignUpInfo.getSocialFB());
            info.put("sex", fbSignUpInfo.getSex());
            info.put("registrationType", fbSignUpInfo.getRegistrationType());

            String dob = fbSignUpInfo.getDob();
            if (!dob.isEmpty() && !dob.equals("null")) {
                info.put("dob", dob);
            }
        }

        try {
            if (mSignUpInfo.getImage() != null) {
                if (mSignUpInfo.getImage().toString().contains("facebook.com")) {
                    info.put("profileImage", Utils.encodeImageBase64(
                            BitmapFactory.decodeFile(
                                    Glide.with(this).load(mSignUpInfo.getImage()).downloadOnly(720, 720).get().getAbsolutePath()
                            )
                    ));
                } else {
                    info.put("profileImage",
                            Utils.encodeImageBase64(
                                    Bitmap.createScaledBitmap(
                                            MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mSignUpInfo.getImage()),
                                            720,
                                            720,
                                            false)));
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        return info;
    }


    private void sendInfoToServer(HashMap<String, Object> params, final ProgressDialog dialog) {
        if (getActivity() == null) return;

        new LSDKUser(getActivity()).createUser(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        PreLoginActivity activity = (PreLoginActivity) getActivity();
                        if (activity != null) {
                            persistData(new LinuteUser(new JSONObject(responseString)), activity); //save data
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });
                            activity.goToNextActivity();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }


                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            }
        });
    }


    private void persistData(LinuteUser user, PreLoginActivity activity) {
        SharedPreferences.Editor sharedPreferences = activity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("status", user.getStatus());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("phone", user.getPhone());
        sharedPreferences.putString("collegeName", user.getCollegeName());
        sharedPreferences.putString("collegeId", user.getCollegeId());
        sharedPreferences.putString("campus", user.getCampus());
        sharedPreferences.putString("socialFacebook", user.getSocialFacebook());
        sharedPreferences.putString("email", user.getEmail());

        sharedPreferences.putString("userToken", user.getUserToken());
        sharedPreferences.putString("userName", user.getUserName());
        sharedPreferences.putString("points", user.getPoints());

        sharedPreferences.putBoolean("isLoggedIn", true);

        sharedPreferences.putBoolean("notif_follow", true);
        sharedPreferences.putBoolean("notif_message", true);
        sharedPreferences.putBoolean("notif_mention", true);
        sharedPreferences.putBoolean("notif_alsoComment", true);
        sharedPreferences.putBoolean("notif_comment", true);
        sharedPreferences.putBoolean("notif_like", true);
        sharedPreferences.apply();
    }


    private void activateButton(boolean act) {
        if (act) {
            vButton.setBackgroundResource(R.drawable.active_button);
            vButton.setTextColor(ContextCompat.getColor(vButton.getContext(), R.color.pure_white));
            vButton.setText("Finish");
        } else {
            vButton.setBackgroundResource(R.drawable.inactive_button);
            vButton.setTextColor(ContextCompat.getColor(vButton.getContext(), R.color.secondaryColor));
            vButton.setText("Skip");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_PHOTO) && resultCode == Activity.RESULT_OK) { //photo came back from crop
            Uri image = data.getData();
            if (getActivity() == null || image == null) return;
            mSignUpInfo.setImage(image);
            Glide.with(this)
                    .load(image)
                    .into(vProfileImage);
            activateButton(true);
        }
    }
}
