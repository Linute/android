package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;

/**
 * Created by QiFeng on 2/3/16.
 */
public class StatusFeedHolder extends BasePostFeedHolder {


    public static final String TAG = StatusFeedHolder.class.getSimpleName();

    protected TextView vStatus;
    protected View vStatusContainer; //so status is easier to press

    public StatusFeedHolder(View itemView, Context context, RequestManager manager, BaseFeedAdapter.PostAction action) {
        super(itemView, context, manager, action);

        vStatus = (TextView) itemView.findViewById(R.id.feedDetail_status_post);

        vStatusContainer = itemView.findViewById(R.id.feedDetail_status_container);

        vStatusContainer.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                vLikesHeart.toggle();
            }
        });
    }


    @Override
    public void bindModel(Post post) {
        super.bindModel(post);

        vStatus.setText(post.getTitle());
    }

}
