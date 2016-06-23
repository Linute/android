package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class RoomsActivityFragment extends BaseFragment {
    public static final String TAG = RoomsActivityFragment.class.getSimpleName();

    private RoomsAdapter mRoomsAdapter;

    private List<Rooms> mRoomsList = new ArrayList<>(); //list of rooms
    private String mUserId;

    private View mEmptyText;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mSkip = 0;

    private boolean mCanLoadMore = false;

    private Handler mHandler = new Handler();

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        mRoomsAdapter.setLoadMore(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (mCanLoadMore)
                    getMoreRooms();
            }
        });
        mRoomsAdapter.setDeleteRoom(new RoomsAdapter.DeleteRoom() {
            @Override
            public void deleteRoom(int position, Rooms room) {
                RoomsActivityFragment.this.deleteRoom(position, room);
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
        //currently reloads screen whenever resumed. Maybe better way to do it in the future

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        getRooms();

        // when new message comes it, it's intercepted by MainActivity
        // use this eventbus so it is sent down to this screen
        mChatSubscription = NewMessageBus.getInstance().getObservable()
                .observeOn(Schedulers.io())
                .subscribe(mNewMessageSubscriber);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop listening
        if (mChatSubscription != null && !mChatSubscription.isUnsubscribed()) {
            mChatSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.emitSocket(API_Methods.VERSION + ":messages:unread", new JSONObject());
        }
    }

    private void getRooms() {
        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA) return;

        setFragmentState(FragmentState.LOADING_DATA);

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
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resString = response.body().string();

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

                        SimpleDateFormat format = Utils.getDateFormat();
                        final ArrayList<Rooms> tempRooms = new ArrayList<>();

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
                                    date = format.parse(message.getString("date"));
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

                        mRoomsAdapter.setLoadingMoreState(!mCanLoadMore ?
                                LoadMoreViewHolder.STATE_END : LoadMoreViewHolder.STATE_LOADING);

                        mSkip -= 20;

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mSwipeRefreshLayout.isRefreshing()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                    if (tempRooms.isEmpty()) {
                                        if (mEmptyText.getVisibility() == View.GONE)
                                            mEmptyText.setVisibility(View.VISIBLE);
                                    } else {
                                        if (mEmptyText.getVisibility() == View.VISIBLE)
                                            mEmptyText.setVisibility(View.GONE);
                                    }

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRoomsList.clear();
                                            mRoomsList.addAll(tempRooms);
                                            mRoomsAdapter.notifyDataSetChanged();
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
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }


    private boolean mLoadingMore = false;

    private void getMoreRooms() {

        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA || mLoadingMore)
            return;

        mLoadingMore = true;

        int limit = 20;

        int skip = mSkip;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        Map<String, String> params = new HashMap<>();
        params.put("skip", skip + "");
        params.put("limit", limit + "");

        new LSDKChat(getActivity()).getRooms(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mLoadingMore = false;
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

                //if (mSwipeRefreshLayout.isRefreshing()) return;

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

                        final ArrayList<Rooms> tempRooms = new ArrayList<>();
                        SimpleDateFormat format = Utils.getDateFormat();

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
                                    date = format.parse(message.getString("date"));
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

                        mCanLoadMore = skip1 > 0;
                        mRoomsAdapter.setLoadingMoreState(!mCanLoadMore ?
                                LoadMoreViewHolder.STATE_END : LoadMoreViewHolder.STATE_LOADING);


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSkip -= 20;
                                            int pos = mRoomsList.size();
                                            mRoomsList.addAll(tempRooms);
                                            mRoomsAdapter.notifyItemRangeInserted(pos, tempRooms.size());
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
                mLoadingMore = false;
            }
        });
    }

    private Subscription mChatSubscription;

    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            if (!mSwipeRefreshLayout.isRefreshing() && event.getRoomId() != null && getActivity() != null) {
                final Rooms tempRoom = new Rooms(event.getRoomId(), "", "", event.getMessage(), "", true, new Date().getTime());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                final int pos = mRoomsList.indexOf(tempRoom);
                                if (pos >= 0) {
                                    mRoomsList.get(pos).merge(tempRoom);
                                    mRoomsAdapter.notifyItemChanged(pos);
                                    mRoomsList.add(0, mRoomsList.remove(pos));
                                    mRoomsAdapter.notifyItemMoved(pos, 0);
                                } else {
                                    if (!mSwipeRefreshLayout.isRefreshing()) {
                                        mSwipeRefreshLayout.setRefreshing(true);
                                        getRooms();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
    };


    private void deleteRoom(final int position, final Rooms room){
        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            JSONObject object = new JSONObject();
            try {
                if (activity.socketConnected()) {
                    object.put("room", room.getRoomId());
                    activity.emitSocket(API_Methods.VERSION + ":rooms:delete", object);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //find the room that user wanted to delete

                                    //the room might have moved so we might have
                                    //      to find new position of room
                                    int newPos = position;

                                    //if room moved, try looking for it
                                    if (newPos < 0 || !mRoomsList.get(newPos).equals(room)) {
                                        newPos = mRoomsList.indexOf(room);
                                    }

                                    //if can't find appropriate room, return
                                    if (newPos == -1) return;


                                    //remove room from list and notify it was removed
                                    mRoomsList.remove(newPos);
                                    if (mRoomsList.isEmpty()){
                                        mRoomsAdapter.notifyDataSetChanged();
                                        mEmptyText.setVisibility(View.VISIBLE);
                                    }else {
                                        mRoomsAdapter.notifyItemRemoved(newPos);
                                        mRoomsAdapter.notifyItemRangeChanged(newPos, mRoomsList.size());
                                    }
                                }
                            });
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(activity);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
