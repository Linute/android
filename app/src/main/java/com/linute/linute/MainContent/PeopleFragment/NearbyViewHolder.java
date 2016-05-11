package com.linute.linute.MainContent.PeopleFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.linute.linute.API.API_Methods.VERSION;

/**
 * Created by QiFeng on 1/29/16.
 */
public class NearbyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ViewPager vImages;
    private TextView vName;
    private TextView vDistance;
    private TextView vStatus;
    private TextView vSchool;

    private TextView vRating1;
    private TextView vRating2;
    private TextView vRating3;
    private TextView vRating4;

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

        vImages = (ViewPager) itemView.findViewById(R.id.images);
        vImages.setOffscreenPageLimit(3);
        vName = (TextView) itemView.findViewById(R.id.name);
        vDistance = (TextView) itemView.findViewById(R.id.distance);
        vStatus = (TextView) itemView.findViewById(R.id.status);
        mActionButton = (ImageView) itemView.findViewById(R.id.action_button);
        vSchool = (TextView) itemView.findViewById(R.id.school);

        vRating1 = (TextView) itemView.findViewById(R.id.first);
        vRating2 = (TextView) itemView.findViewById(R.id.second);
        vRating3 = (TextView) itemView.findViewById(R.id.third);
        vRating4 = (TextView) itemView.findViewById(R.id.fourth);
        vRating1.setOnClickListener(this);
        vRating2.setOnClickListener(this);
        vRating3.setOnClickListener(this);
        vRating4.setOnClickListener(this);

    }

    public void bindView(People people) {
        mPerson = people;
        vName.setText(people.getName());
        vStatus.setText(people.getStatus());
        vSchool.setText(people.getSchoolName());

        //setting new adapter everytime. maybe there is a better idea?
        vImages.setAdapter(
                new NearbyImagesAdapter(
                        mContext,
                        people.getPersonRecentPosts(),
                        mPerson.getProfileImage(),
                        mPerson.getName(),
                        mPerson.getID(),
                        mImageSignature
                )
        );
        vDistance.setText(people.getDate());
        mActionButton.setImageResource(people.isFriend() ? R.drawable.message_friend : R.drawable.add_friend);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPerson.isFriend()) {
                    Intent enterRooms = new Intent(mContext, RoomsActivity.class);
                    enterRooms.putExtra("NOTIFICATION", LinuteConstants.MESSAGE);
                    enterRooms.putExtra("ownerID", mPerson.getID());
                    enterRooms.putExtra("ownerFullName", mPerson.getName());
                    enterRooms.putExtra("room", "");
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

        vRating1.setText(getNameForRatingObjectAtPosition(0));
        vRating2.setText(getNameForRatingObjectAtPosition(1));
        vRating3.setText(getNameForRatingObjectAtPosition(2));
        vRating4.setText(getNameForRatingObjectAtPosition(3));
        calculateAndShowNewView(mPerson.isAlreadyRated());
    }


    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity == null || mPerson == null || mPerson.isAlreadyRated()) return;

        if (!activity.socketConnected()) {
            Toast.makeText(activity, R.string.error_connect, Toast.LENGTH_SHORT).show();
            return;
        }

        int position;
        if (v == vRating1) {
            position = 0;
        } else if (v == vRating2) {
            position = 1;
        } else if (v == vRating3) {
            position = 2;
        } else {
            position = 3;
        }

        try {
            JSONObject result = new JSONObject();
            result.put("choice", mPerson.getRatingObjects().get(position).getKey());
            result.put("user", mPerson.getID());
            activity.emitSocket(VERSION + ":users:rate", result);
            mPerson.setAlreadyRated(true);
            mPerson.incrementRateAtPosition(position);
            calculateAndShowNewView(true);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity, "An error occurred. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateAndShowNewView(boolean calculate) {
        if (calculate) {
            int weightLeft = 100;
            int weight;

            LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) vRating1.getLayoutParams();
            weight = getWeightForObjectAtPosition(0);
            layout.weight = weight;
            vRating1.setLayoutParams(layout);
            vRating1.setBackgroundResource(R.color.post_color_5);
            weightLeft -= weight;

            layout = (LinearLayout.LayoutParams) vRating2.getLayoutParams();
            weight = getWeightForObjectAtPosition(1);
            layout.weight = weight;
            vRating2.setLayoutParams(layout);
            vRating2.setBackgroundResource(R.color.yellow_color);
            weightLeft -= weight;

            layout = (LinearLayout.LayoutParams) vRating3.getLayoutParams();
            weight = getWeightForObjectAtPosition(2);
            layout.weight = weight;
            vRating3.setLayoutParams(layout);
            vRating3.setBackgroundResource(R.color.green_color);
            weightLeft -= weight;

            layout = (LinearLayout.LayoutParams) vRating4.getLayoutParams();
            layout.weight = weightLeft;
            vRating4.setLayoutParams(layout);
            vRating4.setBackgroundResource(R.color.blue_color);
        } else {
            LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) vRating1.getLayoutParams();
            layout.weight = 25;
            vRating1.setLayoutParams(layout);
            vRating1.setBackgroundResource(R.color.twentyfive_black);

            layout = (LinearLayout.LayoutParams) vRating2.getLayoutParams();
            layout.weight = 25;
            vRating2.setLayoutParams(layout);
            vRating2.setBackgroundResource(R.color.fifty_black);

            layout = (LinearLayout.LayoutParams) vRating3.getLayoutParams();
            layout.weight = 25;
            vRating3.setLayoutParams(layout);
            vRating3.setBackgroundResource(R.color.twentyfive_black);

            layout = (LinearLayout.LayoutParams) vRating4.getLayoutParams();
            layout.weight = 25;
            vRating4.setLayoutParams(layout);
            vRating4.setBackgroundResource(R.color.fifty_black);
        }
    }

    private int getWeightForObjectAtPosition(int pos) {
        return (60 * mPerson.getRatingObjects().get(pos).getNumOfRates() / mPerson.getTotalRatings()) + 10;

    }

    private String getNameForRatingObjectAtPosition(int pos) {
        return mPerson.getRatingObjects().get(pos).getName();
    }


}
