package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by Arman on 1/20/16.
 */
public class RoomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = RoomsAdapter.class.getSimpleName();

    private Context aContext;
    private List<Rooms> mRoomsList;
    private SharedPreferences mSharedPreferences;


    public RoomsAdapter(Context aContext, List<Rooms> roomsList) {
        this.aContext = aContext;
        mRoomsList = roomsList;
        mSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RoomsViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_rooms_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((RoomsViewHolder) holder).bindModel(mRoomsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mRoomsList.size();
    }

    class RoomsViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vRoomsListLinear;
        protected CircularImageView vUserImage;
        protected TextView vUserName;
        protected TextView vLastMessage;

        public RoomsViewHolder(View itemView) {
            super(itemView);

            vRoomsListLinear = (LinearLayout) itemView.findViewById(R.id.rooms_list_linear);
            vUserImage = (CircularImageView) itemView.findViewById(R.id.rooms_user_image);
            vUserName = (TextView) itemView.findViewById(R.id.rooms_user_name);
            vLastMessage = (TextView) itemView.findViewById(R.id.rooms_user_last_message);

            vRoomsListLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(aContext, mRoomsList.get(getAdapterPosition()).getLastMessageUserName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: " + mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", "") + " fsfsfsf");
                    ChatFragment newFragment = ChatFragment.newInstance(mRoomsList.get(getAdapterPosition()).getRoomId(), mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", ""), mSharedPreferences.getString("userID", ""));
                    Log.d(TAG, "onClick: " + newFragment.getArguments().getString("username"));
                    FragmentTransaction transaction = ((RoomsActivity) aContext).getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.chat_container, newFragment);
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                    ((RoomsActivity) aContext).toggleFab(false);
                }
            });
        }

        void bindModel(Rooms room) {
//            Log.d(TAG, "bindModel: " + room.getLastMessageUserImage());
            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(room.getLastMessageUserImage()))
                    .asBitmap()
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            vUserName.setText(room.getLastMessageUserName());
            vLastMessage.setText(room.getLastMessage());
        }
    }
}
