package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    //    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private FeedDetail mFeedDetail;

    public FeedDetailAdapter(FeedDetail feedDetail, Context context) {
        this.context = context;
        mFeedDetail = feedDetail;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            return new FeedDetailViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_feed_detail_page_list_item, parent, false), context);
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            return new FeedDetailHeaderViewHolder(this, LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_feed_detail_page_head, parent, false), context);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedDetailViewHolder) {
            ((FeedDetailViewHolder) holder).bindModel(mFeedDetail.getComments().get(position - 1));
        } else if (holder instanceof FeedDetailHeaderViewHolder) {
            ((FeedDetailHeaderViewHolder) holder).bindModel(mFeedDetail);
        }
    }

    @Override
    public int getItemCount() {
        return mFeedDetail.getComments().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}
