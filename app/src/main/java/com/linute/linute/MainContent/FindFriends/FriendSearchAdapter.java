package com.linute.linute.MainContent.FindFriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 1/16/16.
 */
public class FriendSearchAdapter extends RecyclerView.Adapter<FriendSearchAdapter.FriendSearchViewHolder> {

    private List<FriendSearchUser> mFriendSearchList;
    private Context mContext;

    //private int mImageSize;

    private SharedPreferences mSharedPreferences;

    private boolean mInInviteMode = false;


    public FriendSearchAdapter(Context context, List<FriendSearchUser> mSearchList, boolean inInviteMode) {
        mFriendSearchList = mSearchList;
        mContext = context;
        //mImageSize = mContext.getResources().getDimensionPixelSize(R.dimen.friend_search_profile_radius);

        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mInInviteMode = inInviteMode;
    }


    @Override
    public FriendSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendSearchViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.friend_search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FriendSearchViewHolder holder, int position) {
        holder.bindViews(mFriendSearchList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFriendSearchList.size();
    }

    public class FriendSearchViewHolder extends RecyclerView.ViewHolder {

        private TextView mNameView;
        private ImageView mAddButton;
        private ImageView mProfileImage;

        public FriendSearchViewHolder(View itemView) {
            super(itemView);

            mNameView = (TextView) itemView.findViewById(R.id.friendSearchItem_full_name);
            mProfileImage = (ImageView) itemView.findViewById(R.id.friendSearchItem_profile_image);
            mAddButton = (ImageView) itemView.findViewById(R.id.friendSearchItem_add_button);
        }


        public void bindViews(FriendSearchUser user) {
            mNameView.setText(user.getFullName());

            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                    .asBitmap()
                    //.override(mImageSize, mImageSize)
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);

            setUpFollowButton(user.isFollowing(), user.getUserId(), user.getFullName());
            setUpOnClickListeners(user.getFullName(), user.getUserId());
        }


        private void setUpFollowButton(boolean areFriends, final String userID, final String name) {

            if (!areFriends) {

                mAddButton.setImageResource(R.drawable.add_friend); //plus icon

                mAddButton.setOnClickListener(new View.OnClickListener() { //when pressed

                    boolean mFollowed = false; //if we are following other person

                    @Override
                    public void onClick(View v) {

                        if (mFollowed) {
                            BaseTaptActivity activity = (BaseTaptActivity) mContext;
                            if (activity != null) {
                                activity.addFragmentToContainer(ChatFragment.newInstance(null, name, userID));
                            }
                        }

                        mFollowed = true;
                        mAddButton.setImageResource(R.drawable.message_friend); //change icon
                        Map<String, Object> params = new HashMap<>();
                        params.put("user", userID);

                        final Activity activity = ((Activity) mContext);

                        new LSDKPeople(mContext).postFollow(params, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("UpdatesAdapter", "No internet connection");

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFollowed = false;
                                        Utils.showBadConnectionToast(activity);
                                        mAddButton.setImageResource(R.drawable.add_friend);
                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.i("Update Adapter", "onResponse: " + response.body().string());
                                if (!response.isSuccessful()) { //unsuccessful, undo button change
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFollowed = false;
                                            Utils.showServerErrorToast(activity);
                                            mAddButton.setImageResource(R.drawable.add_friend);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }

            //are following person so hide button
            else {
                mAddButton.setImageResource(R.drawable.message_friend);

                mAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseTaptActivity activity = (BaseTaptActivity) mContext;
                        if (activity != null) {
                            activity.addFragmentToContainer(ChatFragment.newInstance(null, name, userID));
                        }
                    }
                });
            }
        }

        private void setUpOnClickListeners(final String name, final String userId) {
            View.OnClickListener goToProfile = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: fix
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(name, userId));
                    }
                }
            };

            mNameView.setOnClickListener(goToProfile);
            mProfileImage.setOnClickListener(goToProfile);
        }

    }
}
