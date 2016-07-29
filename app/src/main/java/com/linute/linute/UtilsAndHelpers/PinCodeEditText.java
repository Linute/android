package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.linute.linute.R;

/**
 * Created by QiFeng on 7/29/16.
 */
public class PinCodeEditText extends LinearLayout {


    public PinCodeEditText(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.)
    }

    public PinCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PinCodeEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }




}
