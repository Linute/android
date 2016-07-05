package com.linute.linute.SquareCamera.overlay;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by mikhail on 6/25/16.
 */
public class OverlayWipeAdapter implements WipeViewPager.WipeAdapter {

    private ArrayList<Bitmap> mOverlays;

    public OverlayWipeAdapter(Bitmap... overlays) {
        if (overlays != null) {
            mOverlays = new ArrayList<>(overlays.length + 1);
            mOverlays.add(null); //for no filter
            Collections.addAll(mOverlays, overlays);
        } else {
            mOverlays = new ArrayList<>();
            mOverlays.add(null);
        }
    }

    public OverlayWipeAdapter(Collection<Bitmap> overlays) {
        mOverlays = new ArrayList<>(overlays.size() + 1);
        mOverlays.add(null); //for no filter
        mOverlays.addAll(overlays);
    }

    @Override
    public int getCount() {
        return (mOverlays.size() > 1 ? Integer.MAX_VALUE : 1);
    }

    @Override
    public Bitmap getOverlay(int position) {
        while (position < 0) {
            position += mOverlays.size();
        }
        position = position % mOverlays.size();

        return mOverlays.get(position);
    }

    public void add(Bitmap overlay) {
        add(overlay, 1);
    }

    public void add(Bitmap overlay, int position) {
        if (position == -1) {
            position = mOverlays.size() - 1;
        }

        mOverlays.add(position, overlay);
    }

    public void destroy() {
        for (Bitmap b : mOverlays) {
            if (b != null)
                b.recycle();
        }
    }


}
