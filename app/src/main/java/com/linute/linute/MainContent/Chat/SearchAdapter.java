package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arman on 1/19/16.
 */
public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SearchAdapter.class.getSimpleName();
    private String mImageSign;
    private Context aContext;
    protected List<SearchUser> mSearchUserList;

    public SearchAdapter(Context aContext, List<SearchUser> searchUserList) {
        this.aContext = aContext;
        mSearchUserList = searchUserList;
        mImageSign = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000");

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_search_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SearchViewHolder) holder).bindModel(mSearchUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSearchUserList.size();
    }


    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected LinearLayout vSearchItemLinear;
        protected CircleImageView vUserImage;
        protected TextView vUserName;
        protected String mUserId;
        protected String mUserName;

        public SearchViewHolder(View itemView) {
            super(itemView);

            vSearchItemLinear = (LinearLayout) itemView.findViewById(R.id.search_users_list_layout);
            vUserImage = (CircleImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);

            vSearchItemLinear.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            BaseTaptActivity activity = (BaseTaptActivity) aContext;
            if (activity != null) {
                activity.getSupportFragmentManager().popBackStack();
                activity.addFragmentToContainer(ChatFragment.newInstance(null, mUserName, mUserId));
            }
        }

        void bindModel(SearchUser user) {
            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(user.getUserImage()))
                    .dontAnimate()
                    .signature(new StringSignature(mImageSign))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            mUserId = user.getUserId();
            mUserName = user.getUserName();

            vUserName.setText(user.getUserName());
        }
    }
}
