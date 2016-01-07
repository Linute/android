package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.Settings.ChangeProfileImageActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BlurBuilder;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
    private final Profile mProfile;
    protected CircularImageView vProfilePicture;
    protected TextView vStatusText;
    protected TextView vPosts;
    protected TextView vFollowing;
    protected TextView vFollowers;
    protected ImageView vBlurBack;

    private Context mContext;
    private SharedPreferences mSharedPreferences;


    public ProfileHeaderViewHolder(View itemView, Context context, Profile profile) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mProfile = profile;

        vProfilePicture = (CircularImageView) itemView.findViewById(R.id.profilefrag_prof_image);
        vStatusText = (TextView) itemView.findViewById(R.id.profilefrag_status);
        vPosts = (TextView) itemView.findViewById(R.id.profilefrag_num_posts);
        vFollowers = (TextView) itemView.findViewById(R.id.profilefrag_num_followers);
        vFollowing = (TextView) itemView.findViewById(R.id.profilefrag_num_following);
        vBlurBack = (ImageView) itemView.findViewById(R.id.profile_blur_back);

        vProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfile.editProfileImage();
            }
        });
    }

    void bindModel(LinuteUser user) {
        vStatusText.setText(user.getStatus());
        vPosts.setText(String.valueOf(user.getPosts()));
        vFollowing.setText(String.valueOf(user.getFollowing()));
        vFollowers.setText(String.valueOf(user.getFollowers()));

        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfilePicture);

        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        // Do something with bitmap here.
                        Drawable drawable = mContext.getResources().getDrawable(R.drawable.college_walk1, null);
                        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();
                        Bitmap blurredBitmap = BlurBuilder.blur(mContext, originalBitmap);
                        vBlurBack.setBackground(new BitmapDrawable(mContext.getResources(), blurredBitmap));
//                        vBlurBack.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                    }
                });
    }
}