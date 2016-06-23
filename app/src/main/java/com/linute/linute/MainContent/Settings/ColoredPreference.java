package com.linute.linute.MainContent.Settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mikhail on 6/14/16.
 *
 * Preference with a red title
 */
public class ColoredPreference extends Preference {

    public ColoredPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColoredPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColoredPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(0xFFCC0000);
    }
}
