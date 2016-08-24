package com.linute.linute.MainContent.EditScreen;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/23/16.
 */
public class TextTool extends EditContentTool{


    private final View[] textModeViews;

    private static class TextMode {

        private final TextView[] mextViews;
        public int icon;


        public TextMode(int icon, TextView... textViews) {
            this.icon = icon;
            mextViews = textViews;
        }

        public void onSelected(){
            for(TextView tv:mextViews){
                tv.setVisibility(View.VISIBLE);
            }
        };
        public void onUnSelected(){
            for(TextView tv:mextViews){
                tv.setVisibility(View.GONE);
            }
        };
    }

    private TextView topTV;
    private TextView botTV;
    private TextView midTV;


    TextMode[] textModes;
    public TextTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays) {
        super(uri, type, overlays);

        View rootView = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tool_overlay_text, overlays, false);
        Typeface font = Typeface.createFromAsset(overlays.getContext().getAssets(), "Veneer.otf");

        topTV = (TextView)rootView.findViewById(R.id.text_top);
        botTV = (TextView)rootView.findViewById(R.id.text_bot);

        topTV.setTypeface(font);
        botTV.setTypeface(font);

        topTV.setVisibility(View.GONE);
        botTV.setVisibility(View.GONE);


        textModes = new TextMode[]{
                new TextMode(R.drawable.sticker_icon){
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        topTV.setVisibility(View.GONE);
                        botTV.setVisibility(View.GONE);
//                    midTV.setVisibility(View.GONE);
                    }
                },//None
                new TextMode(R.drawable.sticker_icon/*, midTV*/),//Snapchat
                new TextMode(R.drawable.sticker_icon, topTV, botTV),//Full Meme
                new TextMode(R.drawable.sticker_icon, topTV),//Top
                new TextMode(R.drawable.sticker_icon, botTV),//Bottom
        };
        textModeViews = new View[textModes.length];

        mOverlaysView.addView(rootView);

    }

    private int mSelected;

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        LinearLayout rootView = new LinearLayout(parent.getContext());
        rootView.setOrientation(LinearLayout.HORIZONTAL);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTextMode((int)view.getTag());
            }
        };


        for (int i = 0; i < textModes.length; i++) {
            TextMode mode = textModes[i];
            ImageView iv = new ImageView(rootView.getContext());
            iv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            iv.setImageResource(mode.icon);
            iv.setTag(i);
            iv.setOnClickListener(listener);
            rootView.addView(iv);

            textModeViews[i] = iv;
        }

        selectTextMode(0);

        return rootView;
    }

    private void selectTextMode(int index){
        int oldSelected = mSelected;
        mSelected = index;

        textModes[oldSelected].onUnSelected();
        textModes[mSelected].onSelected();

        ((ImageView)textModeViews[oldSelected]).setColorFilter(null);
        ((ImageView)textModeViews[mSelected]).setColorFilter(new PorterDuffColorFilter(
                textModeViews[mSelected].getResources().getColor(R.color.colorAccent),
                PorterDuff.Mode.ADD
        ));

    }

    @Override
    public void onOpen() {
        super.onOpen();
        botTV.setInputType(InputType.TYPE_CLASS_TEXT);
        topTV.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    @Override
    public void onClose() {
        super.onClose();
        botTV.setInputType(InputType.TYPE_NULL);
        topTV.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public int getDrawable() {
        return R.drawable.meme_icon;
    }
}
