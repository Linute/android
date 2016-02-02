package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.BlurBuilder;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    protected TextView vFollowing;
    protected TextView vFollowers;
    protected TextView vCollegeName;
    protected ImageView vFollowStatus;

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private LinuteUser mUser;
    private JSONObject jsonObject;


    public ProfileHeaderViewHolder(RecyclerView.Adapter adapter, View itemView, Context context, Profile profile) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mProfile = profile;
        mAdapter = adapter;

        vProfilePicture = (CircularImageView) itemView.findViewById(R.id.profilefrag_prof_image);
        vStatusText = (TextView) itemView.findViewById(R.id.profilefrag_status);
        vPosts = (TextView) itemView.findViewById(R.id.profilefrag_num_posts);
        vFollowers = (TextView) itemView.findViewById(R.id.profilefrag_num_followers);
        vFollowing = (TextView) itemView.findViewById(R.id.profilefrag_num_following);
        vFollowStatus = (ImageView) itemView.findViewById(R.id.follow_button);

        vCollegeName = (TextView) itemView.findViewById(R.id.college_name);

        vProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSharedPreferences.getString("userID", "").equals(mUser.getUserID())) {
                    //TODO: VIEW IMAGE
                }
            }
        });

        vFollowStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSharedPreferences.getString("userID", "").equals(mUser.getUserID())) {
                    if (mUser.getFriend().equals("")) {
                        Map<String, Object> postData = new HashMap<>();
                        postData.put("user", mUser.getUserID());

                        new LSDKPeople(mContext).postFollow(postData, new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.d(TAG, response.body().string());
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
                                        vFollowStatus.setImageResource(R.drawable.unfollowing);
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
                            public void onFailure(Request request, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.d(TAG, response.body().string());
                                }else {
                                    response.body().close();
                                    ((MainActivity) mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            vFollowStatus.setImageResource(R.drawable.follow);
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
    }

    void bindModel(LinuteUser user) {
        if (mUser == null)
            mUser = user;
        vStatusText.setText(user.getStatus());
        vPosts.setText(String.valueOf(user.getPosts()));
        vFollowing.setText(String.valueOf(user.getFollowing()));
        vFollowers.setText(String.valueOf(user.getFollowers()));
        vCollegeName.setText(user.getCollegeName());

        if (!mSharedPreferences.getString("userID", "").equals(user.getUserID())) {
            if (user.getFriend() != null && user.getFriend().equals("")) {
                vFollowStatus.setImageResource(R.drawable.follow);
            } else {
                vFollowStatus.setImageResource(R.drawable.unfollowing);
            }
            vFollowStatus.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfilePicture);

    }
}
