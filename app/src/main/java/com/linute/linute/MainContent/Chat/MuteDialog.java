package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by mikhail on 7/23/16.
 */
public class MuteDialog extends AlertDialog.Builder{

    private int mSelectedItem = 0;
    private int[] mItemValues;

    private DurationSelectedListener mDurationSelectedListener;

    public MuteDialog setDurationSelectedListener(DurationSelectedListener mDurationSelectedListener) {
        this.mDurationSelectedListener = mDurationSelectedListener;
        return this;
    }

    public MuteDialog(Context context, String[] itemText, int[] itemValues) {
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

    interface DurationSelectedListener{
        void onDurationSelected(int item);
    }
}
