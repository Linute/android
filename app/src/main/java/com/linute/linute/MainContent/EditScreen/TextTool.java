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
import android.view.inputmethod.EditorInfo;
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
    private final View mTextContainer;

    public static final int MID_TEXT_INDEX = 1;




    private static class TextMode {

        private final TextView[] mextViews;
        public int icon;

        static String[] savedText = new String[3];

        public TextMode(int icon, TextView... textViews) {
            this.icon = icon;
            mextViews = textViews;
        }

        public void onSelected() {
            for (int i = 0; i < mextViews.length; i++) {
                TextView tv = mextViews[i];
                tv.setText(savedText[i]);
                tv.setVisibility(View.VISIBLE);
            }
            if (mextViews.length > 0)
                showKeyboard(mextViews[0]);

        }


        public void onUnSelected() {
            for (int i = 0; i < mextViews.length; i++) {
                TextView tv = mextViews[i];
                tv.setVisibility(View.GONE);
                savedText[i] = tv.getText().toString();
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
        mTextContainer = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tool_overlay_text, overlays, false);
        Typeface font = Typeface.createFromAsset(overlays.getContext().getAssets(), "Veneer.otf");

        topTV = (TextView) mTextContainer.findViewById(R.id.text_top);
        botTV = (TextView) mTextContainer.findViewById(R.id.text_bot);
        midTV = (TextView) mTextContainer.findViewById(R.id.text_mid);
        midET = (CustomBackPressedEditText) mTextContainer.findViewById(R.id.edit_text_mid);

        topTV.setTypeface(font);
        botTV.setTypeface(font);

        topTV.setVisibility(View.GONE);
        botTV.setVisibility(View.GONE);
        midTV.setVisibility(View.GONE);
        midET.setVisibility(View.GONE);

        midTV.setY(dim.height / 2);

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
                        if (System.currentTimeMillis() - timeDown < 400
                                && Math.abs(motionEvent.getRawY() - downY) < 20) {
                            swapSnapchatET();
                        }
                        return true;
                }
                return false;
            }
        });

        textModes = new TextMode[]{
                new TextMode(R.drawable.no_text_icon) {
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        topTV.setVisibility(View.GONE);
                        botTV.setVisibility(View.GONE);
                        midET.setVisibility(View.GONE);
                    }
                },//None
                new TextMode(R.drawable.middle_text_icon, midTV) {
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        swapSnapchatET();
                    }
                },//Snapchat
                new TextMode(R.drawable.text_meme_icon, topTV, botTV) {
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        topTV.setNextFocusDownId(R.id.text_bot);
                        botTV.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    }

                    @Override
                    public void onUnSelected() {
                        super.onUnSelected();
                        topTV.setNextFocusDownId(0);
                    }
                },//Full Meme
                new TextMode(R.drawable.top_text_icon, topTV),//Top
                new TextMode(R.drawable.text_bottom_icon, botTV),//Bottom
        };
        textModeViews = new View[textModes.length];


        //OnClick to hide the keyboard when image is tapped
        //Turned on and off in OnOpen and OnClose to allow other touch listeners to function
        mTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        mTextContainer.setClickable(false);

        mOverlaysView.addView(mTextContainer);
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

    private static void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager lManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        lManager.showSoftInput(view, 0);
    }

    public void selectTextMode(int index) {
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
        mTextContainer.setClickable(true);
    }

    @Override
    public void onClose() {
        super.onClose();
        botTV.setInputType(InputType.TYPE_NULL);
        topTV.setInputType(InputType.TYPE_NULL);
        mTextContainer.setClickable(false);
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

    }

    @Override
    public String getName() {
        return "Meme";
    }

    @Override
    public int getDrawable() {
        return R.drawable.meme_icon_selected;
    }
}
