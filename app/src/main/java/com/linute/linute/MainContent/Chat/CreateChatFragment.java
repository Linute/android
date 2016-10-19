package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 7/28/16.
 */
public class CreateChatFragment extends SelectUsersFragment {

    public List<ChatRoom> mSearchRoomList = new ArrayList<>();

    private static final String TAG = CreateChatFragment.class.getSimpleName();

    UserGroupSearchAdapter2.OnRoomClickListener onRoomSelectedListener;


    public void setOnRoomSelectedListener(UserGroupSearchAdapter2.OnRoomClickListener onRoomSelectedListener) {
        this.onRoomSelectedListener = onRoomSelectedListener;
//        ((UserGroupSearchAdapter)mSearchAdapter).setOnRoomSelectedListener(onRoomSelectedListener);
    }

    @Override
    protected UserSelectAdapter2 createSearchAdapter() {
        UserGroupSearchAdapter2 userGroupSearchAdapter = new UserGroupSearchAdapter2(getContext());
        userGroupSearchAdapter.setUsers(mSearchUserList);
        userGroupSearchAdapter.setRooms(mSearchRoomList);
        userGroupSearchAdapter.setRoomClickListener(onRoomSelectedListener);
        userGroupSearchAdapter.setUserClickListener(this);
        return userGroupSearchAdapter;
    }

    public static CreateChatFragment newInstance(ArrayList<User> selectedUsers) {
        CreateChatFragment createChatFrag = new CreateChatFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(KEY_SELECTED_USERS, selectedUsers);
        createChatFrag.setArguments(arguments);
        return createChatFrag;
    }

    public static CreateChatFragment newInstance(ArrayList<User> lockedUsers, ArrayList<User> selectedUsers){
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(KEY_LOCKED_USERS, lockedUsers);
        arguments.putParcelableArrayList(KEY_SELECTED_USERS, selectedUsers);
        CreateChatFragment selectUserFragment = new CreateChatFragment();
        selectUserFragment.setArguments(arguments);
        return selectUserFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.search_users_entry).requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    protected void search(String searchWord) {
        LSDKChat users = new LSDKChat(getActivity());
        Map<String, Object> newChat = new HashMap<>();

        if (!searchWord.equals("")) {
            newChat.put("fullName", searchWord);
        }

        JSONArray usersJson = new JSONArray();
        for (User user : mSelectedUsers) {
            usersJson.put(user.userId);
        }

        newChat.put("users", usersJson);

        users.getUsersAndRooms(newChat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                } else {
//                    mSearchUserList.clear();
                    final ArrayList<User> tempUsers = new ArrayList<>();
                    JSONObject jsonObject;
                    JSONArray usersJson;
                    JSONArray roomsJson;
                    try {
                        jsonObject = new JSONObject(response.body().string());

//                        Log.d(TAG, jsonObject.toString(4));
                        usersJson = jsonObject.getJSONArray("users");
                        JSONObject user;
                        for (int i = 0; i < usersJson.length(); i++) {
                            user = ((JSONObject) usersJson.get(i));

                            String collegeName;
                            if (!user.isNull("college")) {
                                collegeName = user.getJSONObject("college").getString("name");

                            } else {
                                collegeName = "";
                            }

                            tempUsers.add(new User(
                                    user.getString("id"),
                                    user.getString("firstName"),
                                    user.getString("lastName"),
                                    user.getString("profileImage"),/*.getString("thumbnail"),*/
                                    collegeName
                            ));
                            /*if(findUser(mSelectedUsers, user.getString("id")) != -1) {
                                tempUsers.add(new User(
                                        user.getString("id"),
                                        user.getString("fullName"), user.getString("profileImage")
                                ));
                            }else if(findUser(mLockedUsers, user.getString("id")) != -1){
                                tempUsers.add(new User)
                            }*/

                        }


                        roomsJson = jsonObject.getJSONArray("rooms");

                        final ArrayList<ChatRoom> tempRoomList = new ArrayList<ChatRoom>();

                        for (int i = 0; i < roomsJson.length(); i++) {
                            JSONObject roomJson = roomsJson.getJSONObject(i);

                            JSONObject lastMessage = roomJson.getJSONArray("messages").getJSONObject(0);
                            boolean lastMessageRead = false;

                            JSONArray roomUsersJson = roomJson.getJSONArray("users");
                            ArrayList<User> usersList = new ArrayList<User>();
                            for (int u = 0; u < roomUsersJson.length(); u++) {
                                JSONObject userJSON = roomUsersJson.getJSONObject(u);
                                usersList.add(new User(
                                        userJSON.getString("id"),
                                        userJSON.getString("firstName"),
                                        userJSON.getString("lastName"),
                                        userJSON.getString("profileImage")
//          not set up on back end      userJSON.getJSONObject("college").getString("name")
                                ));
                            }

                            long unMuteAt = 0;
                            if (!roomJson.isNull("unMuteAt"))
                                unMuteAt = roomJson.getLong("unMuteAt");
                            tempRoomList.add(new ChatRoom(
                                    roomJson.getString("id"),
                                    1,
                                    roomJson.getString("name"),
                                    roomJson.getJSONObject("profileImage").getString("thumbnail"),
                                    usersList,
                                    lastMessage.getString("text"),
                                    lastMessageRead,
                                    0,
//                                    roomJson.getLong("time"),
                                    roomJson.getBoolean("isMuted"),
                                    unMuteAt
                            ));
                        }


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.removeCallbacksAndMessages(null);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSearchUserList.clear();
                                            mSearchUserList.addAll(tempUsers);
                                            mSearchRoomList.clear();
                                            mSearchRoomList.addAll(tempRoomList);
                                            mSearchAdapter2.notifyDataSetChanged();

                                        }
                                    });

                                    //mSearchRV.getRecycledViewPool().clear();

                                    vEmpty.setVisibility(tempRoomList.isEmpty() && tempUsers.isEmpty() ? View.VISIBLE : View.GONE);
                                    vProgress.setVisibility(View.GONE);


                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                }
            }
        });
    }


}
