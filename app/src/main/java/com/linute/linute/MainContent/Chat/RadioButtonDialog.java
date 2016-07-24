package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by mikhail on 7/23/16.
 */
public class RadioButtonDialog<T> extends AlertDialog.Builder{

    private int mSelectedItem = 0;
    private T[] mItemValues;

    private DurationSelectedListener<T> mDurationSelectedListener;

    public RadioButtonDialog setDurationSelectedListener(DurationSelectedListener<T> mDurationSelectedListener) {
        this.mDurationSelectedListener = mDurationSelectedListener;
        return this;
    }

    public RadioButtonDialog(Context context, String[] itemText, T[] itemValues) {
        super(context);
        this.mItemValues = itemValues;
        setSingleChoiceItems(itemText, mSelectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectedItem = i;
            }
        })
                .setTitle("Mute for")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(mDurationSelectedListener != null){
                            mDurationSelectedListener.onDurationSelected(mItemValues[mSelectedItem]);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
    }

    interface DurationSelectedListener<T>{
        void onDurationSelected(T item);
    }
}
