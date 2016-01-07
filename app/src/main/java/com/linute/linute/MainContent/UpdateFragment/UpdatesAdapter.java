package com.linute.linute.MainContent.UpdateFragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.SectionedRecyclerViewAdapter;

import java.util.List;

/**
 * Created by QiFeng on 1/6/16.
 */
public class UpdatesAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {

    //items in section one
    private List<Update> mRecentItems;

    //items in section two
    private List<Update> mOlderItems;

    public UpdatesAdapter(List<Update> recentItems, List<Update> olderItems){
        mRecentItems = recentItems;
        mOlderItems = olderItems;
    }


    @Override
    public int getSectionCount() {
        //if both empty, 0
        //one empty, 1
        //both non-empty, 2
        return (mRecentItems.isEmpty() ? 0 : 1) + (mOlderItems.isEmpty() ? 0 : 1);
    }

    @Override
    public int getItemCount(int section) {
        switch (section){
            case 0:
                return mRecentItems.size();
            case 1:
                return mOlderItems.size();
            default:
                return 0;
        }
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int section) {

        UpdateItemHeaderViewHolder tHolder = (UpdateItemHeaderViewHolder) holder;
        if (holder == null) return;

        if (getSectionCount() == 1){ //either recent or older is empty
            if (mRecentItems.isEmpty())  //older is not empty
                tHolder.setTitleText("Older");
            else //recent is not empty
                tHolder.setTitleText("Recent");
        }else if(getSectionCount() == 2){ //both non-empty
            if (section == 0) //recent
                tHolder.setTitleText("Recent");
            else //older
                tHolder.setTitleText("Older");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        UpdateItemViewHolder tHolder = (UpdateItemViewHolder) holder;
        if (holder == null) return;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }


    public static class UpdateItemHeaderViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;

        public UpdateItemHeaderViewHolder(View itemView) {
            super(itemView);
            mTitleText = (TextView) itemView.findViewById(R.id.updateFragment_title_text);
        }

        public void setTitleText(String title){
            mTitleText.setText(title);
        }
    }

    public static class UpdateItemViewHolder extends RecyclerView.ViewHolder{

        public UpdateItemViewHolder(View itemView) {
            super(itemView);
        }

        public void bindView(Update update){

        }
    }

}
