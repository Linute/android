package com.linute.linute.MainContent.ProfileFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.FriendsList.FriendsListFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.BlurBuilder;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ProfileHeaderViewHolder.class.getSimpleName();
    private final Profile mProfile;
    private final RecyclerView.Adapter mAdapter;
    protected CircularImageView vProfilePicture;
    protected TextView vStatusText;
    protected TextView vPosts;
    //protected TextView vFollowing;
    protected TextView vFollowers;
    protected TextView vCollegeName;

    protected View mFollowButton;
    protected View mChatButton;
    protected View mActionBarContainer;

    protected TextView mFollowingButtonText;

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private LinuteUser mUser;
    private JSONObject jsonObject;


    public ProfileHeaderViewHolder(RecyclerView.Adapter adapter, View itemView, Context context, final Profile profile) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mProfile = profile;
        mAdapter = adapter;

        vProfilePicture = (CircularImageView) itemView.findViewById(R.id.profilefrag_prof_image);
        vStatusText = (TextView) itemView.findViewById(R.id.profilefrag_status);
        vPosts = (TextView) itemView.findViewById(R.id.profilefrag_num_posts);
        vFollowers = (TextView) itemView.findViewById(R.id.profilefrag_num_followers);

        mFollowButton = itemView.findViewById(R.id.prof_header_follow_button);
        mChatButton = itemView.findViewById(R.id.prof_header_chat_button);
        mActionBarContainer = itemView.findViewById(R.id.prof_header_action_bar);
        mFollowingButtonText = (TextView) itemView.findViewById(R.id.prof_header_follow_button_text);


        vCollegeName = (TextView) itemView.findViewById(R.id.college_name);

        vProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: VIEW IMAGE
            }
        });

        mChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser.getUserID() == null) return;

                Intent enterRooms = new Intent(mContext, RoomsActivity.class);
                enterRooms.putExtra("CHATICON", false);
                enterRooms.putExtra("USERID", mUser.getUserID());
                mContext.startActivity(enterRooms);
            }
        });

        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUser.getUserID() == null) return;

                if (!mSharedPreferences.getString("userID", "").equals(mUser.getUserID())) {
                    if (mUser.getFriend().equals("")) {
                        Map<String, Object> postData = new HashMap<>();
                        postData.put("user", mUser.getUserID());

                        new LSDKPeople(mContext).postFollow(postData, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                final Activity activity = (Activity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.showBadConnectionToast(activity);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.d(TAG, response.body().string());
                                    return;
                                }
//                                Log.d(TAG, "onResponse: " + response.body().string());
                                jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response.body().string());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ((BaseTaptActivity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFollowButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.secondaryColor));
                                        mFollowingButtonText.setText("following");
                                        Toast.makeText(mContext, "You got a new friend!", Toast.LENGTH_SHORT).show();
                                        try {
                                            mUser.setFriendship(jsonObject.getString("id"));
                                            mUser.setFriend("NotEmpty");
                                            Log.d(TAG, "run: " + jsonObject.getString("id"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        });
                    } else {
                        Map<String, Object> putData = new HashMap<>();
                        putData.put("isDeleted", true);

                        new LSDKPeople(mContext).putUnfollow(putData, mUser.getFriendship(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                final Activity activity = (Activity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.showBadConnectionToast(activity);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.d(TAG, response.body().string());
                                } else {
                                    response.body().close();
                                    BaseTaptActivity activity1 = (BaseTaptActivity) mContext;

                                    if (activity1 == null) return;
                                    activity1.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFollowButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.follow_grey));
                                            mFollowingButtonText.setText("follow");
                                            Toast.makeText(mContext, "You've lost friend!", Toast.LENGTH_SHORT).show();
                                            mUser.setFriend("");
                                        }
                                    });
                                }


                            }
                        });
                    }
                }
            }
        });


        final BaseTaptActivity activity = (BaseTaptActivity) mContext;

        itemView.findViewById(R.id.prof_header_followers_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    activity.addFragmentToContainer(FriendsListFragment.newInstance(false, mUser.getUserID()));
                }
            }
        });
    }

    void bindModel(final LinuteUser user) {
        if (mUser == null)
            mUser = user;

        if (user.getStatus() != null ) {
            vStatusText.setText(user.getStatus().equals("") ? "No bio... :|" : user.getStatus());
        }
        vPosts.setText(String.valueOf(user.getPosts()));
        vFollowers.setText(String.valueOf(user.getFollowers()));
        vCollegeName.setText(user.getCollegeName());

        if (!mSharedPreferences.getString("userID", "").equals(user.getUserID())) {
            if (user.getFriend() != null && user.getFriend().equals("")) {
                mFollowButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.follow_grey));
                mFollowingButtonText.setText("follow");
            } else {
                mFollowButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.secondaryColor));
                mFollowingButtonText.setText("following");
            }
            mActionBarContainer.setVisibility(View.VISIBLE);
        }


        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfilePicture);

    }

}
