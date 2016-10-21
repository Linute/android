package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 10/21/16.
 */
public class RatingBar extends FrameLayout {

    private ProgressBar vProgressBar;
    private TextView vOptionText;
    private TextView vPercentText;

    public RatingBar(Context context) {
        super(context);
        inflate(context, R.layout.ratingbar_layout, this);
        vProgressBar = (ProgressBar) findViewById(R.id.rating_progress);
        vOptionText = (TextView) findViewById(R.id.rating_text);
        vPercentText = (TextView) findViewById(R.id.rating_percent);
    }

    public RatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setProgressColor(@ColorInt int color){
        vProgressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void setProgress(int progress){
        vProgressBar.setProgress(progress);

        String text = progress+"%";
        vPercentText.setText(text);
    }

    public void setOptionText(String text){
        vOptionText.setText(text);
    }

    public void setOptionTextColor(@ColorInt int color){
        vOptionText.setTextColor(color);
    }

    public void setPercentTextColor(@ColorInt int color){
        vPercentText.setTextColor(color);
    }

}
