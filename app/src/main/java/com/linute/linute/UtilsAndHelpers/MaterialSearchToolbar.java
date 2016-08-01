package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.linute.linute.R;

/**
 * Created by QiFeng on 7/30/16.
 */
public class MaterialSearchToolbar extends Toolbar {

    private EditText editText;

    private SearchActions mSearchActions;

    public MaterialSearchToolbar(Context context) {
        super(context);
        init();
    }

    public MaterialSearchToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialSearchToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){
        inflate(getContext(), R.layout.material_search_toolbar, this);

        editText = (EditText) findViewById(R.id.search_view);
        final View clear = findViewById(R.id.action);

        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().clear();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSearchActions != null) mSearchActions.search(s.toString());

                clear.setVisibility(s.toString().isEmpty() ? INVISIBLE : VISIBLE);
            }
        });
    }


    public void setSearchActions(SearchActions searchActions){
        mSearchActions = searchActions;
    }

    public String getText(){
        return editText.getText().toString();
    }

    public interface SearchActions{
        void search(String query);
    }
}
