package com.linute.linute.SquareCamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.lyft.android.scissors.CropView;

import java.io.File;

import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/* At the moment this is just a dummy fragment so our CameraFragment isn't continuously
 * resumed for no reason. Maybe in the future we can create our own gallery
 * */

public class GalleryFragment extends Fragment {
    public static final String TAG = GalleryFragment.class.getSimpleName();

    private int SELECT_PICTURE = 1;

    private CropView mCropView;

    CompositeSubscription subscriptions = new CompositeSubscription();

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        mCropView = (CropView) view.findViewById(R.id.crop_view_scis);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.crop_image_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Scale and crop");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });


        view.findViewById(R.id.save_photo).setOnClickListener(new View.OnClickListener() {
            boolean mIsRunning = false;

            @Override
            public void onClick(View v) {

                if (mIsRunning) return;

                mIsRunning = true;

                final File temp = new File(getActivity().getCacheDir(), "image_crop.jpg");

                Observable<Void> onSave = Observable.from(
                        mCropView.extensions()
                                .crop()
                                .quality(100)
                                .format(Bitmap.CompressFormat.JPEG)
                                .into(temp))
                        .subscribeOn(io())
                        .observeOn(mainThread());


                subscriptions.add(onSave
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void nothing) {
                                CameraActivity activity = (CameraActivity) getActivity();
                                if (activity != null) {
                                    activity.launchEditAndSaveFragment(Uri.fromFile(temp));
                                }
                            }
                        }));
            }
        });

        return view;
    }

    private boolean mImageLoaded = false;
    private static final String IMAGE_LOADED_KEY = "image_loaded_key";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGE_LOADED_KEY, mImageLoaded);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mImageLoaded = savedInstanceState.getBoolean(IMAGE_LOADED_KEY, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mImageLoaded && hasCameraAndWritePermission()) {
            mImageLoaded = true;
            getImage();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (!hasCameraAndWritePermission()) {
            getFragmentManager().popBackStack();
            return;
        }

        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PICTURE) { //got an image
            mCropView.extensions().load(data.getData());
        } else { //didnt get image, so popbackstack
            getFragmentManager().popBackStack();
        }
    }


    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.RECORD_AUDIO);
    }
}
