package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomBackPressedEditText;

/**
 * Created by mikhail on 8/23/16.
 */
public class TextTool extends EditContentTool {

    private static final String TAG = TextTool.class.getSimpleName();
    private Space vFocusView; //so we can focus on this

    private final View[] textModeViews;
    int midTvPost = 0;
    private final View mTextContainer;
    public static final int MID_TEXT_INDEX = 1;

    private boolean mMidETCanBeMoved = false;


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
                tv.setText("");

                if (savedText[i] != null)
                    tv.append(savedText[i]);

                tv.setVisibility(View.VISIBLE);
            }

            if (mextViews.length > 0)
                showKeyboard(mextViews[0]);
        }


        public void onUnSelected() {
            for (int i = 0; i < mextViews.length; i++) {
                TextView tv = mextViews[i];
                tv.setVisibility(View.INVISIBLE);
                savedText[i] = tv.getText().toString();
            }
        }
    }

    private final CustomBackPressedEditText topET;
    private final CustomBackPressedEditText botET;
    private final CustomBackPressedEditText midET;

//    private TextView midTV;


    TextMode[] textModes;

    public TextTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, Dimens dim, final EditFragment frag) {
        super(uri, type, overlays);
        mTextContainer = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tool_overlay_text, overlays, false);
        Typeface font = Typeface.createFromAsset(overlays.getContext().getAssets(), "Veneer.otf");

        TextMode.savedText = new String[3];

        vFocusView = (Space) mTextContainer.findViewById(R.id.focus);
        topET = (CustomBackPressedEditText) mTextContainer.findViewById(R.id.text_top);
        botET = (CustomBackPressedEditText) mTextContainer.findViewById(R.id.text_bot);
        midET = (CustomBackPressedEditText) mTextContainer.findViewById(R.id.edit_text_mid);

        topET.setTypeface(font);
        botET.setTypeface(font);


        topET.setVisibility(View.INVISIBLE);
        botET.setVisibility(View.INVISIBLE);
        midET.setVisibility(View.INVISIBLE);

        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                frag.selectTool(TextTool.this);
                //topET.setCursorVisible(b);
                //botET.setCursorVisible(b);
                //midET.setCursorVisible(b);
            }
        };

        topET.setOnFocusChangeListener(focusChangeListener);
        botET.setOnFocusChangeListener(focusChangeListener);
        midET.setOnFocusChangeListener(focusChangeListener);

        midET.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                //hideKeyboard(midET);
                clearFocus(midET);
                //midET.setFocusable(false);

                String text = midET.getText().toString().trim();
                midET.setText(text);
                if (text.isEmpty())
                    selectTextMode(0);
                else
                    mMidETCanBeMoved = true;

            }
        });

        topET.addTextChangedListener(new LimitTextWatcher(topET));
        midET.addTextChangedListener(new LimitTextWatcher(midET));
        botET.addTextChangedListener(new LimitTextWatcher(botET));

        topET.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                clearFocus(topET);
            }
        });

        botET.setBackAction(new CustomBackPressedEditText.BackButtonAction() {
            @Override
            public void backPressed() {
                clearFocus(botET);
            }
        });

        midET.setEnterAction(new CustomBackPressedEditText.EnterButtonAction() {
            @Override
            public void enterPressed() {
                if (midET.getText().toString().trim().isEmpty())
                    selectTextMode(0);

                hideKeyboard(midET);
                clearFocus(midET);
                //midET.setFocusable(false);
                mMidETCanBeMoved = true;
            }
        });

        botET.setEnterAction(new CustomBackPressedEditText.EnterButtonAction() {
            @Override
            public void enterPressed() {
                hideKeyboard(botET);
                clearFocus(botET);
            }
        });

        topET.setEnterAction(new CustomBackPressedEditText.EnterButtonAction() {
            @Override
            public void enterPressed() {
                hideKeyboard(topET);
                clearFocus(topET);
            }
        });

        midET.setOnTouchListener(new View.OnTouchListener() {

            float downY, initY;
            long timeDown;
            int max;
            FrameLayout.LayoutParams params;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getRawY();
                        initY = view.getY();
                        timeDown = System.currentTimeMillis();
                        max = mTextContainer.getHeight() - view.getHeight();
                        params = (FrameLayout.LayoutParams)view.getLayoutParams();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //setY() doesn't allow screen to panAdjust correctly sp using margins instead
                        if (mMidETCanBeMoved) {
                            params.gravity = Gravity.NO_GRAVITY;
                            params.setMargins(0,clipTopMargin(0, max, (int)(initY + motionEvent.getRawY() - downY)),0,0);
                            view.setLayoutParams(params);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(motionEvent.getRawY() - downY) < 20) {
                            //midET.setFocusableInTouchMode(true);
                            midET.requestFocus();
                            showKeyboard(midET);
                            mMidETCanBeMoved = false;
                        }
                        return true;
                }

                return true;
            }
        });

        textModes = new TextMode[]{
                new TextMode(R.drawable.no_text_icon) {
                    @Override
                    public void onSelected() {
                        super.onSelected();
                        topET.setVisibility(View.INVISIBLE);
                        botET.setVisibility(View.INVISIBLE);
                        midET.setVisibility(View.INVISIBLE);
                    }
                },//None
                new TextMode(R.drawable.middle_text_icon, midET){
                    @Override
                    public void onSelected() {
                        //midET.setFocusableInTouchMode(true);
                        super.onSelected();
                    }
                },//middle
                new TextMode(R.drawable.text_meme_icon, topET, botET) {
                    @Override
                    public void onSelected() {
                        topET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        topET.setNextFocusForwardId(R.id.text_bot);
                        super.onSelected();
                    }

                    @Override
                    public void onUnSelected() {
                        super.onUnSelected();
                        topET.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    }
                },//Full Meme
                new TextMode(R.drawable.top_text_icon, topET),//Top
                new TextMode(R.drawable.text_bottom_icon, botET),//Bottom
        };
        textModeViews = new View[textModes.length];


        //OnClick to hide the keyboard when image is tapped
        //Turned on and off in OnOpen and OnClose to allow other touch listeners to function
        mTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelected == MID_TEXT_INDEX) {
                    hideKeyboard(midET);
                    if (midET.getText().toString().trim().isEmpty())
                        selectTextMode(0);
                    else {
                        clearFocus(midET);
                        mMidETCanBeMoved = true;
                    }

                } else if (mSelected != 0) {
                    hideKeyboard(view);
                    botET.clearFocus();
                    topET.clearFocus();
                    vFocusView.requestFocus();
                }
            }
        });

        mTextContainer.setClickable(false);

        mOverlaysView.addView(mTextContainer);
    }

    protected class LimitTextWatcher implements TextWatcher{
        String beforeText;

        public LimitTextWatcher(EditText mTV) {
            this.mTV = mTV;
        }

        EditText mTV;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mTV.getLineCount() > mTV.getMaxLines()) {
                mTV.setText(beforeText);
                mTV.setSelection(mTV.getText().length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


    private int clipTopMargin(int min, int max, int newMargin){
        if (newMargin <= min) return min;
        else if (newMargin >= max) return max;
        else return newMargin;
    }

    private void clearFocus(View v) {
        v.clearFocus();
        vFocusView.requestFocus();
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            View v = inflater.inflate(R.layout.tool_option_text_mode, rootView, false);
            ImageView iv = (ImageView) v.findViewById(R.id.image_icon);
            iv.setImageResource(mode.icon);
            v.setTag(i);
            v.setOnClickListener(listener);
            rootView.addView(v);

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

        if (textModeViews[index] != null) {
            ((ImageView) textModeViews[oldSelected]).setColorFilter(null);
            ((ImageView) textModeViews[mSelected]).setColorFilter(new PorterDuffColorFilter(
                    textModeViews[mSelected].getResources().getColor(R.color.secondaryColor),
                    PorterDuff.Mode.MULTIPLY
            ));
        }
    }

    public boolean hasText() {
        return botET.getVisibility() == View.VISIBLE || midET.getVisibility() == View.VISIBLE || topET.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onOpen() {
        super.onOpen();
        //midET.setCursorVisible(true);
//        botET.setInputType(InputType.TYPE_CLASS_TEXT);
//        topET.setInputType(InputType.TYPE_CLASS_TEXT);
        mTextContainer.setClickable(true);
    }

    @Override
    public void onClose() {
        super.onClose();
//        botET.setInputType(InputType.TYPE_NULL);
//        topET.setInputType(InputType.TYPE_NULL);
        mTextContainer.setClickable(false);

        if (midET.getText().toString().trim().length() == 0) {
            midET.setVisibility(View.INVISIBLE);
        }
        if (botET.getText().toString().trim().length() == 0) {
            botET.setVisibility(View.INVISIBLE);
        }
        if (topET.getText().toString().trim().length() == 0) {
            topET.setVisibility(View.INVISIBLE);
        }
    }


    private void updateMidETVisibility() {
        if (midET.getText().toString().trim().isEmpty())
            midET.setVisibility(View.INVISIBLE);
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {
        if (mSelected == MID_TEXT_INDEX) {
            hideKeyboard(midET);
            updateMidETVisibility();
        }

        hideKeyboard(botET);
        hideKeyboard(topET);
        midET.setFocusable(false);
        botET.setFocusable(false);
        topET.setFocusable(false);
        //midET.setInputType(InputType.TYPE_NULL);
        //botET.setInputType(InputType.TYPE_NULL);
        //topET.setInputType(InputType.TYPE_NULL);
        midET.clearComposingText();
        botET.clearComposingText();
        topET.clearComposingText();
//        topTV.setVisibility(topET.getVisibility());
//        botTV.setVisibility(botET.getVisibility());
//        midTV.setVisibility(midET.getVisibility());
//        topET.setVisibility(View.GONE);
//        botET.setVisibility(View.GONE);
//        midET.setVisibility(View.GONE);
//        topTV.setText(topET.getText());
//        String text = botET.getText().toString();
//        botTV.setText(text);
//        midTV.setText(midET.getText());
//        midTV.setY(midET.getY());


        options.text = topET.getText().toString() + " " + midET.getText().toString() + " " + botET.getText().toString();

        //midET.setCursorVisible(false);
    }

    @Override
    public String getName() {
        return "Meme";
    }

    @Override
    public void onPause() {
        if (mSelected == MID_TEXT_INDEX){
            if (midET.getText().toString().trim().isEmpty()){
                selectTextMode(0);
            }
        }
    }

    @Override
    public int getDrawable() {
        return R.drawable.meme_icon_selected;
    }
}
