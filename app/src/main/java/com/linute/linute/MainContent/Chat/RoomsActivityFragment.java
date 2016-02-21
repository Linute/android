package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class RoomsActivityFragment extends Fragment {

    //make sure this always gets called: getRoom

    private static final String TAG = RoomsActivityFragment.class.getSimpleName();

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private RoomsAdapter mRoomsAdapter;

    private List<Rooms> mRoomsList = new ArrayList<>(); //list of rooms
    private SharedPreferences mSharedPreferences;

    private View mEmptyText;

    public RoomsActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mRoomsAdapter = new RoomsAdapter(context, mRoomsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (getActivity().getIntent().getStringExtra("ROOMS") != null) { //make sure not null

            // start chat fragment
            // use same procedure unless found better
            ArrayList<ChatHead> chatHeads = getActivity().getIntent().getParcelableArrayListExtra("chatHeads");
            ChatFragment newFragment = ChatFragment.newInstance(
                    getActivity().getIntent().getStringExtra("roomId"),
                    getActivity().getIntent().getStringExtra("ownerName"),
                    getActivity().getIntent().getStringExtra("ownerId"),
                    Integer.parseInt(getActivity().getIntent().getStringExtra("roomCnt")), //2
                    chatHeads);
            Log.d(TAG, "onClick: " + newFragment.getArguments().getString("username"));
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.chat_container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        }


        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyText = view.findViewById(R.id.rooms_empty_text);
        recList = (RecyclerView) view.findViewById(R.id.rooms_list);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        recList.setAdapter(mRoomsAdapter);


    }

    @Override
    public void onResume() {
        super.onResume();
        getRooms();
    }

    private void getRooms() {
        if (getActivity() == null) return;
        final String userID = mSharedPreferences.getString("userID", "");

        final LSDKChat chat = new LSDKChat(getActivity());
        chat.getRooms(null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
                String resString = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObj = new JSONObject(resString);
                        JSONArray rooms = jsonObj.getJSONArray("rooms");

                        ArrayList<ChatHead> chatHeads = new ArrayList<>();
                        String lastMessage = "";

                        JSONObject room;
                        JSONArray users;
                        JSONArray messages;

//                        mRoomsList.clear();
                        ArrayList<Rooms> tempRooms = new ArrayList<>();

                        for (int i = 0; i < rooms.length(); i++) {
                            room = rooms.getJSONObject(i);
                            users = room.getJSONArray("users");

                            for (int j = 0; j < users.length(); j++) {
                                chatHeads.add(new ChatHead(
                                                users.getJSONObject(j).getString("fullName"),
                                                users.getJSONObject(j).getString("profileImage"),
                                                users.getJSONObject(j).getString("id")
                                        )
                                );
                            }

                            messages = room.getJSONArray("messages");
                            if (messages.length() > 0 && !messages.isNull(0)) {
                                if (messages.getJSONObject(0).getJSONObject("owner").getString("id").equals(userID)) {
                                    lastMessage = "You: " + messages.getJSONObject(0).getString("text");
                                } else {
                                    lastMessage = messages.getJSONObject(0).getString("text");
                                }
                            }else {
                                lastMessage = "...";
                            }

                            Log.i("ROOM_TEST", "onResponse: "+room.toString());

                            //Throws error but still runs correctly... weird
                            tempRooms.add(new Rooms(getStringFromObj(room, "owner"),
                                    getStringFromObj(room, "id"),
                                    getStringFromObj(users.getJSONObject(0), "id"),
                                    getStringFromObj(users.getJSONObject(0), "fullName"),
                                    lastMessage,
                                    getStringFromObj(users.getJSONObject(0), "profileImage"),
                                    users.length() + 1,  // add yourself
                                    chatHeads
                            ));
                        }

                        mRoomsList.clear();
                        mRoomsList.addAll(tempRooms);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mRoomsList.isEmpty()){
                                        if (mEmptyText.getVisibility() == View.GONE)
                                            mEmptyText.setVisibility(View.VISIBLE);
                                    }else {
                                        if (mEmptyText.getVisibility() == View.VISIBLE)
                                            mEmptyText.setVisibility(View.GONE);
                                    }

                                    mRoomsAdapter.notifyDataSetChanged();
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
                } else {
                    Log.e(TAG, "onResponse: " + resString);
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
        });
    }


    public static String getStringFromObj(JSONObject oobj, String key) {
        try {
            return oobj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "getStringFromObj: " + key);
            return "";
        }
    }
}
