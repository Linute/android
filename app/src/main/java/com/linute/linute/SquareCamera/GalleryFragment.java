package com.linute.linute.SquareCamera;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.soundcloud.android.crop.Crop;

/* At the moment this is just a dummy fragment so our CameraFragment isn't continuously
 * resumed for no reason. Maybe in the future we can create our own gallery
 * */

public class GalleryFragment extends Fragment {

    public static final String TAG = GalleryFragment.class.getSimpleName();

    public GalleryFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }
}
