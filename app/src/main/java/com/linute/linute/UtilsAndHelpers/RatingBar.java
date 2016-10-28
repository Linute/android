package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.MainContent.DiscoverFragment.PollChoiceItem;
import com.linute.linute.R;

/**
 * Created by QiFeng on 10/21/16.
 */
public class RatingBar extends FrameLayout implements View.OnClickListener{

    private ProgressBar vProgressBar;
    private TextView vOptionText;
    //private TextView vPercentText;
    private PollChoiceItem mChoice;
    private OnClickChoice mOnClickChoice;

    public RatingBar(Context context) {
        super(context);
        inflate(context, R.layout.ratingbar_layout, this);
        setOnClickListener(this);
        vProgressBar = (ProgressBar) findViewById(R.id.rating_progress);
        vOptionText = (TextView) findViewById(R.id.rating_text);
        //vPercentText = (TextView) findViewById(R.id.rating_percent);
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
        LayerDrawable drawable = ((LayerDrawable)vProgressBar.getProgressDrawable());
        drawable.getDrawable(1).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        drawable.getDrawable(0).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void removeColorFilters(){
        LayerDrawable drawable = ((LayerDrawable)vProgressBar.getProgressDrawable());
        drawable.getDrawable(1).setColorFilter(null);
        drawable.getDrawable(0).setColorFilter(null);
    }

    public void setOptionTextStyle(Typeface typeface){
        vOptionText.setTypeface(typeface);
    }

    public void setProgress(int progress){
        vProgressBar.setProgress(progress);
    }

    public void setOptionText(String text){
        vOptionText.setText(text);
    }

    public void setOptionTextColor(@ColorInt int color){
        vOptionText.setTextColor(color);
    }

    public void setChoice(PollChoiceItem choice) {
        mChoice = choice;
    }

    public void setOnClickChoice(OnClickChoice onClickChoice) {
        mOnClickChoice = onClickChoice;
    }

    @Override
    public void onClick(View v) {
        if (mOnClickChoice != null){
            mOnClickChoice.click(mChoice);
        }
    }

    public interface OnClickChoice{
        void click(PollChoiceItem choice);
    }
}
