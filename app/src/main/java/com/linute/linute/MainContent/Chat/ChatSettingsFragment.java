package com.linute.linute.MainContent.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 7/8/16.
 */
public class ChatSettingsFragment extends BaseFragment {

    public static final String TAG = "ChatSettingsFragment";

    private static final String ARG_ROOM_ID = "roomId";
    public static final int MENU_USER_DELETE = 0;
    public static final String KEY_USER = "KEY_USER";

    private ChatRoom mChatRoom;

    private String mRoomId;
    private ArrayList<User> mParticipants = new ArrayList<>();
    private ChatParticipantsAdapter mParticipantsAdapter;

    public static ChatSettingsFragment newInstance(String roomId) {
        ChatSettingsFragment fragment = new ChatSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ARG_ROOM_ID);
        }
        Map<String, String> params = new HashMap<>();
        params.put("room", mRoomId);
        new LSDKChat(getContext()).getChat(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject chat = new JSONObject(response.body().string());

                    mChatRoom = ChatRoom.fromJSON(chat.getJSONObject("room"));

                    JSONObject room = chat.getJSONObject("room");
                    JSONArray users = room.getJSONArray("users");

                    mParticipants.clear();
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        mParticipants.add(new User(
                                user.getString("id"),
                                user.getString("fullName"),
                                user.getString("profileImage")
                        ));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            display();
                        }
                    });
                }
            }
        });
        display();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_settings, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        display();
    }

   /* @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }*/

    public void display() {
        View view = getView();
        if (view == null) return;

        if (mParticipants != null) {
            RecyclerView participantsRV = (RecyclerView) view.findViewById(R.id.list_participants);
            participantsRV.setLayoutManager(new LinearLayoutManager(getContext()));
            mParticipantsAdapter = new ChatParticipantsAdapter(mParticipants);
            participantsRV.setAdapter(mParticipantsAdapter);
            mParticipantsAdapter.notifyDataSetChanged();

            mParticipantsAdapter.setUserClickListener(new ChatParticipantsAdapter.OnUserClickListener() {
                @Override
                public void OnUserClick(User user) {
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    TaptUserProfileFragment fragment = TaptUserProfileFragment.newInstance(user.userName, user.userId);
                    activity.addFragmentToContainer(fragment);
                }

                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, final User user, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    contextMenu.setHeaderTitle(user.userName);
                    MenuItem item = contextMenu.add(0, MENU_USER_DELETE, 0, "Delete");
                    Intent i = new Intent();
                    i.putExtra(KEY_USER, user);
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Toast.makeText(getContext(), "Remove", Toast.LENGTH_SHORT).show();
                            try {
                                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                JSONObject paramsJSON = new JSONObject();
                                JSONArray usersJSON = new JSONArray();
                                usersJSON.put(user.userId);
                                paramsJSON.put("room", mRoomId);
                                paramsJSON.put("users", usersJSON);


                                activity.emitSocket(API_Methods.VERSION + ":rooms:delete users", paramsJSON);
                                //TODO add users

                                Log.i("AAA", "delete user \n" + paramsJSON.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;

                        }
                    });
                    item.setIntent(i);
                }
            });


            mParticipantsAdapter.setAddPeopleListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    SelectUserFragment selectUserFragment = SelectUserFragment.newInstance(mParticipants);
                    selectUserFragment.setOnUsersSelectedListener(new SelectUserFragment.OnUsersSelectedListener() {
                        @Override
                        public void onUsersSelected(ArrayList<User> users) {
                            try {
                                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                JSONObject paramsJSON = new JSONObject();
                                JSONArray usersJSON = new JSONArray();
                                for (User user : users) {
                                    usersJSON.put(user.userId);
                                }
                                paramsJSON.put("room", mRoomId);
                                paramsJSON.put("users", usersJSON);

                                activity.emitSocket(API_Methods.VERSION + ":rooms:add users", paramsJSON);
                                //TODO add users

                                Log.i("AAA", "add user \n" + paramsJSON.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    activity.replaceContainerWithFragment(selectUserFragment);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        activity.connectSocket("add users", onAddUsers);
        activity.connectSocket("delete users", onDeleteUsers);


    }

    @Override
    public void onPause() {
        super.onPause();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        activity.disconnectSocket("add users", onAddUsers);
        activity.disconnectSocket("delete users", onDeleteUsers);
    }

    private Emitter.Listener onAddUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject event = new JSONObject(args[0].toString());
                            JSONArray users = event.getJSONArray("users");
                            final int oldSize = mParticipants.size();
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                mParticipants.add(new User(
                                        user.getString("id"),
                                        user.getString("fullName"),
                                        user.getString("profileImage")
                                ));
                            }

//                            mParticipantsAdapter.notifyItemRangeInserted(oldSize, mParticipants.size() - 1);
mParticipantsAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private Emitter.Listener onDeleteUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject event = new JSONObject(args[0].toString());
                            JSONArray users = event.getJSONArray("users");

                            final int[] removedPositions = new int[users.length()];
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(0);
                                for (int j = 0; j < mParticipants.size(); j++) {
                                    if (user.getString("id").equals(mParticipants.get(j).userId)) {
                                        removedPositions[i] = j;
                                        mParticipants.remove(j);
                                        break;
                                    }
                                }
                            }

                            for (int i : removedPositions) {
                                mParticipantsAdapter.notifyItemRemoved(i+1);
                            }
//                            mParticipantsAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        }
    };

    //:room:add users
    //:room:delete users

    //room:
    //users: [;    ]


}
