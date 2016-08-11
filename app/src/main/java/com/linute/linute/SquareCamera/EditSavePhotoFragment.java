package com.linute.linute.SquareCamera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.MinimumWidthImageView;

import org.bson.types.ObjectId;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.schedulers.Schedulers.io;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

/**
 *
 */
public class EditSavePhotoFragment extends AbstractEditSaveFragment {

    public static final String TAG = EditSavePhotoFragment.class.getSimpleName();
    public static final String BITMAP_URI = "bitmap_Uri";
    public static final String FROM_GALLERY = "from_gallery";

    private Subscription mSubscription;

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
    protected void backPressed() {
        if(isStickerDrawerOpen()){
            toggleStickerDrawer();
        }else {
            CameraActivity activity = (CameraActivity) getActivity();
            if (activity != null) {
                activity.clearBackStack();
            }
        }
    }


    @Override
    protected void loadContent(ViewGroup container) {
        //setup ImageView
        final Uri imageUri = getArguments().getParcelable(BITMAP_URI);
        ImageView vPhotoImageView = new MinimumWidthImageView(container.getContext());
        vPhotoImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        vPhotoImageView.setAdjustViewBounds(true);
        vPhotoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        vPhotoImageView.setBackgroundColor(getResources().getColor(R.color.pure_black));

        if (mFromGallery) {
            Glide.with(this)
                    .load(imageUri)
                    .into(vPhotoImageView);
        } else {
            //when taken from our camera, no need to resize
            //no lag time when loading with this method
            vPhotoImageView.setImageURI(imageUri);
        }

        container.addView(vPhotoImageView);

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

        showProgress(true);
        if (getActivity() == null) return;


        if (mReturnType != CameraActivity.SEND_POST) {
            mSubscription = Observable.just(ImageUtility.savePictureToCache(getActivity(), ImageUtility.getBitmapFromView(mAllContent)))
                    .observeOn(io())
                    .subscribeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {
                                Intent i = new Intent()
                                        .putExtra("image", uri)
                                        .putExtra("type", CameraActivity.IMAGE)
                                        .putExtra("privacy", mAnonSwitch.isChecked())
                                        .putExtra("title", mEditText.getText().toString());
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
            mSubscription = Observable.just(ImageUtility.savePicture(getActivity(), ImageUtility.getBitmapFromView(mAllContent)))
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
                                                (mAnonSwitch.isChecked() ? 1 : 0),
                                                mAnonComments.isChecked() ? 0 : 1,
                                                mEditText.getText().toString(),
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
                            }else {
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
}
