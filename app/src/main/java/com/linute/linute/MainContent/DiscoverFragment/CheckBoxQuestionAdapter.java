package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.MultiChoiceMode;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionAdapter extends ChoiceCapableAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = CheckBoxQuestionAdapter.class.getSimpleName();
    private List<Post> mPosts;
    private Context context;
    private GetMoreFeed mGetMoreFeed; //interface that gets more feed

    public CheckBoxQuestionAdapter(List<Post> posts, Context context) {
        super(new MultiChoiceMode());
        mPosts = posts;
        this.context = context;
    }

    public void setGetMoreFeed(GetMoreFeed moreFeed){
        mGetMoreFeed = moreFeed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == IMAGE_POST){
            return new ImageFeedHolder(this,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_image, parent, false),
                    mPosts,
                    context);
        }
        else{
            return new StatusFeedHolder(this,
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_status, parent, false),
                    mPosts,
                    context);
        }

//        return new CheckBoxQuestionViewHolder(this, LayoutInflater.
//                from(parent.getContext()).
//                inflate(R.layout.feed_discover, parent, false), mPosts, context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageFeedHolder){
            ((ImageFeedHolder) holder).bindModel(mPosts.get(position));
        }else {
            ((StatusFeedHolder) holder).bindModel(mPosts.get(position));
        }

        //holder.bindModel(mPosts.get(position));
        if (position + 1 == mPosts.size()) {
            //NOTE: Changed refresh to interface
            mGetMoreFeed.getMoreFeed();
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static final int IMAGE_POST = 0;
    public static final int STATUS_POST = 1;

    @Override
    public int getItemViewType(int position) {
        return  mPosts.get(position).isImagePost() ? IMAGE_POST : STATUS_POST;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }



    public interface GetMoreFeed {
        void getMoreFeed();
    }

}
