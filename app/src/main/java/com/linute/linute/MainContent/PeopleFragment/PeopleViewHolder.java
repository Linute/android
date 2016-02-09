package com.linute.linute.MainContent.PeopleFragment;

import android.animation.ObjectAnimator;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.MainActivity;
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
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Arman on 1/8/16.
 */
public class PeopleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = PeopleViewHolder.class.getSimpleName();
    protected CircularImageView vProfilePicture;
    protected TextView vName;
    protected TextView vState;
    protected ImageView vStateImage;

    private Context vContext;
    private SharedPreferences mSharedPreferences;
    private List<People> vPeopleList;

    private TextView mStatus;

    public PeopleViewHolder(View itemView, Context context, List<People> peopleList) {
        super(itemView);

        vContext = context;
        mSharedPreferences = vContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        vPeopleList = peopleList;

        vProfilePicture = (CircularImageView) itemView.findViewById(R.id.people_user_image);
        vName = (TextView) itemView.findViewById(R.id.list_people_name);
        vState = (TextView) itemView.findViewById(R.id.list_people_state);
        vStateImage = (ImageView) itemView.findViewById(R.id.list_people_image_state);
        mStatus = (TextView) itemView.findViewById(R.id.people_status);

        vProfilePicture.setOnClickListener(this);
        vName.setOnClickListener(this);
        vStateImage.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {

        if (v == vProfilePicture || v == vName){
            BaseTaptActivity activity = (BaseTaptActivity) vContext;
            if (activity != null)
                activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(
                        vPeopleList.get(getAdapterPosition()).getName(),
                        vPeopleList.get(getAdapterPosition()).getID()));
        }

        if (v == vStateImage) {
            if (!vPeopleList.get(getAdapterPosition() /*- 4*/).isFriend()) {
                Map<String, Object> postData = new HashMap<>();
                postData.put("user", vPeopleList.get(getAdapterPosition() /*- 4*/).getID());

                new LSDKPeople(vContext).postFollow(postData, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                        Activity activity = (Activity) vContext;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(vContext);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        Activity activity = (Activity) vContext;


                        if (!response.isSuccessful()) {
                            Log.d(TAG, response.body().string());
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(vContext);
                                }
                            });
                            return;
                        }

                        response.body().close();
//                        Log.d(TAG, response.body().string());
                        ((Activity) vContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vStateImage.setImageResource(R.drawable.message_friend);
                                Toast.makeText(vContext, "You got a new friend!", Toast.LENGTH_SHORT).show();
                                vPeopleList.get(getAdapterPosition() /*- 4*/).setFriend(true);
                            }
                        });

                    }
                });
            } else {
                Intent enterRooms = new Intent(vContext, RoomsActivity.class);
                enterRooms.putExtra("CHATICON", false);
                enterRooms.putExtra("USERID", vPeopleList.get(getAdapterPosition() /*- 4*/).getID());
                vContext.startActivity(enterRooms);
            }
        }
    }

    void bindModel(People peeps) {
        Glide.with(vContext)
                .load(Utils.getImageUrlOfUser(peeps.getProfileImage()))
                .asBitmap()
                //.animate(animationObject)
                .placeholder(R.drawable.image_loading_background)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfilePicture);

        vName.setText(peeps.getName());
        vState.setText(peeps.getDate());

        mStatus.setText(vContext.getString(PlaceholderStatuses.getRandomStringRes(new Random().nextInt(41))));

        if (peeps.isFriend())
            vStateImage.setImageResource(R.drawable.message_friend);
        else
            vStateImage.setImageResource(R.drawable.add_friend);
    }

//    ViewPropertyAnimation.Animator animationObject = new ViewPropertyAnimation.Animator() {
//        @Override
//        public void animate(View view) {
//            // if it's a custom view class, cast it here
//            // then find subviews and do the animations
//            // here, we just use the entire view for the fade animation
//            view.setAlpha(0f);
//
//            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
//            fadeAnim.setDuration(2500);
//            fadeAnim.start();
//        }
//    };
}
