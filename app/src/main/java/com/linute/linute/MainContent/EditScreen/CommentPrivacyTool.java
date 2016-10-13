package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by mikhail on 8/22/16.
 */
public class CommentPrivacyTool extends EditContentTool {

    private boolean isAnonCommentsDisabled = ProcessingOptions.DEFAULT_ANON_COMMENTS_DISABLED;
    private boolean postAsAnon = ProcessingOptions.DEFAULT_POST_AS_ANON;
    private final FrameLayout mMidTextTarget1;

    public CommentPrivacyTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, final EditFragment frag) {
        super(uri, type, overlays);
        mMidTextTarget1 = new FrameLayout(overlays.getContext());
        mMidTextTarget1.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mMidTextTarget1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("AAA", "say");
                ((TextTool)frag.selectTool(TextTool.class)).selectTextMode(TextTool.MID_TEXT_INDEX);
            }
        });
        mOverlaysView.addView(mMidTextTarget1);
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        View root = inflater.inflate(R.layout.tool_options_privacy, parent, false);


        View leftSwitch = root.findViewById(R.id.switch_left);
        TextView postingAsHeader = (TextView) leftSwitch.findViewById(R.id.text_heading_top);
        TextView postingAsLeftText = (TextView) leftSwitch.findViewById(R.id.text_heading_left);
        TextView postingAsRightText = (TextView) leftSwitch.findViewById(R.id.text_heading_right);
        SwitchCompat postingAsSwitch = (SwitchCompat) leftSwitch.findViewById(R.id.switch_main);
        postingAsHeader.setText("Commenting as");

        postingAsLeftText.setText("");
        postingAsRightText.setText("");
        postingAsSwitch.setTextOn("Anon");
        postingAsSwitch.setTextOff("Self");

        View rightSwitch = root.findViewById(R.id.switch_right);
        rightSwitch.setVisibility(View.GONE);

        if (!postingAsSwitch.getShowText()) {
            postingAsLeftText.setText("Self");
            postingAsRightText.setText("Anon");

        } else {
            postingAsSwitch.setTextOff("Self");
            postingAsSwitch.setTextOn("Anon");

        }

        final ImageView profileImageView = (ImageView)root.findViewById(R.id.image_profile);
        profileImageView.setVisibility(View.GONE);

        postingAsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                postAsAnon = b;
                if(b){
                    profileImageView.setImageResource(R.drawable.ic_anon);
                }else{
                    String profileImageUrl = Utils.getImageUrlOfUser(profileImageView.getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("profileImage", ""));
                    Glide.with(profileImageView.getContext()).load(profileImageUrl).into(profileImageView);
                }
            }
        });


        String profileImageUrl = Utils.getImageUrlOfUser(profileImageView.getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("profileImage", ""));
        Glide.with(profileImageView.getContext()).load(profileImageUrl).into(profileImageView);


        postingAsSwitch.setChecked(postAsAnon);

        return root;
    }



    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {
        options.isAnonCommentsDisabled = isAnonCommentsDisabled;
        options.postAsAnon = postAsAnon;
    }

    @Override
    public void onOpen() {
        super.onOpen();
        mMidTextTarget1.setClickable(true);
    }

    @Override
    public void onClose() {
        super.onClose();
        Log.i("AAA", "ay");
        mMidTextTarget1.setClickable(false);
    }

    @Override
    public String getName() {
        return "Privacy";
    }

    @Override
    public int getDrawable() {
        return R.drawable.privacy_icon_selected;
    }
}
