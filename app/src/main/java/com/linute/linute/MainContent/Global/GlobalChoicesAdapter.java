package com.linute.linute.MainContent.Global;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoicesAdapter extends RecyclerView.Adapter <GlobalChoicesAdapter.TrendingChoiceViewHolder> {

    List<GlobalChoiceItem> mGlobalChoiceItems;
    Context mContext;
    GoToTrend mGoToTrend;


    public GlobalChoicesAdapter(Context context, List<GlobalChoiceItem> list){
        mGlobalChoiceItems = list;
        mContext = context;
    }

    public void setGoToTrend(GoToTrend r){
        mGoToTrend = r;
    }

    @Override
    public TrendingChoiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrendingChoiceViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.global_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TrendingChoiceViewHolder holder, int position) {
        holder.bindView(mGlobalChoiceItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mGlobalChoiceItems.size();
    }

    public class TrendingChoiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView vTitle;
        private ImageView vImage;
        private TextView vText;

        private GlobalChoiceItem mGlobalChoiceItem;

        public TrendingChoiceViewHolder(View itemView) {
            super(itemView);


            vTitle = (TextView) itemView.findViewById(R.id.text);
            vTitle.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "AbadiMTCondensedExtraBold.ttf"));
            vImage = (ImageView) itemView.findViewById(R.id.background);
            vText = (TextView) itemView.findViewById(R.id.text1);
            itemView.setOnClickListener(this);
        }



        public void bindView(GlobalChoiceItem item){
            vTitle.setText(item.title);

            mGlobalChoiceItem = item;

            if (item.hasUnread()){
                vImage.clearColorFilter();
            }else {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                vImage.setColorFilter(new ColorMatrixColorFilter(matrix));
            }

            //vImage.setImageResource(R.color.seperator_color);
            Glide.with(mContext)
                    .load(Utils.getTrendsImageURL(item.imageUrl))
                    .dontAnimate()
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(vImage);

            vText.setText("Temp");
        }

        @Override
        public void onClick(View v) {
            if (mGoToTrend != null){
                mGoToTrend.goToTrend(mGlobalChoiceItem.key, mGlobalChoiceItem.title);
            }
        }
    }

    interface GoToTrend{
        void goToTrend(String id, String title);
    }
}
