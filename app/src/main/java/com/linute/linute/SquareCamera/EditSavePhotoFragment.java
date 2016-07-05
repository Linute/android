package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.MinimumWidthImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class EditSavePhotoFragment extends AbstractEditSaveFragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    public static final String FROM_GALLERY = "from_gallery";


    public static Fragment newInstance(Uri imageUri) {
        Fragment fragment = new EditSavePhotoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(Uri imageUri, boolean fromGallery) {
        Fragment fragment = new EditSavePhotoFragment();

        Bundle args = new Bundle();

        if (imageUri != null)
            args.putParcelable(BITMAP_URI, imageUri);

        args.putBoolean(FROM_GALLERY, fromGallery);

        fragment.setArguments(args);
        return fragment;
    }

    protected void showProgress(final boolean show) {
        if (mReturnType == CameraActivity.SEND_POST) {
            vBottom.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
        mUploadButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void loadContent(ViewGroup container) {
        //setup ImageView
        final Uri imageUri = getArguments().getParcelable(BITMAP_URI);
        boolean fromGallery = getArguments().getBoolean(FROM_GALLERY, false);
        final ImageView photoImageView = new MinimumWidthImageView(container.getContext());
        photoImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        photoImageView.setAdjustViewBounds(true);
        photoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photoImageView.setBackgroundColor(getResources().getColor(R.color.pure_black));

        if (fromGallery) {
            Glide.with(this)
                    .load(imageUri)
                    .dontAnimate()
                    .into(photoImageView);
        } else {
            //when taken from our camera, no need to resize
            //no lag time when loading with this method
            photoImageView.setImageURI(imageUri);
        }
        container.addView(photoImageView);

    }

    protected void uploadContent() {

        if (getActivity() == null) return;

        if (mEditText.getVisibility() == View.VISIBLE) {
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
            hideKeyboard();
            return;
        }


        if (mReturnType == CameraActivity.SEND_POST && (!Utils.isNetworkAvailable(getActivity()) || !mSocket.connected())) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        Bitmap bitmap = ImageUtility.getBitmapFromView(mContentContainer);

        showProgress(true);
        if (getActivity() == null) return;
        if (mReturnType == CameraActivity.RETURN_URI) {
            Uri image = ImageUtility.savePictureToCache(getActivity(), bitmap);
            if (image != null) {
                Intent i = new Intent()
                        .putExtra("image", image)
                        .putExtra("type", CameraActivity.IMAGE)
                        .putExtra("title", mEditText.getText().toString());
                getActivity().setResult(Activity.RESULT_OK, i);
                getActivity().finish();
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        } else {
            try {
                JSONObject postData = new JSONObject();

                postData.put("college", mCollegeId);
                postData.put("privacy", (mAnonSwitch.isChecked() ? 1 : 0) + "");
                postData.put("isAnonymousCommentsDisabled", mAnonComments.isChecked() ? 0 : 1);
                postData.put("title", mEditText.getText().toString());
                JSONArray imageArray = new JSONArray();
                imageArray.put(Utils.encodeImageBase64(bitmap));
                postData.put("images", imageArray);
                postData.put("type", "1");
                postData.put("owner", mUserId);


                JSONArray coord = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                coord.put(0);
                coord.put(0);
                jsonObject.put("coordinates", coord);

                postData.put("geo", jsonObject);

                mSocket.emit(API_Methods.VERSION + ":posts:new post", postData);

            } catch (JSONException e) {
                e.printStackTrace();
                Utils.showServerErrorToast(getActivity());
                showProgress(false);
            }
        }
    }

}
