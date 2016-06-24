package com.linute.linute.SquareCamera.overlay;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by mikhail on 6/24/16.
 */
public class ImageOverlayAdapter extends FragmentStatePagerAdapter {

    ArrayList<ImageOverlayFragment> overlayFragments = new ArrayList<>();
    private ArrayList<Bitmap> overlayImages = new ArrayList<>();
    ;

    public ImageOverlayAdapter(FragmentManager fm, Bitmap... overlays) {
        super(fm);
        overlayImages.add(null); //for no filter
        overlayFragments.add(null);
        if (overlays != null) {
            for (Bitmap bitmap : overlays) {
                overlayImages.add(bitmap);
                overlayFragments.add(null);
            }
        }
    }

    public void setOverlays(Bitmap... overlays) {

        overlayImages.clear();
        overlayFragments.clear();
        overlayImages.add(null); //for no filter
        overlayFragments.add(null);
        if (overlays != null) {
            for (Bitmap bitmap : overlays) {
                overlayImages.add(bitmap);
                overlayFragments.add(null);
            }
        }
    }

    public void setOverlays(ArrayList<Bitmap> overlays) {
        overlayImages.clear();
        overlayFragments.clear();
        overlayImages.add(null); //for no filter
        overlayFragments.add(null);
        if (overlays != null) {
            for (Bitmap bitmap : overlays) {
                overlayImages.add(bitmap);
                overlayFragments.add(null);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        position = position % overlayImages.size();

        ImageOverlayFragment fragment = overlayFragments.get(position);
        if (fragment == null) {
            fragment = ImageOverlayFragment.newInstance();
            overlayFragments.set(position, fragment);
        }

        fragment.setImage(overlayImages.get(position));
        return fragment;
    }

    @Override
    public int getCount() {
        return (overlayImages.size() < 3 ? overlayImages.size() : Integer.MAX_VALUE);
    }
}
