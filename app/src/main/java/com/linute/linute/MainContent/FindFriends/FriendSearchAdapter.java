package com.linute.linute.MainContent.FindFriends;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
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
    private RequestManager mRequestManager;

    private SharedPreferences mSharedPreferences;


    public FriendSearchAdapter(Context context, RequestManager manager,List<FriendSearchUser> mSearchList) {
        mFriendSearchList = mSearchList;
        mContext = context;
        mRequestManager = manager;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void clearContext(){
        mContext = null;
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

        private FriendSearchUser mFriendSearchUser;

        public FriendSearchViewHolder(View itemView) {
            super(itemView);

            mNameView = (TextView) itemView.findViewById(R.id.friendSearchItem_full_name);
            mProfileImage = (ImageView) itemView.findViewById(R.id.friendSearchItem_profile_image);
            mAddButton = (ImageView) itemView.findViewById(R.id.friendSearchItem_add_button);
            setUpOnClickListeners();

        }


        public void bindViews(FriendSearchUser user) {
            mFriendSearchUser = user;

            mNameView.setText(user.getFullName());

            mRequestManager
                    .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                    .dontAnimate()
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);

            mAddButton.setImageResource(mFriendSearchUser.isFollowing() ?
                    R.drawable.message_friend : R.drawable.add_friend);
        }


        private void setUpOnClickListeners() {

            //setup profile pic and name
            View.OnClickListener goToProfile = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFriendSearchUser == null) return;
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mFriendSearchUser.getFullName(), mFriendSearchUser.getUserId()));
                    }
                }
            };

            mNameView.setOnClickListener(goToProfile);
            mProfileImage.setOnClickListener(goToProfile);

            //the add or message button
            mAddButton.setOnClickListener(new View.OnClickListener() { //when pressed
                @Override
                public void onClick(View v) {

                    if (mFriendSearchUser == null) return;

                    //are friends: message them
                    if (mFriendSearchUser.isFollowing()) {
                        BaseTaptActivity activity = (BaseTaptActivity) mContext;
                        if (activity != null) {
                            activity.addFragmentToContainer(ChatFragment.newInstance(null, mFriendSearchUser.getFullName(), mFriendSearchUser.getUserId()));
                        }
                    }

                    //add them
                    else {

                        if (mContext == null) return;

                        mAddButton.setImageResource(R.drawable.message_friend); //change icon
                        mFriendSearchUser.setFollowing(true);
                        Map<String, Object> params = new HashMap<>();
                        params.put("user", mFriendSearchUser.getUserId());

                        new LSDKPeople(mContext).postFollow(params, new Callback() {
                            FriendSearchUser user = mFriendSearchUser;

                            @Override
                            public void onFailure(Call call, IOException e) {
                                user.setFollowing(false);
                                BaseTaptActivity activity = (BaseTaptActivity) mContext;
                                if (activity == null) return;
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showBadConnectionToast(mContext);
                                        if (mFriendSearchUser.getUserId().equals(user.getUserId()))
                                            mAddButton.setImageResource(R.drawable.add_friend);
                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.i("Update Adapter", "onResponse: " + response.body().string());
                                if (!response.isSuccessful()) { //unsuccessful, undo button change
                                    user.setFollowing(false);

                                    BaseTaptActivity activity = (BaseTaptActivity) mContext;
                                    if (activity == null) return;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.showServerErrorToast(mContext);
                                            if (mFriendSearchUser.getUserId().equals(user.getUserId()))
                                                mAddButton.setImageResource(R.drawable.add_friend);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }

}
