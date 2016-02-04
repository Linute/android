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
    private static final int TYPE_IMAGE_HEADER = 0;
    private static final int TYPE_STATUS_HEADER = 1;
    private static final int TYPE_ITEM = 2;
    private static final int TYPE_NO_COMMENTS = 3;

    private Context context;

    //    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private FeedDetail mFeedDetail;

    private boolean mIsImage;

    public FeedDetailAdapter(FeedDetail feedDetail, Context context, boolean isImage) {
        this.context = context;
        mFeedDetail = feedDetail;
        mIsImage = isImage;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            return new FeedDetailViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_feed_detail_page_list_item, parent, false), context);
        }
        //image post
        else if (viewType == TYPE_IMAGE_HEADER) { //TODO: FIX ME
            //inflate your layout and pass it to view holder
            return new FeedDetailHeaderViewHolder(this, LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_feed_detail_page_head, parent, false), context);
        } else if (viewType == TYPE_STATUS_HEADER) { //was a status post
            return new FeedDetailHeaderStatusViewHolder(this,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.feed_detail_header_status, parent, false), context);
        }else {
            return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedDetailViewHolder) {
            ((FeedDetailViewHolder) holder).bindModel(mFeedDetail.getComments().get(position - 1));
        } else if (holder instanceof FeedDetailHeaderViewHolder) { //// TODO: 2/4/16 fix
            ((FeedDetailHeaderViewHolder) holder).bindModel(mFeedDetail);
        } else if (holder instanceof FeedDetailHeaderStatusViewHolder) {
            ((FeedDetailHeaderStatusViewHolder) holder).bindModel(mFeedDetail);
        }
    }

    @Override
    public int getItemCount() {
        return mFeedDetail.getComments().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return mIsImage ? TYPE_IMAGE_HEADER : TYPE_STATUS_HEADER;

        if (mFeedDetail.getComments().get(0) == null)  //first item is no, means no comments
            return TYPE_NO_COMMENTS;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public FeedDetail getFeedDetail() {
        return mFeedDetail;
    }
}
