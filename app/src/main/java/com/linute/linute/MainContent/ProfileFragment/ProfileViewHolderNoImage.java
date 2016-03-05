package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

/**
 * Created by QiFeng on 2/29/16.
 */
public class ProfileViewHolderNoImage extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;

    private String mPostId;
    private String mUserId;

    private TextView mTextView;

    //private View vAnonIcon;


    public ProfileViewHolderNoImage(View itemView, Context context) {
        super(itemView);

        mContext = context;

        mTextView = (TextView) itemView.findViewById(R.id.profile_grid_item_no_image_text);
        mTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "Lato-LightItalic.ttf"));

        //vAnonIcon = itemView.findViewById(R.id.profile_frag_anon_icon);
        itemView.setOnClickListener(this);
    }

    void bindModel(UserActivityItem userActivityItem) {
        String text = userActivityItem.getDescription();

        mTextView.setText((text == null || text.equals("")) ? "No text..." : text);

        mPostId = userActivityItem.getEventID();
        mUserId = userActivityItem.getOwnerID();
    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null) {
            activity.addFragmentToContainer(FeedDetailPage.newInstance(false, false, mPostId, mUserId));
        }
    }
}
