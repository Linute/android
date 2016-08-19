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

    UserGroupSearchAdapter.OnRoomSelectedListener onRoomSelectedListener;

    public void setOnRoomSelectedListener(UserGroupSearchAdapter.OnRoomSelectedListener onRoomSelectedListener) {
        this.onRoomSelectedListener = onRoomSelectedListener;
//        ((UserGroupSearchAdapter)mSearchAdapter).setOnRoomSelectedListener(onRoomSelectedListener);
    }

    @Override
    protected UserSelectAdapter createSearchAdapter() {
        UserGroupSearchAdapter userGroupSearchAdapter = new UserGroupSearchAdapter(getContext(), mSearchRoomList, mSearchUserList);
        userGroupSearchAdapter.setOnRoomSelectedListener(onRoomSelectedListener);
        return userGroupSearchAdapter;
    }

    public static CreateChatFragment newInstance(ArrayList<User> selectedUsers) {
        CreateChatFragment createChatFrag = new CreateChatFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(KEY_SELECTED_USERS, selectedUsers);
        createChatFrag.setArguments(arguments);
        return createChatFrag;
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
                    ArrayList<User> tempUsers = new ArrayList<>();
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

                        mSearchUserList.clear();
                        mSearchUserList.addAll(tempUsers);
                        /*
                        *     "rooms": [
       {
           "owner": {
               "userName": "maxseleznov",
               "isOnline": false,
               "profileImage": "56a7e16e37e5adf32b97cc3d.jpg",
               "college": "564a46ff8ac4a559174247b5",
               "fullName": "Max Seleznov",
               "lastName": "Seleznov",
               "firstName": "Max",
               "id": "56a7e16e37e5adf32b97cc3d"
           },
           "date": "2016-07-29T18:24:16.120Z",
           "deleted": [],
           "online": [],
           "users": [
               {
                   "userName": "mikhailfoenko",
                   "isOnline": true,
                   "profileImage": "profile.jpg",
                   "college": "564a46ff8ac4a559174247b5",
                   "fullName": "Mikhail Foenko",
                   "lastName": "Foenko",
                   "firstName": "Mikhail",
                   "id": "5755c71a84c92d0d04a5a296"
               },                                      [=l
               {
                   "userName": "andimuskaj",
                   "isOnline": false,
                   "profileImage": "56d4fc054ba332837d686595.jpg",
                   "college": "564a46ff8ac4a559174247b5",
                   "fullName": "Andi Muskaj",
                   "lastName": "Muskaj",
                   "firstName": "Andi",
                   "id": "56d4fc054ba332837d686595"
               },
               {
                   "userName": "maxseleznov",
                   "isOnline": false,
                   "profileImage": "56a7e16e37e5adf32b97cc3d.jpg",
                   "college": "564a46ff8ac4a559174247b5",
                   "fullName": "Max Seleznov",
                   "lastName": "Seleznov",
                   "firstName": "Max",
                   "id": "56a7e16e37e5adf32b97cc3d"
               }
           ],
           "type": 1,
           "image": "group.png",
           "name": "",
           "id": "579b9f55eaf4532a030041aa",
           "profileImage": {
               "original": "http:\/\/images.linute.com\/rooms\/original\/group.png",
               "thumbnail": "http:\/\/images.linute.com\/rooms\/thumbnail\/group.png"
           },
           "messages": [
               {
                   "room": "579b9f55eaf4532a030041aa",
                   "owner": {
                       "userName": "maxseleznov",
                       "isOnline": false,
                       "profileImage": "56a7e16e37e5adf32b97cc3d.jpg",
                       "college": "564a46ff8ac4a559174247b5",
                       "fullName": "Max Seleznov",
                       "lastName": "Seleznov",
                       "firstName": "Max",
                       "id": "56a7e16e37e5adf32b97cc3d"
                   },
                   "text": "Gbcc",
                   "isDeleted": false,
                   "date": "2016-07-29T18:24:16.106Z",
                   "deleted": [],
                   "delivered": [
                       "56a7e16e37e5adf32b97cc3d"
                   ],
                   "read": [
                       "56a7e16e37e5adf32b97cc3d",
                       "5755c71a84c92d0d04a5a296"
                   ],
                   "videos": [],
                   "images": [],
                   "type": 0,
                   "id": "579b9f55eaf4532a030041ab"
               }
           ],
           "unMuteAt": null,
           "isMuted": false
       }
   ],
                        * */


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

                                    mSearchRoomList.clear();
                                    mSearchRoomList.addAll(tempRoomList);

                                    Log.i("AAA", "test");
                                    Log.i("AAA", "" + mSearchAdapter.getItemCount());
                                    mSearchRV.getRecycledViewPool().clear();
                                    mSearchAdapter.notifyDataSetChanged();
                                    View view = getView();
                                    if (view != null)
                                        view.findViewById(R.id.empty_view).setVisibility(View.GONE);


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
