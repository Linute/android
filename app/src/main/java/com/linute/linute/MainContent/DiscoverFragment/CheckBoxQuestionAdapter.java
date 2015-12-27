package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.MultiChoiceMode;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionAdapter extends ChoiceCapableAdapter<CheckBoxQuestionViewHolder> {
    private List<Post> mPosts;
    private Context context;

    public CheckBoxQuestionAdapter(List<Post> posts, Context context) {
        super(new MultiChoiceMode());
        mPosts = posts;
        this.context = context;
    }

    @Override
    public CheckBoxQuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CheckBoxQuestionViewHolder(this, LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.events_discover2, parent, false), mPosts, context);
    }

    @Override
    public void onBindViewHolder(CheckBoxQuestionViewHolder holder, int position) {
        holder.bindModel(mPosts.get(position));
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
