package com.linute.linute.MainContent.EditScreen;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/22/16.
 */
public class PrivacySettingTool extends EditContentTool {

    private boolean allowAnonComments = ProcessingOptions.DEFAULT_ALLOW_ANON_COMMENTS;
    private boolean postAsAnon = ProcessingOptions.DEFAULT_POST_AS_ANON;

    public PrivacySettingTool(ViewGroup overlays) {
        super(overlays);
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        View root = inflater.inflate(R.layout.tool_options_privacy, parent, false);


        View leftSwitch = root.findViewById(R.id.switch_left);
        TextView postingAsHeader = (TextView)leftSwitch.findViewById(R.id.text_heading_top);
        TextView postingAsLeftText = (TextView)leftSwitch.findViewById(R.id.text_heading_left);
        TextView postingAsRightText = (TextView)leftSwitch.findViewById(R.id.text_heading_right);
        Switch postingAsSwitch = (Switch)leftSwitch.findViewById(R.id.switch_main);
        postingAsHeader.setText("Posting as");
        postingAsLeftText.setText("Self");
        postingAsRightText.setText("Anon");


        View rightSwitch = root.findViewById(R.id.switch_right);
        TextView anonCommentsHeader = (TextView)rightSwitch.findViewById(R.id.text_heading_top);
        TextView anonCommentsLeftText = (TextView)rightSwitch.findViewById(R.id.text_heading_left);
        TextView anonCommentsRightText = (TextView)rightSwitch.findViewById(R.id.text_heading_right);
        Switch anonCommentsSwitch = (Switch)rightSwitch.findViewById(R.id.switch_main);
        anonCommentsHeader.setText("Anon comments");
        anonCommentsLeftText.setText("Yes");
        anonCommentsRightText.setText("No");


        postingAsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                postAsAnon = b;
            }
        });

        anonCommentsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                allowAnonComments = b;
            }
        });

        return root;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {
        options.allowAnonComments = allowAnonComments;
        options.postAsAnon = postAsAnon;
    }

    @Override
    public String getName() {
        return "Privacy";
    }

    @Override
    public int getDrawable() {
        return 0;
    }
}