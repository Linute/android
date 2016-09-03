package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;

import com.linute.linute.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mikhail on 9/1/16.
 * <p/>
 * This class exists solely to make it easy to switch between rounded and circular
 * ImageViews for profile images. Just change what ProfileImageView extends
 */
public class ProfileImageView extends CircleImageView {

    public ProfileImageView(Context context) {
        super(context);
        setUpBorder();
    }

    public ProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpBorder();
    }

    public ProfileImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUpBorder();
    }


    private void setUpBorder() {
        setBorderColorResource(R.color.seperator_color);
        setBorderWidth(getResources().getDimensionPixelSize(R.dimen.profile_border_width));
    }
}
