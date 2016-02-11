package com.linute.linute.MainContent.PeopleFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.PlaceholderStatuses;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by QiFeng on 1/29/16.
 */
public class NearbyViewHolder extends RecyclerView.ViewHolder {

    private CircularImageView mProfileImage;
    private TextView mName;
    private TextView mDistance;
    private TextView mStatus;
    private ImageView mActionButton;


    private Context mContext;

    public NearbyViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;

        mProfileImage = (CircularImageView) itemView.findViewById(R.id.peopleNearby_profile_image);
        mName = (TextView) itemView.findViewById(R.id.peopleNearby_person_name);
        mDistance = (TextView) itemView.findViewById(R.id.peopleNearby_distance);
        mStatus = (TextView) itemView.findViewById(R.id.peopleNearby_status);
        mActionButton = (ImageView) itemView.findViewById(R.id.peopleNearby_action_button);
    }

    public void bindView(final People people) {
        mName.setText(people.getName());

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(people.getProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .into(mProfileImage);

        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseTaptActivity) mContext).addFragmentToContainer(
                        TaptUserProfileFragment.newInstance(people.getName(), people.getID()));
            }
        };

        mProfileImage.setOnClickListener(lis);
        mName.setOnClickListener(lis);

        mStatus.setText(mContext.getString(PlaceholderStatuses.getRandomStringRes(new Random().nextInt(41))));


        mDistance.setText(people.getDate());

        mActionButton.setImageResource(people.isFriend() ? R.drawable.message_friend : R.drawable.add_friend);


        mActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (people.isFriend()) {
                    Intent enterRooms = new Intent(mContext, RoomsActivity.class);
                    enterRooms.putExtra("CHATICON", false);
                    enterRooms.putExtra("USERID", people.getID());
                    mContext.startActivity(enterRooms);
                } else {

                    final Map<String, Object> postData = new HashMap<>();
                    postData.put("user", people.getID());

                    new LSDKPeople(mContext).postFollow(postData, new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
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
                        public void onResponse(Response response) throws IOException {
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
                                    mActionButton.setImageResource(R.drawable.message_friend);
                                    Toast.makeText(mContext, "You got a new friend!", Toast.LENGTH_SHORT).show();
                                    people.setFriend(true);
                                }
                            });

                        }
                    });

                }
            }
        });
    }


}
