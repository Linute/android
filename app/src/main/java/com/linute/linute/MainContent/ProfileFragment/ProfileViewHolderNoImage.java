package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by QiFeng on 2/29/16.
 */
public class ProfileViewHolderNoImage extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;
    private TextView mTextView;

    //private View vAnonIcon;

    private UserActivityItem mUserActivityItem ;


    public ProfileViewHolderNoImage(View itemView, Context context) {
        super(itemView);

        mContext = context;

        mTextView = (TextView) itemView.findViewById(R.id.profile_grid_item_no_image_text);
        mTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "Lato-LightItalic.ttf"));

        //vAnonIcon = itemView.findViewById(R.id.profile_frag_anon_icon);
        itemView.setOnClickListener(this);
    }

    void bindModel(UserActivityItem userActivityItem) {
        String text = userActivityItem.getPostText();

        mTextView.setText((text == null || text.equals("")) ? "No text..." : text);
        mUserActivityItem = userActivityItem;
    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null && mUserActivityItem != null) {
            activity.addFragmentToContainer(FeedDetailPage.newInstance(mUserActivityItem.getPost()));
        }
    }
}
