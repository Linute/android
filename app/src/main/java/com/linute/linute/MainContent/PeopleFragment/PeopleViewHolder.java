package com.linute.linute.MainContent.PeopleFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/8/16.
 */
public class PeopleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = PeopleViewHolder.class.getSimpleName();
    protected CircleImageView vProfilePicture;
    protected TextView vName;
    protected View vStateButton;
    private TextView mStatus;
    private TextView vTime;

    private Context vContext;
    private SharedPreferences mSharedPreferences;
    private List<People> vPeopleList;

    public PeopleViewHolder(View itemView, Context context, List<People> peopleList) {
        super(itemView);

        vContext = context;
        mSharedPreferences = vContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        vPeopleList = peopleList;

        vProfilePicture = (CircleImageView) itemView.findViewById(R.id.people_user_image);
        vName = (TextView) itemView.findViewById(R.id.people_name);
        vStateButton =  itemView.findViewById(R.id.people_state_button_container);
        mStatus = (TextView) itemView.findViewById(R.id.people_status);
        vTime = (TextView) itemView.findViewById(R.id.people_time_or_active_text);

        vProfilePicture.setOnClickListener(this);
        vName.setOnClickListener(this);
        vStateButton.setOnClickListener(this);
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

        if (v == vStateButton) {
            if (!vPeopleList.get(getAdapterPosition() /*- 4*/).isFriend()) {
                Map<String, Object> postData = new HashMap<>();
                postData.put("user", vPeopleList.get(getAdapterPosition() /*- 4*/).getID());

                new LSDKPeople(vContext).postFollow(postData, new Callback() {
                    final int position = getAdapterPosition();
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Activity activity = (Activity) vContext;
                        if (activity == null) return;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(vContext);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Activity activity = (Activity) vContext;

                        if (activity == null) return;

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
                        ((Activity) vContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //vStateImage.setImageResource(R.drawable.message_friend);
                                Toast.makeText(vContext, "You got a new friend!", Toast.LENGTH_SHORT).show();
                                vPeopleList.get(position /*- 4*/).setFriend(true);
                                TextView v = ((TextView)vStateButton.findViewById(R.id.people_action_add_text));
                                v.setText("Chat");
                                v.setTextColor(ContextCompat.getColor(vContext, R.color.secondaryColor));
                                ((ImageView)vStateButton.findViewById(R.id.people_action_icon)).setImageResource(R.drawable.ic_chat_people);
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
        vTime.setText(peeps.getDate());

        mStatus.setText(peeps.getStatus());

        if (peeps.isFriend()){
            TextView v = (TextView)vStateButton.findViewById(R.id.people_action_add_text);
            v.setText("Chat");
            v.setTextColor(ContextCompat.getColor(vContext, R.color.secondaryColor));
            ((ImageView)vStateButton.findViewById(R.id.people_action_icon)).setImageResource(R.drawable.ic_chat_people);
        }

        else {
            TextView v = (TextView)vStateButton.findViewById(R.id.people_action_add_text);
            v.setText("Follow");
            v.setTextColor(ContextCompat.getColor(vContext, R.color.fifty_black));
            ((ImageView)vStateButton.findViewById(R.id.people_action_icon)).setImageResource(R.drawable.ic_action_add_people);
        }
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
