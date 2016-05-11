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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/8/16.
 */
public class PeopleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = PeopleViewHolder.class.getSimpleName();
    protected ImageView vProfilePicture;
    protected TextView vName;
    protected ImageView vActionButton;
    protected ImageView vCrownImage;
    protected View vRibbon;

    private Context vContext;
    private SharedPreferences mSharedPreferences;

    private People mPerson;

    public PeopleViewHolder(View itemView, Context context) {
        super(itemView);

        vContext = context;
        mSharedPreferences = vContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        vProfilePicture = (ImageView) itemView.findViewById(R.id.profile_image);
        vName = (TextView) itemView.findViewById(R.id.name);
        vActionButton = (ImageView) itemView.findViewById(R.id.action_button);
        vCrownImage = (ImageView) itemView.findViewById(R.id.crown);
        vRibbon = itemView.findViewById(R.id.ribbon);

        itemView.setOnClickListener(this);
        vActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {

        if (v == vActionButton && mPerson != null) {
            if (!mPerson.isFriend()) {
                Map<String, Object> postData = new HashMap<>();
                postData.put("user", mPerson.getID());

                new LSDKPeople(vContext).postFollow(postData, new Callback() {
                    final People person = mPerson;

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
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                person.setFriend(true);
                                if (mPerson == person) //still referencing same object
                                    vActionButton.setImageResource(R.drawable.message_friend);
                            }
                        });
                    }
                });
            } else {
//                Intent enterRooms = new Intent(vContext, RoomsActivity.class);
//                enterRooms.putExtra("NOTIFICATION", LinuteConstants.MESSAGE);
//                enterRooms.putExtra("ownerID", mPerson.getID());
//                enterRooms.putExtra("ownerFullName", mPerson.getName());
//                enterRooms.putExtra("room", "");
//                vContext.startActivity(enterRooms);
            }
            return;
        }

        BaseTaptActivity activity = (BaseTaptActivity) vContext;

        if (mPerson != null && activity != null) {
            activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(
                    mPerson.getName(),
                    mPerson.getID()));
        }
    }

    void bindModel(People peeps) {
        Glide.with(vContext)
                .load(Utils.getImageUrlOfUser(peeps.getProfileImage()))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfilePicture);

        mPerson = peeps;
        vName.setText(peeps.getName());
        vActionButton.setImageResource(peeps.isFriend() ? R.drawable.message_friend : R.drawable.add_friend);

        switch (peeps.getRank()) {
            case 0:
                vCrownImage.setVisibility(View.VISIBLE);
                vRibbon.setVisibility(View.INVISIBLE);
                vCrownImage.setImageResource(R.drawable.ic_best_crown);
                break;
            case 1:
                vCrownImage.setVisibility(View.VISIBLE);
                vRibbon.setVisibility(View.INVISIBLE);
                vCrownImage.setImageResource(R.drawable.ic_second_crown);
                break;
            default:
                vCrownImage.setVisibility(View.INVISIBLE);
                vRibbon.setVisibility(View.VISIBLE);
                break;
        }
    }

}
