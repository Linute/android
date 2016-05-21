package com.linute.linute.UtilsAndHelpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 5/16/16.
 */
public class LoadMoreViewHolder extends RecyclerView.ViewHolder {

    public static final int FOOTER = 15;

    public static final short STATE_LOADING = 0;
    public static final short STATE_ERROR = 1;
    public static final short STATE_END = 2;

    private View vProgressbar;
    private TextView vText;
    private short mState;

    private OnLoadMore mOnLoadMore;

    private String mErrorText;
    private String mDoneText;

    public LoadMoreViewHolder(View itemView, OnLoadMore o, String error, String done) {
        super(itemView);

        mErrorText = error;
        mDoneText = done;
        mOnLoadMore = o;
        vProgressbar = itemView.findViewById(R.id.progress_bar);
        vText = (TextView) itemView.findViewById(R.id.textView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == STATE_ERROR && mOnLoadMore != null){
                    mOnLoadMore.onLoadMore(true);
                    vProgressbar.setVisibility(View.VISIBLE);
                    vText.setVisibility(View.GONE);
                }
            }
        });
    }

    public void bindView(short state){
        mState = state;

        switch (state){
            case STATE_LOADING:
                vProgressbar.setVisibility(View.VISIBLE);
                vText.setVisibility(View.GONE);
                if (mOnLoadMore != null){
                    mOnLoadMore.onLoadMore(false);
                }
                break;
            case STATE_ERROR:
                vProgressbar.setVisibility(View.GONE);
                vText.setVisibility(View.VISIBLE);
                vText.setText(mErrorText);
                break;
            case STATE_END:
                vProgressbar.setVisibility(View.GONE);
                vText.setVisibility(View.VISIBLE);
                vText.setText(mDoneText);
                break;
        }
    }

    public interface OnLoadMore{
        void onLoadMore(boolean clicked);
    }
}
