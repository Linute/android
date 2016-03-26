package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 *
 * the rooms you have with people.
 *
 * i.e. chatroom with max, chat room with nabeel.
 * You can click to see your convo with max or convo with nabeel
 *
 */
public class RoomsActivityFragment extends Fragment {

    //make sure this always gets called: getRoom

    private static final String TAG = RoomsActivityFragment.class.getSimpleName();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private RoomsAdapter mRoomsAdapter;

    private List<Rooms> mRoomsList = new ArrayList<>(); //list of rooms
    private String mUserId;

    private FloatingActionButton mFab;

    private View mEmptyText;

    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mUserId = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", "");

        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.rooms_toolbar);
        toolbar.setTitle("Inbox");
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.ic_action_new_message);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create fragment and give it an argument specifying the article it should show
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (activity != null) {
                    activity.addFragmentToContainer(new SearchUsers());
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_rooms);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRooms();
            }
        });

        mEmptyText = view.findViewById(R.id.rooms_empty_text);
        recList = (RecyclerView) view.findViewById(R.id.rooms_list);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        recList.setAdapter(mRoomsAdapter);
    }

    public void hideFab(boolean hide) {
        if (hide && mFab.isShown()) {
            mFab.hide();
        } else if (!hide && !mFab.isShown()) {
            mFab.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getRooms();

        RoomsActivity activity = (RoomsActivity) getActivity();

        if (activity != null){

            //hideFab(false);
            activity.setTitle("Inbox");

            JSONObject obj = new JSONObject();

            //send tracking info
            try {
                obj.put("owner", mUserId);
                obj.put("action", "active");
                obj.put("screen", "Inbox");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RoomsActivity activity = (RoomsActivity) getActivity();

        if (activity != null){
            //hideFab(true);

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mUserId);
                obj.put("action", "inactive");
                obj.put("screen", "Inbox");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getRooms() {
        if (getActivity() == null) return;

        if (!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        new LSDKChat(getActivity()).getRooms(null, new Callback() {
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

                //Log.i(TAG, "onResponse: "+resString);

                //todo add unread messages icon

                if (response.isSuccessful()) {
                    try {

                        JSONObject jsonObj = new JSONObject(resString);
                        JSONArray rooms = jsonObj.getJSONArray("rooms");

                        //ArrayList<ChatHead> chatHeads = new ArrayList<>();
                        String lastMessage = "";
                        boolean hasUnreadMessage;

                        JSONObject room;
                        JSONObject user;
                        JSONArray messages;

                        JSONObject message;
                        JSONArray readArray;

                        Date date;

                        ArrayList<Rooms> tempRooms = new ArrayList<>();

                        for (int i = 0; i < rooms.length(); i++) {
                            hasUnreadMessage = true;

                            room = rooms.getJSONObject(i);
                            user = room.getJSONArray("users").getJSONObject(0);

                            messages = room.getJSONArray("messages"); //list of messages in room


                            //if messages not empty or null
                            if (messages.length() > 0 && !messages.isNull(0)) {
                                message = messages.getJSONObject(messages.length() - 1);
                                readArray = message.getJSONArray("read");

                                try {
                                    date = simpleDateFormat.parse(message.getString("date"));
                                }catch (ParseException e){
                                    date = null;
                                }

                                //check last message. check if we already read it
                                for (int k = 0; k < readArray.length() ;  k++){
                                    if (readArray.getString(k).equals(mUserId)){
                                        hasUnreadMessage = false;
                                        break;
                                    }
                                }

                                //if you sent last message : show  "You: <text>"
                                if (message.getJSONObject("owner").getString("id").equals(mUserId)) {
                                    lastMessage = "You: " + messages.getJSONObject(0).getString("text");
                                }

                                //the other person sent last message : show <message>
                                else {
                                    lastMessage = messages.getJSONObject(0).getString("text");
                                }
                            }

                            else { //no messages show "..."
                                lastMessage = "...";
                                hasUnreadMessage = false;
                                date = null;
                            }


                            //Throws error but still runs correctly... weird
                            tempRooms.add(new Rooms(
                                    room.getString("id"),
                                    user.getString("id"),
                                    user.getString("fullName"),
                                    lastMessage,
                                    user.getString("profileImage"),
                                    hasUnreadMessage,
                                    date == null ? 0 : date.getTime()
                            ));
                           // ,
//                                    users.length() + 1,  // add yourself
//                                    chatHeads
                        }

                        mRoomsList.clear();
                        mRoomsList.addAll(tempRooms);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mSwipeRefreshLayout.isRefreshing()){
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
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
}
