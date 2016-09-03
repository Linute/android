package com.linute.linute.MainContent.Global;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<GlobalChoiceItem> mGlobalChoiceItems;
    Context mContext;
    GoToTrend mGoToTrend;
    RequestManager mRequestManager;


    public GlobalChoicesAdapter(Context context, List<GlobalChoiceItem> list) {
        mGlobalChoiceItems = list;
        mContext = context;
    }

    public void setRequestManager(RequestManager manager) {
        mRequestManager = manager;
    }


    public void setGoToTrend(GoToTrend r) {
        mGoToTrend = r;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == GlobalChoiceItem.TYPE_TREND)
            return new TrendingChoiceViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.global_item, parent, false));

        return new HeaderViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.global_header_items, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TrendingChoiceViewHolder) {
            ((TrendingChoiceViewHolder) holder).bindView(mGlobalChoiceItems.get(position));
        } else {
            ((HeaderViewHolder) holder).bindView(mGlobalChoiceItems.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mGlobalChoiceItems.get(position).type;
    }

    @Override
    public int getItemCount() {
        return mGlobalChoiceItems.size();
    }

    public class TrendingChoiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView vTitle;
        private ImageView vImage;
        private TextView vText;
        private View vIndicator;

        private GlobalChoiceItem mGlobalChoiceItem;

        public TrendingChoiceViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.text);
            vTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AbadiMTCondensedExtraBold.ttf"));
            vImage = (ImageView) itemView.findViewById(R.id.background);
            vText = (TextView) itemView.findViewById(R.id.text1);
            vIndicator = itemView.findViewById(R.id.indicator);
            itemView.setOnClickListener(this);
        }


        public void bindView(GlobalChoiceItem item) {
            vTitle.setText(item.title);
            mGlobalChoiceItem = item;
            vIndicator.setVisibility(item.hasUnread() ? View.VISIBLE : View.GONE);
            mRequestManager
                    .load(Utils.getTrendsImageURL(item.imageUrl))
                    .dontAnimate()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .placeholder(R.color.seperator_color)
                    .into(vImage);

            vText.setText(item.description);
        }

        @Override
        public void onClick(View v) {
            if (mGoToTrend != null) {
                mGoToTrend.goToTrend(mGlobalChoiceItem);
            }
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View vParent;
        private TextView vTitle;
        private ImageView vImageView;
        private GlobalChoiceItem mGlobalChoiceItem;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            vParent = itemView.findViewById(R.id.parent);
            vTitle = (TextView) vParent.findViewById(R.id.text);
            vTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AbadiMTCondensedExtraBold.ttf"));
            vImageView = (ImageView) vParent.findViewById(R.id.image);
            vImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.pure_white), PorterDuff.Mode.SRC_ATOP);

            itemView.setOnClickListener(this);
        }

        public void bindView(GlobalChoiceItem item) {
            mGlobalChoiceItem = item;
            vParent.getBackground().setColorFilter(
                    ContextCompat.getColor(
                            mContext,
                            item.type == GlobalChoiceItem.TYPE_HEADER_FRIEND ?
                                    R.color.global_friend_orange :
                                    R.color.global_hot_red
                    )
                    , PorterDuff.Mode.SRC_ATOP
            );

            vImageView.setImageResource(
                    item.type == GlobalChoiceItem.TYPE_HEADER_FRIEND ?
                            R.drawable.ic_friends :
                            R.drawable.ic_fire_off
            );

            vTitle.setText(item.title);
        }

        @Override
        public void onClick(View v) {
            if (mGoToTrend != null) {
                mGoToTrend.goToTrend(mGlobalChoiceItem);
            }
        }
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    interface GoToTrend {
        void goToTrend(GlobalChoiceItem item);
    }
}
