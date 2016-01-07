package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.MultiChoiceMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private final Profile mProfile;

    private Context context;
    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private LinuteUser mUser;

    public ProfileAdapter(ArrayList<UserActivityItem> userActivityItems, LinuteUser user, Context context, Profile profile) {
        this.context = context;
        mProfile = profile;
        mUserActivityItems = userActivityItems;
        mUser = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            return new ProfileViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_profile2, parent, false), context);
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            return new ProfileHeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_profile_header2, parent, false), context, mProfile);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProfileViewHolder) {
            ((ProfileViewHolder) holder).bindModel(mUserActivityItems.get(position - 1));
        } else if (holder instanceof ProfileHeaderViewHolder) {
            ((ProfileHeaderViewHolder) holder).bindModel(mUser);
        }
    }


    @Override
    public int getItemCount() {
        return mUserActivityItems.size() + 1;
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