package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 * <p/>
 * the rooms you have with people.
 * <p/>
 * i.e. chatroom with max, chat room with nabeel.
 * You can click to see your convo with max or convo with nabeel
 */
public class RoomsActivityFragment extends Fragment {

    //make sure this always gets called: getRoom

    public static final String TAG = RoomsActivityFragment.class.getSimpleName();

    private RoomsAdapter mRoomsAdapter;

    private List<Rooms> mRoomsList = new ArrayList<>(); //list of rooms
    private String mUserId;

    private View mEmptyText;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mSkip = 0;

    private boolean mCanLoadMore = false;

    private Handler mHandler = new Handler();

    private boolean mRoomsRetrieved;

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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_action_new_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create fragment and give it an argument specifying the article it should show
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (activity != null){
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

        mRoomsAdapter.setLoadMore(new Runnable() {
            @Override
            public void run() {
                if (mCanLoadMore) {
                    getMoreRooms();
                }
            }
        });

        mEmptyText = view.findViewById(R.id.rooms_empty_text);
        RecyclerView recList = (RecyclerView) view.findViewById(R.id.rooms_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mRoomsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getRooms();
        mChatSubscription = NewMessageBus.getInstance().getObservable()
                .observeOn(Schedulers.io())
                .subscribe(mNewMessageSubscriber);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mChatSubscription != null && !mChatSubscription.isUnsubscribed()) {
            mChatSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseTaptActivity activity = (BaseTaptActivity)getActivity();
        if (activity!=null){
            activity.emitSocket(API_Methods.VERSION + ":messages:unread", new JSONObject());
        }
    }

    private void getRooms() {
        if (getActivity() == null || mSwipeRefreshLayout.isRefreshing()) return;

        mRoomsRetrieved = false;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("limit", "20");

        new LSDKChat(getActivity()).getRooms(params, new Callback() {
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

                if (response.isSuccessful()) {
                    try {

                        JSONObject jsonObj = new JSONObject(resString);

                        mSkip = jsonObj.getInt("skip");

                        JSONArray rooms = jsonObj.getJSONArray("rooms");

                        //ArrayList<ChatHead> chatHeads = new ArrayList<>();
                        String lastMessage = "";
                        boolean hasUnreadMessage;

                        JSONObject room;
                        JSONObject user;
                        JSONArray messages;
                        JSONArray tempArray;

                        JSONObject message;
                        JSONArray readArray;

                        Date date;

                        ArrayList<Rooms> tempRooms = new ArrayList<>();

                        for (int i = rooms.length() - 1; i >= 0; i--) {
                            hasUnreadMessage = true;

                            room = rooms.getJSONObject(i);
                            user = room.getJSONArray("users").getJSONObject(0);

                            messages = room.getJSONArray("messages"); //list of messages in room


                            //if messages not empty or null
                            if (messages.length() > 0 && !messages.isNull(0)) {
                                message = messages.getJSONObject(messages.length() - 1);
                                readArray = message.getJSONArray("read");

                                try {
                                    date = Utils.DATE_FORMAT.parse(message.getString("date"));
                                } catch (ParseException e) {
                                    date = null;
                                }

                                //check last message. check if we already read it
                                for (int k = 0; k < readArray.length(); k++) {
                                    if (readArray.getString(k).equals(mUserId)) {
                                        hasUnreadMessage = false;
                                        break;
                                    }
                                }

                                boolean isOwner = message.getJSONObject("owner").getString("id").equals(mUserId);
                                //if you sent last message : show  "You: <text>"

                                tempArray = message.getJSONArray("videos");

                                if (tempArray.length() > 0) {
                                    lastMessage = isOwner ? "You: sent a video" : "sent you a video";
                                } else {
                                    tempArray = message.getJSONArray("images");
                                    if (tempArray.length() > 0) {
                                        lastMessage = isOwner ? "You: sent an image" : "sent you an image";
                                    } else {
                                        lastMessage = isOwner ? "You: " + message.getString("text") :
                                                message.getString("text");
                                    }
                                }

                            } else { //no messages show "..."
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


                        mCanLoadMore = mSkip > 0;

                        mSkip -= 20;

                        mRoomsList.clear();
                        mRoomsList.addAll(tempRooms);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mSwipeRefreshLayout.isRefreshing()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                    if (mRoomsList.isEmpty()) {
                                        if (mEmptyText.getVisibility() == View.GONE)
                                            mEmptyText.setVisibility(View.VISIBLE);
                                    } else {
                                        if (mEmptyText.getVisibility() == View.VISIBLE)
                                            mEmptyText.setVisibility(View.GONE);
                                    }

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRoomsAdapter.notifyDataSetChanged();
                                            mRoomsRetrieved = true;
                                        }
                                    });
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
                                    mSwipeRefreshLayout.setRefreshing(false);
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
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }


    private void getMoreRooms() {

        if (getActivity() == null || mSwipeRefreshLayout.isRefreshing()) return;

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        int limit = 20;

        if (mSkip < 0) {
            limit += mSkip;
            mSkip = 0;
        }

        Map<String, String> params = new HashMap<>();
        params.put("skip", mSkip + "");
        params.put("limit", limit + "");

        new LSDKChat(getActivity()).getRooms(params, new Callback() {
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

                        for (int i = rooms.length() - 1; i >= 0; i--) {
                            hasUnreadMessage = true;

                            room = rooms.getJSONObject(i);
                            user = room.getJSONArray("users").getJSONObject(0);

                            messages = room.getJSONArray("messages"); //list of messages in room


                            //if messages not empty or null
                            if (messages.length() > 0 && !messages.isNull(0)) {
                                message = messages.getJSONObject(messages.length() - 1);
                                readArray = message.getJSONArray("read");

                                try {
                                    date = Utils.DATE_FORMAT.parse(message.getString("date"));
                                } catch (ParseException e) {
                                    date = null;
                                }

                                //check last message. check if we already read it
                                for (int k = 0; k < readArray.length(); k++) {
                                    if (readArray.getString(k).equals(mUserId)) {
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
                            } else { //no messages show "..."
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
                        mCanLoadMore = mSkip > 0;
                        mSkip -= 20;

                        mRoomsList.clear();
                        mRoomsList.addAll(tempRooms);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRoomsAdapter.notifyDataSetChanged();
                                        }
                                    });

                                    mSwipeRefreshLayout.setRefreshing(false);
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
                                    mSwipeRefreshLayout.setRefreshing(false);
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
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }

    private Subscription mChatSubscription;

    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            if (mRoomsRetrieved && event.getRoomId() != null && getActivity() != null ) {

                Rooms tempRoom = new Rooms(event.getRoomId(), "", "", event.getMessage(), "", true, new Date().getTime());
                final int pos = mRoomsList.indexOf(tempRoom);

                if (pos >= 0) {
                    mRoomsList.get(pos).merge(tempRoom);
                    mRoomsList.add(0, mRoomsList.remove(pos));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRoomsAdapter.notifyItemChanged(pos);
                                }
                            });
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRoomsAdapter.notifyItemMoved(pos, 0);
                                }
                            });
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getRooms();
                        }
                    });
                }
            }
        }
    };
}
