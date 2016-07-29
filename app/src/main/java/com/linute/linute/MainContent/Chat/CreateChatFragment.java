package com.linute.linute.MainContent.Chat;

import android.util.Log;

import com.linute.linute.API.LSDKChat;
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


    @Override
    protected UserSelectAdapter createSearchAdapter() {
        return new UserGroupSearchAdapter(getContext(),mSearchRoomList, mSearchUserList);
    }

    @Override
    protected void search(String searchWord) {
        Log.i("AAA", "call");
        LSDKChat users = new LSDKChat(getActivity());
        Map<String, Object> newChat = new HashMap<>();
//        newChat.put("owner", mSharedPreferences.getString("userID", null));

//        newChat.put("owner", mSharedPreferences.getString("userID", null));


        if (!searchWord.equals("")) {
            newChat.put("fullName", searchWord);
        }

        JSONArray usersJson = new JSONArray();
        for(User user:mSelectedUsers){
            usersJson.put(user);
        }
        newChat.put("users", usersJson);

        users.getUsersAndRooms(newChat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null){
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
                    if (getActivity() != null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                } else {
//                    mSearchUserList.clear();
                    ArrayList<User> tempUsers = new ArrayList<>();
                    JSONObject jsonObject;
                    JSONArray friends;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.d(TAG, jsonObject.toString(4));
                        friends = jsonObject.getJSONArray("users");
                        JSONObject user;
                        for (int i = 0; i < friends.length(); i++) {
                            user = ((JSONObject) friends.get(i));
                            tempUsers.add(new User(
                                    user.getString("id"),
                                    user.getString("fullName"),
                                    user.getString("profileImage")
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

                        mSearchUserList.clear();
                        mSearchUserList.addAll(tempUsers);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSearchAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null){
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
