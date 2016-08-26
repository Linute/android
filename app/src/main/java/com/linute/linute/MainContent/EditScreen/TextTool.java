package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;

/**
 * Created by mikhail on 8/23/16.
 */
public class TextTool extends EditContentTool {


    private final View[] textModeViews;
    int midTvPost = 0;
    private final CustomBackPressedEditText midET;

    private static class TextMode {

        private final TextView[] mextViews;
        public int icon;


        public TextMode(int icon, TextView... textViews) {
            this.icon = icon;
            mextViews = textViews;
        }

        public void onSelected() {
            for (TextView tv : mextViews) {
                tv.setVisibility(View.VISIBLE);
            }
            if(mextViews.length>0)
            showKeyboard(mextViews[0]);

        }

        ;

        public void onUnSelected() {
            for (TextView tv : mextViews) {
                tv.setVisibility(View.GONE);
            }
        }

        ;
    }

    private TextView topTV;
    private TextView botTV;
    private TextView midTV;


    TextMode[] textModes;

    public TextTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, Dimens dim) {
        super(uri, type, overlays);
        View rootView = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tool_overlay_text, overlays, false);
        Typeface font = Typeface.createFromAsset(overlays.getContext().getAssets(), "Veneer.otf");



        topTV = (TextView) rootView.findViewById(R.id.text_top);
        botTV = (TextView) rootView.findViewById(R.id.text_bot);
        midTV = (TextView) rootView.findViewById(R.id.text_mid);
        midET = (CustomBackPressedEditText) rootView.findViewById(R.id.edit_text_mid);

        topTV.setTypeface(font);
        botTV.setTypeface(font);

        topTV.setVisibility(View.GONE);
        botTV.setVisibility(View.GONE);
        midTV.setVisibility(View.GONE);
        midET.setVisibility(View.GONE);

        midTV.setY(dim.height/2);

        midET.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                hideKeyboard(midET);

                String text = midET.getText().toString().trim();
                midET.setText(text);
                midTV.setText(text);


                midET.setVisibility(View.GONE);
                midTV.setVisibility(View.VISIBLE);
                //TODO animate?
            }
        });

        midTV.setOnTouchListener(new View.OnTouchListener() {

            float downY, initY;
            long timeDown;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        initY = view.getY();
                        timeDown = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        view.setY(initY + (motionEvent.getRawY() - downY));

                        return true;
                    case MotionEvent.ACTION_UP:
                        if(System.currentTimeMillis() - timeDown < 400
                                && Math.abs(motionEvent.getRawY()-downY) < 20){
                            swapSnapchatET();
                        }
                        return true;
                }
                return false;
            }
        });

        textModes = new TextMode[]{
                new TextMode(R.drawable.sticker_icon) {
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        topTV.setVisibility(View.GONE);
                        botTV.setVisibility(View.GONE);
                        midET.setVisibility(View.GONE);
                    }
                },//None
                new TextMode(R.drawable.sticker_icon, midTV){
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        swapSnapchatET();
                    }
                },//Snapchat
                new TextMode(R.drawable.sticker_icon, topTV, botTV),//Full Meme
                new TextMode(R.drawable.sticker_icon, topTV),//Top
                new TextMode(R.drawable.sticker_icon, botTV),//Bottom
        };
        textModeViews = new View[textModes.length];

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        mOverlaysView.addView(rootView);

    }

    public void hideKeyboard(View view) {
        view.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void swapSnapchatET() {
        midET.setVisibility(View.VISIBLE);
        midTV.setVisibility(View.GONE);
        showKeyboard(midET);
    }

    private int mSelected;

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        LinearLayout rootView = new LinearLayout(parent.getContext());
        rootView.setOrientation(LinearLayout.HORIZONTAL);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTextMode((int) view.getTag());
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

    private static void showKeyboard(View view){
        view.requestFocus();
        InputMethodManager lManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(view, 0);
    }

    private void selectTextMode(int index) {
        int oldSelected = mSelected;
        mSelected = index;

        textModes[oldSelected].onUnSelected();
        textModes[mSelected].onSelected();

        ((ImageView) textModeViews[oldSelected]).setColorFilter(null);
        ((ImageView) textModeViews[mSelected]).setColorFilter(new PorterDuffColorFilter(
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
        return R.drawable.meme_icon_selected;
    }
}
