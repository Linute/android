package com.linute.linute.MainContent.PeopleFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by QiFeng on 1/29/16.
 */
public class NearbyViewHolder extends RecyclerView.ViewHolder implements ImageListener {

    private CarouselView vImages;
    private TextView vName;
    private TextView vDistance;
    private TextView vStatus;
    private TextView vSchool;

    private ImageView mActionButton;

    private String mImageSignature;

    private People mPerson;
    private Context mContext;

    public NearbyViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        mImageSignature = mContext
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("imageSigniture", "000");

        vImages = (CarouselView) itemView.findViewById(R.id.images);
        vName = (TextView) itemView.findViewById(R.id.name);
        vDistance = (TextView) itemView.findViewById(R.id.distance);
        vStatus = (TextView) itemView.findViewById(R.id.status);
        mActionButton = (ImageView) itemView.findViewById(R.id.action_button);
        vSchool = (TextView) itemView.findViewById(R.id.school);
        vImages.setImageListener(this);
    }

    public void bindView(People people) {
        mPerson = people;
        vName.setText(people.getName());
        vStatus.setText(people.getStatus());
        vSchool.setText(people.getSchoolName());

        vImages.setPageCount(people.getPersonRecentPosts().size() + 1);

        vDistance.setText(people.getDate());
        mActionButton.setImageResource(people.isFriend() ? R.drawable.message_friend : R.drawable.add_friend);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPerson.isFriend()) {
                    Intent enterRooms = new Intent(mContext, RoomsActivity.class);
                    enterRooms.putExtra("CHATICON", false);
                    enterRooms.putExtra("USERID", mPerson.getID());
                    mContext.startActivity(enterRooms);
                } else {

                    final Map<String, Object> postData = new HashMap<>();
                    postData.put("user", mPerson.getID());

                    new LSDKPeople(mContext).postFollow(postData, new Callback() {
                        final People p = mPerson;

                        @Override
                        public void onFailure(Call call, IOException e) {
                            Activity activity = ((Activity) mContext);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showBadConnectionToast(mContext);
                                }
                            });
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Activity activity = ((Activity) mContext);

                            if (!response.isSuccessful()) {
                                Log.d("NearbyViewHolder", response.body().string());
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showSavedToast(mContext);
                                    }
                                });
                                return;
                            }
                            response.body().close();
//                        Log.d(TAG, response.body().string());
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    p.setFriend(true);
                                    if (p == mPerson)
                                        mActionButton.setImageResource(R.drawable.message_friend);
                                }
                            });

                        }
                    });

                }
            }
        });
    }

    @Override
    public void setImageForPosition(int position, ImageView imageView) {
        if (position == 0) {
            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(mPerson.getProfileImage()))
                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .signature(new StringSignature(mImageSignature))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(
                                mPerson.getName(),
                                mPerson.getID()));
                    }
                }
            });
        } else {
            final People.PersonRecentPost post = mPerson.getPersonRecentPosts().get(position - 1);

            Glide.with(mContext)
                    .load(Utils.getEventImageURL(post.getImage()))
                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(FeedDetailPage.newInstance(
                                new Post(post.getImage(),
                                        post.getPostId(),
                                        mPerson.getID(),
                                        mPerson.getName())
                        ));
                    }
                }
            });
        }


    }
}
