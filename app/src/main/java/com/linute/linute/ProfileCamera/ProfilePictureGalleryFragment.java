package com.linute.linute.ProfileCamera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.ImageUtils;
import com.lyft.android.scissors.CropView;

import java.io.File;
import java.util.Date;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 7/5/16.
 */
public class ProfilePictureGalleryFragment extends BaseFragment{

    public static final String TAG = ProfilePictureGalleryFragment.class.getSimpleName();

    private static final int REQUEST_PICK = 123;
    private CropView vCropView;
    private Subscription mSubscription;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crop, container, false);
        vCropView = (CropView) root.findViewById(R.id.crop_image);
        Toolbar t = (Toolbar) root.findViewById(R.id.toolbar);

        if (t != null) {
            t.inflateMenu(R.menu.crop_menu);
            t.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() == null) return;
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            });

            t.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.check) {
                        cropView();
                        return true;
                    }
                    return false;
                }
            });
        }

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            ImageUtils.pickUsing(this, REQUEST_PICK);
            setFragmentState(FragmentState.FINISHED_UPDATING);
        }

        return root;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                vCropView.setImageURI(data.getData());
            } else {
                if (getActivity() != null) getActivity().finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void cropView() {
        if (getActivity() == null) return;

        final File croppedFile = new File(getActivity().getCacheDir(), new Date().getTime() + "");

        mSubscription = Observable
                .from(vCropView.extensions()
                        .crop()
                        .quality(100)
                        .format(JPEG)
                        .into(croppedFile))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void nothing) {
                        ProfileCameraActivity activity = (ProfileCameraActivity) getActivity();
                        if (activity != null) {
                            activity.replaceFragment(
                                    ConfirmProfilePicture.newInstance(Uri.fromFile(croppedFile))
                            );
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSubscription != null) mSubscription.unsubscribe();
    }
}
