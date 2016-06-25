package com.linute.linute.SquareCamera.overlay;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by mikhail on 6/25/16.
 */
public class OverlayWipeAdapter implements WipeViewPager.WipeAdapter{

    private ArrayList<Bitmap> mOverlays;

    public OverlayWipeAdapter(Bitmap... overlays){
        if(overlays != null){
            mOverlays = new ArrayList<>(overlays.length+1);
            mOverlays.add(null); //for no filter
            Collections.addAll(mOverlays, overlays);
        }else{
            mOverlays = new ArrayList<>();
            mOverlays.add(null);
        }
    }

    public OverlayWipeAdapter(Collection<Bitmap> overlays){
        mOverlays = new ArrayList<>(overlays.size()+1);
        mOverlays.add(null); //for no filter
        mOverlays.addAll(overlays);
    }

    @Override
    public int getCount() {
        return (mOverlays.size() > 1 ? Integer.MAX_VALUE : 1);
    }

    @Override
    public View getView(View recycle, ViewGroup container, int position, boolean right) {
        while(position < 0){
            position+=mOverlays.size();
        }
        Bitmap overlay = mOverlays.get(position % mOverlays.size());

        AlignedImageView overlayIV = (AlignedImageView)recycle;
        Log.i("BBB", recycle + " " + overlayIV);
        if(overlayIV == null){
            overlayIV = new AlignedImageView(container.getContext());
            DisplayMetrics displayMetrics = container.getContext().getResources().getDisplayMetrics();
            overlayIV.setLayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels,displayMetrics.heightPixels));
            overlayIV.setMinimumWidth(displayMetrics.widthPixels);
            overlayIV.setMaxWidth(displayMetrics.widthPixels);
            overlayIV.setMinimumHeight(displayMetrics.heightPixels);
            overlayIV.setMaxHeight(displayMetrics.heightPixels);
            overlayIV.setScaleType(ImageView.ScaleType.FIT_END);
            /*Matrix m = new Matrix();
            if(right){
                m.setScale(1,1,displayMetrics.widthPixels, displayMetrics.heightPixels/2);
            }else{
                m.setScale(1,1,0,displayMetrics.heightPixels/2);
            }
            overlayIV.setImageMatrix(m);*/
        }

        overlayIV.setAlignLeft(right);
        overlayIV.setImageBitmap(overlay);
        Log.i("BBB", ""+overlayIV);
        return overlayIV;
    }
}
