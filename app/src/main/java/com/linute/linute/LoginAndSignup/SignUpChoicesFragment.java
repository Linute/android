package com.linute.linute.LoginAndSignup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.WebViewActivity;

/**
 * Created by QiFeng on 7/18/16.
 */
public class SignUpChoicesFragment extends Fragment {

    public SignUpChoicesFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup_options, container, false);

        rootView.findViewById(R.id.facebook_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null) {
                    activity.selectedFacebookLogin();
                }
            }
        });

        rootView.findViewById(R.id.linute_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null) {
                    activity.selectedSignup();
                }
            }
        });

        setUpTextView((TextView) rootView.findViewById(R.id.legal));

        rootView.findViewById(R.id.log_in).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreLoginActivity activity = (PreLoginActivity) getActivity();
                        if (activity != null) {
                            activity.selectSignIn();
                        }
                    }
                });

        return rootView;
    }


    private void setUpTextView(TextView view) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "By signing up, you agree to the ");

        ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(view.getContext(), R.color.secondaryColor)); //color of span
        spanTxt.append("Term of services");

        int start = spanTxt.length() - "Term of services".length();

        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/terms-of-service");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, spanTxt.length(), 0);

        spanTxt.setSpan(fcs, start, spanTxt.length(), 0);

        spanTxt.append(" and");
        spanTxt.append(" Privacy Policy");

        start = spanTxt.length() - " Privacy Policy".length();

        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/privacy-policy");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, spanTxt.length(), 0);

        spanTxt.setSpan(fcs, start, spanTxt.length(), 0);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }
}
