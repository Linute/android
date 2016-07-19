package com.linute.linute.MainContent.Chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 7/8/16.
 */
public class ChatSettingsFragment extends BaseFragment{

    public static final String TAG = "ChatSettingsFragment";

    private static final String ARG_ROOM_ID = "roomId";

    private ChatRoom mChatRoom;

    private String mRoomId;
    private ArrayList<User> mParticipants = new ArrayList<>();
    private ChatParticipantsAdapter mParticipantsAdapter;

    public static ChatSettingsFragment newInstance(String roomId){
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

                    mChatRoom = ChatRoom.fromJSON(chat);

                    String chatString = chat.toString(4);
                    for(String s:chatString.split("\n")){
                        Log.i("AAA", s);
                    }

                    JSONObject room = chat.getJSONObject("room");
                    JSONArray users = room.getJSONArray("users");

                    mParticipants.clear();
                    for(int i=0;i<users.length();i++){
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

    public void display(){
        View view = getView();
        Log.i("AAA", mParticipants + " " + view);
        if(view == null) return;

        if(mParticipants != null) {
            RecyclerView participantsRV = (RecyclerView) view.findViewById(R.id.list_participants);
            participantsRV.setLayoutManager(new LinearLayoutManager(getContext()));
            mParticipantsAdapter = new ChatParticipantsAdapter(mParticipants);
            participantsRV.setAdapter(mParticipantsAdapter);
            mParticipantsAdapter.notifyDataSetChanged();
        }
    }





}
