package com.linute.linute.MainContent.Chat;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ROOM_ID = "room";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ChatFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private ChatAdapter mChatAdapter;

    private List<Chat> mChatList = new ArrayList<>();
    private SharedPreferences mSharedPreferences;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChatAdapter = new ChatAdapter(context, mChatList);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ROOM_ID);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        getChat();
//        ((RoomsActivity) getActivity()).toggleFab(false);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.chat_list);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        recList.setAdapter(mChatAdapter);
    }

    private void getChat() {
        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mParam1);
        LSDKChat getChat = new LSDKChat(getActivity());
        getChat.getChat(chat, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + request.body());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                } else {
//                    Log.d(TAG, "onResponseSuccessful: " + response.body().string());
                    mChatList.clear();
                    JSONObject jsonObject = null;
                    JSONArray messages = null;
                    JSONObject message = null;

                    try {
                        jsonObject = new JSONObject(response.body().string());
                        messages = jsonObject.getJSONArray("messages");
                        for (int i = 0; i < messages.length(); i++) {
                            message = (JSONObject) messages.get(i);
                            mChatList.add(new Chat(
                                    message.getString("room"),
                                    message.getJSONObject("owner").getString("profileImage"),
                                    message.getJSONObject("owner").getString("fullName"),
                                    Utils.formatDateToReadableString(message.getString("date")),
                                    message.getJSONObject("owner").getString("id"),
                                    message.getString("id"),
                                    message.getString("text")));
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatAdapter.notifyDataSetChanged();
                                    scrollToBottom();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void scrollToBottom() {
        recList.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

}
