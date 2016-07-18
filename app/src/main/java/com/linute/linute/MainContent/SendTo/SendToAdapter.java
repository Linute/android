package com.linute.linute.MainContent.SendTo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SendToItem> mSendToItems;
    private RequestManager mRequestManager;
    private HashSet<SendToItem> mCheckedItems;
    private ButtonAction mButtonAction;

    private StringSignature mImageSignature;
    private short mLoadState = LoadMoreViewHolder.STATE_LOADING;
    private LoadMoreViewHolder.OnLoadMore mOnLoadMore;

    public SendToAdapter(Context context, RequestManager manager, ArrayList<SendToItem> sendToItems) {
        mSendToItems = sendToItems;
        mRequestManager = manager;
        mCheckedItems = new HashSet<>();

        mImageSignature = new StringSignature(
                context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000")
        );
    }

    public void setLoadState(short state){
        mLoadState = state;
    }

    public void setOnLoadMore(LoadMoreViewHolder.OnLoadMore onLoadMore){
        mOnLoadMore = onLoadMore;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LoadMoreViewHolder.FOOTER){
            return new LoadMoreViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.wrapping_footer_dark, parent, false),
                    "",
                    ""
            );
        }
        else if (viewType == SendToItem.TYPE_PERSON)
            return new SendToPersonViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_send_to, parent, false)
            );
        else
            return new SendToTrendViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_send_to_trend, parent, false)
            );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseSendToViewHolder)
            ((BaseSendToViewHolder)holder).bindViews(mSendToItems.get(position));
        else if (holder instanceof LoadMoreViewHolder){
            ((LoadMoreViewHolder)holder).bindView(mLoadState);
            if (mOnLoadMore != null) mOnLoadMore.loadMore();
        }
    }

    @Override
    public int getItemCount() {
        return mSendToItems.size() > 0 ? mSendToItems.size() + 1 : 0;
    }

    public void setRequestManager(RequestManager manager) {
        mRequestManager = manager;
    }


    public void setButtonAction(ButtonAction buttonAction) {
        mButtonAction = buttonAction;
    }

    public HashSet<SendToItem> getCheckedItems() {
        return mCheckedItems;
    }


    @Override
    public int getItemViewType(int position) {
        return position == mSendToItems.size() ? LoadMoreViewHolder.FOOTER : mSendToItems.get(position).getType();
    }

    public abstract class BaseSendToViewHolder extends RecyclerView.ViewHolder {
        protected SendToItem mSendToItem;

        protected ImageView vImage;
        protected TextView vName;
        protected ToggleImageView vCheckBox;

        public BaseSendToViewHolder(View itemView) {
            super(itemView);
            vImage = (ImageView) itemView.findViewById(R.id.image);
            vName = (TextView) itemView.findViewById(R.id.name);
            vCheckBox = (ToggleImageView) itemView.findViewById(R.id.toggle);
            vCheckBox.setImageViews(R.drawable.send_to_checkbox_off, R.drawable.send_to_checkbox_on);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (vCheckBox.isActive()) {
                        vCheckBox.setActive(false);
                        mCheckedItems.remove(mSendToItem);
                        mSendToItem.setChecked(false);
                        if (mCheckedItems.isEmpty() && mButtonAction != null)
                            mButtonAction.turnOnButton(false);
                    } else {
                        vCheckBox.setActive(true);
                        mCheckedItems.add(mSendToItem);
                        mSendToItem.setChecked(true);
                        if (!mCheckedItems.isEmpty() && mButtonAction != null)
                            mButtonAction.turnOnButton(true);
                    }
                }
            });
        }


        public void bindViews(SendToItem item) {
            mSendToItem = item;

            vCheckBox.setActive(item.isChecked());
            vName.setText(item.getName());
            loadImage(item);
        }


        public abstract void loadImage(SendToItem item);
    }


    public class SendToPersonViewHolder extends BaseSendToViewHolder {


        public SendToPersonViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void loadImage(SendToItem item) {
            mRequestManager.load(Utils.getImageUrlOfUser(item.getImage()))
                    .dontAnimate()
                    .signature(mImageSignature)
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vImage);

        }
    }

    public class SendToTrendViewHolder extends BaseSendToViewHolder {

        public SendToTrendViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void loadImage(SendToItem item) {
            mRequestManager.load(Utils.getTrendsImageURL(item.getImage()))
                    .dontAnimate()
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vImage);
        }
    }


    public interface ButtonAction {
        void turnOnButton(boolean turnOn);
    }
}
