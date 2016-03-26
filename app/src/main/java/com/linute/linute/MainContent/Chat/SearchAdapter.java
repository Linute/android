package com.linute.linute.MainContent.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/19/16.
 */
public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SearchAdapter.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private Context aContext;
    protected List<SearchUser> mSearchUserList;

    public SearchAdapter(Context aContext, List<SearchUser> searchUserList) {
        this.aContext = aContext;
        mSearchUserList = searchUserList;
        mSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_search_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SearchViewHolder) holder).bindModel(mSearchUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSearchUserList.size();
    }


    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected LinearLayout vSearchItemLinear;
        protected CircleImageView vUserImage;
        protected TextView vUserName;
        protected String mUserId;
        protected String mUserName;

        public SearchViewHolder(View itemView) {
            super(itemView);

            vSearchItemLinear = (LinearLayout) itemView.findViewById(R.id.search_users_list_layout);
            vUserImage = (CircleImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);

            vSearchItemLinear.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            ((RoomsActivity) aContext).toggleFab(true);
//            ((RoomsActivity) aContext).getFragmentManager().popBackStack();

            Toast.makeText(aContext, mSearchUserList.get(getAdapterPosition()).getUserName(), Toast.LENGTH_SHORT).show();


            JSONArray users = new JSONArray();
            users.put(mSharedPreferences.getString("userID", ""));
            users.put(mUserId);


            new LSDKChat(aContext).getPastMessages(users, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: " + "NO INTERNET CONNECTION");

                    final Activity activity = (Activity) aContext;
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
                    final String resString = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(resString);

                            final String roomId = jsonObject.getString("id");
                            //final JSONArray users = jsonObject.getJSONArray("users");


                            final RoomsActivity activity = (RoomsActivity) aContext;
                            if (activity == null) return;

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.chat_container,
                                                    ChatFragment.newInstance(roomId,
                                                            mUserName,
                                                            mUserId
                                                            //,
//                                                            users.length(),
//                                                            new ArrayList<ChatHead>())
                                            ))
                                            .addToBackStack(null)
                                            .commit();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            final Activity activity = (Activity) aContext;
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(activity);
                                    }
                                });
                            }
                        }


                    } else {
                        Log.e(TAG, "run: " + resString);
                        final Activity activity = (Activity) aContext;
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(activity);
                                }
                            });
                        }
                    }
                }
            });


//            FragmentManager fragmentManager = ((RoomsActivity) aContext).getFragmentManager();
//            NewChatDialog newChatDialog = NewChatDialog.newInstance(mSearchUserList.get(getAdapterPosition()));
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            // For a little polish, specify a transition animation
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            // To make it fullscreen, use the 'content' root view as the container
//            // for the fragment, which is always the root view for the activity
//            transaction.add(R.id.fragment, newChatDialog)
//                    .addToBackStack(null).commit();
        }

        void bindModel(SearchUser user) {
            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(user.getUserImage()))
                    .asBitmap()
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            mUserId = user.getUserId();
            mUserName = user.getUserName();

            vUserName.setText(user.getUserName());
        }
    }
}
