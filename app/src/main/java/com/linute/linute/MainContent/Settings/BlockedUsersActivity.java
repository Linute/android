package com.linute.linute.MainContent.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.Chat.User;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 9/24/16.
 */
public class BlockedUsersActivity extends Activity {

    Socket mSocket;
    SharedPreferences mSharedPreferences;

    boolean mConnecting = false;

    private ArrayList<BlockedUser> mBlockedUserList;
    private BlockedUserAdapter mBlockedUserAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blocked_users);



        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Blocked Users");
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        final RecyclerView blockedUsersRV = (RecyclerView) findViewById(R.id.list_blocked_users);

        mBlockedUserList = new ArrayList<>();

        mBlockedUserAdapter = new BlockedUserAdapter();
        mBlockedUserAdapter.setBlockedUserList(mBlockedUserList);
        mBlockedUserAdapter.setBlockedListener(blockListener);

        blockedUsersRV.setAdapter(mBlockedUserAdapter);
        blockedUsersRV.setLayoutManager(new LinearLayoutManager(this));

        new LSDKUser(this).getBlockedUsers(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject blocked = new JSONObject(response.body().string());
                    JSONArray blockedReal = blocked.getJSONArray("blockedReal");
                    ArrayList<BlockedUser> tempList = new ArrayList<>(blockedReal.length());
                    for (int i = 0; i < blockedReal.length(); i++) {
                        JSONObject blockedUserJson = blockedReal.getJSONObject(i);

                        String collegeName = null;
                        if (!blockedUserJson.getString("college").equals("null")) {
                            collegeName = blockedUserJson.getJSONObject("college").getString("name");
                        }
                        BlockedUser user = new BlockedUser(
                                blockedUserJson.getString("id"),
                                blockedUserJson.getString("firstName"),
                                blockedUserJson.getString("lastName"),
                                blockedUserJson.getString("profileImage"),
                                collegeName
                        );
                        user.isBlocked = true;
                        tempList.add(user);
                    }

                    mBlockedUserList.clear();
                    mBlockedUserList.addAll(tempList);

                    blockedUsersRV.post(new Runnable() {
                        @Override
                        public void run() {
                            mBlockedUserAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mSocket == null || !mSocket.connected()) && !mConnecting) {
            mConnecting = true;


            try {
                IO.Options op = new IO.Options();
                DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(this);
                op.query =
                        "token=" + mSharedPreferences.getString("userToken", "") +
                                "&deviceToken=" + device.getDeviceToken() +
                                "&udid=" + device.getUdid() +
                                "&version=" + device.getVersionName() +
                                "&build=" + device.getVersionCode() +
                                "&os=" + device.getOS() +
                                "&platform=" + device.getType() +
                                "&api=" + API_Methods.VERSION +
                                "&model=" + device.getModel();

                op.reconnectionDelay = 5;
                op.secure = true;
                op.transports = new String[]{WebSocket.NAME};

                mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/

                mSocket.connect();
                mConnecting = false;

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }

    private BlockedUserAdapter.OnBlockToggleListener blockListener = new BlockedUserAdapter.OnBlockToggleListener() {
        @Override
        public void onBlockToggle(BlockedUser user) {
            if (mSocket != null) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("block", !user.isBlocked);
                    params.put("user", user.userId);

                    mSocket.emit(API_Methods.VERSION + ":users:block:real", params);

                    user.isBlocked = !user.isBlocked;
                } catch (JSONException e) {
                    Utils.showServerErrorToast(getBaseContext());
                }
            } else {
                Utils.showServerErrorToast(getBaseContext());
            }
        }
    };


    protected static class BlockedUserAdapter extends RecyclerView.Adapter<BlockedUserVH> {

        private ArrayList<BlockedUser> mBlockedUsers;
        private OnBlockToggleListener mBlockedListener;

        public void setBlockedUserList(ArrayList<BlockedUser> mBlockedUsers) {
            this.mBlockedUsers = mBlockedUsers;
        }

        public void setBlockedListener(OnBlockToggleListener mBlockedListener) {
            this.mBlockedListener = mBlockedListener;
        }

        @Override
        public BlockedUserVH onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final BlockedUserVH blockedUserVH = new BlockedUserVH(inflater.inflate(R.layout.list_item_blocked_user, parent, false));
            blockedUserVH.vToggleBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = blockedUserVH.getAdapterPosition();
                    mBlockedListener.onBlockToggle(mBlockedUsers.get(pos));
                    notifyItemChanged(pos);
                }
            });
            return blockedUserVH;

        }

        @Override
        public void onBindViewHolder(BlockedUserVH holder, int position) {
            holder.bind(mBlockedUsers.get(position));

        }

        @Override
        public int getItemCount() {
            return mBlockedUsers.size();
        }

        interface OnBlockToggleListener {
            void onBlockToggle(BlockedUser user);
        }
    }

    protected static class BlockedUserVH extends RecyclerView.ViewHolder {

        public final ImageView vProfileImage;
        public final TextView vUserName;
        public final TextView vToggleBlock;
        public final TextView vCollegeName;


        public BlockedUserVH(View itemView) {
            super(itemView);
            vProfileImage = (ImageView) itemView.findViewById(R.id.image_profile);
            vUserName = (TextView) itemView.findViewById(R.id.text_name);
            vCollegeName = (TextView) itemView.findViewById(R.id.text_college_name);
            vToggleBlock = (TextView) itemView.findViewById(R.id.button_toggle_block);
        }

        public void bind(final BlockedUser user) {
            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .signature(Utils.getGlideSignature(itemView.getContext()))
                    .into(vProfileImage);

            vUserName.setText(user.getFullName());
            vCollegeName.setText(user.collegeName);
            vToggleBlock.setText(user.isBlocked ? "Unblock" : "Block");

        }
    }


    protected static class BlockedUser extends User {
        public boolean isBlocked = false;

        public BlockedUser(String userId, String firstName, String lastName, String userImage) {
            super(userId, firstName, lastName, userImage);
        }

        public BlockedUser(String userId, String firstName, String lastName, String userImage, String collegeName) {
            super(userId, firstName, lastName, userImage, collegeName);
        }

        //needs to be here because of "implements parcelable"
        public static final Parcelable.Creator<User> CREATOR = User.CREATOR;

    }

}
