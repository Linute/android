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
import java.util.Locale;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<GlobalChoiceItem> mGlobalChoiceItems;
    Context mContext;
    GoToTrend mGoToTrend;
    RequestManager mRequestManager;
    int hotColor;
    int friendColor;


    public GlobalChoicesAdapter(Context context, List<GlobalChoiceItem> list) {
        mGlobalChoiceItems = list;
        mContext = context;
        hotColor = ContextCompat.getColor(context, R.color.global_hot);
        friendColor = ContextCompat.getColor(context, R.color.global_friend);
    }

    public void setRequestManager(RequestManager manager) {
        mRequestManager = manager;
    }


    public void setGoToTrend(GoToTrend r) {
        mGoToTrend = r;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case GlobalChoiceItem.TYPE_TREND:
            case GlobalChoiceItem.TYPE_ARTICLE:
                return new TrendingChoiceViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.global_item, parent, false));
            case GlobalChoiceItem.TYPE_HEADER_FRIEND:
            case GlobalChoiceItem.TYPE_HEADER_HOT:
                return new HeaderViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.global_header_items, parent, false));
            case GlobalChoiceItem.TYPE_SECTION_TEXT:
                return  new HeaderItem(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.global_item_section_header, parent, false)
                );
            default:
                return new TrendingChoiceViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.global_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TrendingChoiceViewHolder) {
            ((TrendingChoiceViewHolder) holder).bindView(mGlobalChoiceItems.get(position));
        } else if (holder instanceof HeaderViewHolder){
            ((HeaderViewHolder) holder).bindView(mGlobalChoiceItems.get(position));
        }else {
            ((HeaderItem) holder).bindView(mGlobalChoiceItems.get(position));
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
        private TextView vNewPosts;
        private GlobalChoiceItem mGlobalChoiceItem;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            vParent = itemView.findViewById(R.id.parent);
            vTitle = (TextView) vParent.findViewById(R.id.text);
            vTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AbadiMTCondensedExtraBold.ttf"));
            vNewPosts = (TextView) vParent.findViewById(R.id.new_posts);
            itemView.setOnClickListener(this);
        }

        public void bindView(GlobalChoiceItem item) {
            mGlobalChoiceItem = item;

            vParent.getBackground().setColorFilter(
                    item.type == GlobalChoiceItem.TYPE_HEADER_FRIEND ? friendColor : hotColor,
                    PorterDuff.Mode.SRC_ATOP
            );

            vTitle.setText(item.title);
            String text = String.format(Locale.US, "%d %s", item.getUnread(), item.getUnread() == 1 ? "new post" : "new posts");
            vNewPosts.setText(text);
        }

        @Override
        public void onClick(View v) {
            if (mGoToTrend != null) {
                mGoToTrend.goToTrend(mGlobalChoiceItem);
            }
        }
    }

    public class HeaderItem extends RecyclerView.ViewHolder {

        TextView vTitle;

        public HeaderItem(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title_view);
        }

        public void bindView(GlobalChoiceItem item){
            vTitle.setText(item.title);
        }

    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    interface GoToTrend {
        void goToTrend(GlobalChoiceItem item);
    }
}
