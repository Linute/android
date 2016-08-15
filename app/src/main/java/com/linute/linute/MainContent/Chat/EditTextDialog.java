package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.linute.linute.R;

/**
 * Created by mikhail on 7/24/16.
 */
public class EditTextDialog extends AlertDialog.Builder{

    EditText mEditText;


    public EditTextDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_text, null, false);
        mEditText = (EditText)view.findViewById(R.id.edit_text);
        setView(view);
    }

    public EditText getEditText(){
        return mEditText;
    }


    public EditTextDialog setValue(CharSequence value){
        mEditText.setText(value);
        return this;
    }

    public String getValue(){
        return mEditText.getText().toString();
    }


}
