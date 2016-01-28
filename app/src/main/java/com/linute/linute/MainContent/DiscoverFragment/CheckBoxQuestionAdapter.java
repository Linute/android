package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.MultiChoiceMode;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionAdapter extends ChoiceCapableAdapter<CheckBoxQuestionViewHolder> {
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
    public CheckBoxQuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CheckBoxQuestionViewHolder(this, LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.feed_discover, parent, false), mPosts, context);
    }

    @Override
    public void onBindViewHolder(CheckBoxQuestionViewHolder holder, int position) {
        holder.bindModel(mPosts.get(position));
        if (position + 1 == mPosts.size()) {
            //NOTE: Changed refresh to interface
            mGetMoreFeed.getMoreFeed();
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }



    public interface GetMoreFeed {
        void getMoreFeed();
    }

}
