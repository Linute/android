package com.linute.linute.SquareCamera.overlay;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.ImageUtils;

/**
 * Created by mikhail on 6/24/16.
 */
public class ImageOverlayFragment extends Fragment {

//    public static final String KEY_OVERLAY_IMAGE= "overlay_image";

    private ImageView mOverlayIV;
    private Bitmap mBitmap;

    public static ImageOverlayFragment newInstance() {

//        Bundle args = new Bundle();
        ImageOverlayFragment fragment = new ImageOverlayFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mOverlayIV = new ImageView(inflater.getContext());//(ImageView)inflater.inflate(R.layout.fragment_overlay,container,false);
        mOverlayIV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mOverlayIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mOverlayIV.setImageBitmap(mBitmap);
        return mOverlayIV;
    }

    public void setImage(Bitmap bmp) {
        Log.i("AAA", mOverlayIV+"");
        mBitmap = bmp;
        if (mOverlayIV != null) {
            mOverlayIV.setImageBitmap(bmp);
            mOverlayIV.invalidate();
        }
    }

    /*public Bitmap getOverlayImage(){
        ygetView().
    }*/
}
