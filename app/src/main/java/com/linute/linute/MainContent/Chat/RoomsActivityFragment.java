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
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class RoomsActivityFragment extends Fragment {

    private static final String TAG = RoomsActivityFragment.class.getSimpleName();

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private RoomsAdapter mRoomsAdapter;

    private List<Rooms> mRoomsList = new ArrayList<>();
    private SharedPreferences mSharedPreferences;

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
        getRooms();
        if (getActivity().getIntent().getStringExtra("ROOM") != null) {
            // start chat fragment
            // use same procedure unless found better
        }
        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.rooms_list);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        recList.setAdapter(mRoomsAdapter);

    }

    private void getRooms() {
        final LSDKChat chat = new LSDKChat(getActivity());
        chat.getRooms(null, new Callback() {
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
                    mRoomsList.clear();
                    JSONObject jsonObject = null;
                    JSONArray rooms = null;
                    JSONObject room = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.d(TAG, "onResponse: " + jsonObject.toString(4));
                        rooms = jsonObject.getJSONArray("rooms");
                        ArrayList<ChatHead> chatHeads = new ArrayList<ChatHead>();
                        for (int i = 0; i < rooms.length(); i++) {
                            room = (JSONObject) rooms.get(i);
                            for (int j = 0; j < room.getJSONArray("users").length(); j++) {
                                chatHeads.add(new ChatHead(
                                        ((JSONObject) room.getJSONArray("users").get(j)).getString("fullName"),
                                        ((JSONObject) room.getJSONArray("users").get(j)).getString("profileImage")));
                            }
                            mRoomsList.add(new Rooms(
                                    room.getString("owner"),
                                    room.getString("id"),
                                    ((JSONObject) room.getJSONArray("messages").get(0)).getJSONObject("owner").getString("id"),
                                    ((JSONObject) room.getJSONArray("messages").get(0)).getJSONObject("owner").getString("fullName"),
                                    ((JSONObject) room.getJSONArray("messages").get(0)).getString("text"),
                                    ((JSONObject) room.getJSONArray("messages").get(0)).getJSONObject("owner").getString("profileImage"),
                                    room.getJSONArray("users").length() + 1 /* + 1 is for yourself*/,
                                    chatHeads));
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRoomsAdapter.notifyDataSetChanged();
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
}
