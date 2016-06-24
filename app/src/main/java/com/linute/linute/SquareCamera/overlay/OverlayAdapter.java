package com.linute.linute.SquareCamera.overlay;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/24/16.
 */
public class OverlayAdapter extends PagerAdapter {

    ImageView[] mImageViews = new ImageView[2];


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return true;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        super.instantiateItem(container, position);
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
