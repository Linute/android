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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.MainActivity;
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
public class RoomsActivityFragment extends BaseFragment implements RoomsAdapter.RoomContextMenuCreator {
    public static final String TAG = RoomsActivityFragment.class.getSimpleName();

    private RoomsAdapter mRoomsAdapter;

    private List<ChatRoom> mRoomsList = new ArrayList<>(); //list of rooms
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
        toolbar.setTitle("Messenger");
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
                    CreateChatFragment selectUserFragment = new CreateChatFragment();
                    //callback for when the user finishes selecting users
                    selectUserFragment.setOnUsersSelectedListener(new SelectUsersFragment.OnUsersSelectedListener() {
                        @Override
                        public void onUsersSelected(ArrayList<User> users) {
                            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                            activity.replaceContainerWithFragment(ChatFragment.newInstance(null, users));

                        }
                    });
                    selectUserFragment.setOnRoomSelectedListener(new UserGroupSearchAdapter.OnRoomSelectedListener() {
                        @Override
                        public void onRoomSelected(ChatRoom room) {
                            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                            activity.replaceContainerWithFragment(ChatFragment.newInstance(room));
                        }
                    });
                    activity.addFragmentToContainer(selectUserFragment);

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
        mRoomsAdapter.setContextMenuCreator(this);

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
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) activity.setShowSnackbar(false);

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
    public void onStop() {
        super.onStop();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) activity.setShowSnackbar(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            Log.i(TAG, "onDestroy: emit");
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
//                        Log.d(TAG, jsonObj.toString(4));


                        mSkip = jsonObj.getInt("skip");

                        JSONArray rooms = jsonObj.getJSONArray("rooms");

                        //ArrayList<ChatHead> chatHeads = new ArrayList<>();
                        String lastMessage = "";
                        boolean hasUnreadMessage;

                        JSONObject room;
                        JSONArray usersJson;
                        JSONArray messages;
                        JSONArray tempArray;

                        JSONObject message;
                        JSONArray readArray;

                        Date date;

                        String name;
                        String image;

                        int type;

                        boolean isMuted;

                        SimpleDateFormat format = Utils.getDateFormat();
                        final ArrayList<ChatRoom> tempRooms = new ArrayList<>();


                        for (int i = rooms.length() - 1; i >= 0; i--) {
                            hasUnreadMessage = true;

                            room = rooms.getJSONObject(i);


                            name = room.getString("name");
                            image = room.getJSONObject("profileImage").getString("thumbnail");


                            type = room.getInt("type");

                            isMuted = room.getBoolean("isMuted");

                            long mutedUntil = 0;
                            Object unMuteAt = room.get("unMuteAt");

                            if (unMuteAt != null) {
                                try {
                                    mutedUntil = Long.getLong(unMuteAt.toString());
                                } catch (NumberFormatException | NullPointerException e) {
                                }
                            }


                            usersJson = room.getJSONArray("users");
                            ArrayList<User> usersList = new ArrayList<User>();
                            for (int u = 0; u < usersJson.length(); u++) {
                                JSONObject userJson = usersJson.getJSONObject(u);
                                usersList.add(new User(
                                        userJson.getString("id"),
                                        userJson.getString("firstName"),
                                        userJson.getString("lastName"),
                                        userJson.getString("profileImage")
                                ));
                            }

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

                                JSONObject owner = message.getJSONObject("owner");
                                boolean isOwner = owner.getString("id").equals(mUserId);
                                boolean showName = usersList.size() > 1 && message.getInt("type") == 0;

                                //if you sent last message : show  "You: <text>"

                                tempArray = message.getJSONArray("videos");

                                if (tempArray.length() > 0) {
                                    lastMessage = "sent you a video";
                                } else {
                                    tempArray = message.getJSONArray("images");
                                    if (tempArray.length() > 0) {
                                        lastMessage = "sent you an image";
                                    } else {
                                        lastMessage = message.getString("text");
                                    }
                                }

                                if (isOwner) {
                                    lastMessage = "You: " + lastMessage;
                                } else if (showName) {
                                    lastMessage = owner.getString("firstName") + ": " + lastMessage;
                                }

                            } else { //no messages show "..."
                                lastMessage = "...";
                                hasUnreadMessage = false;
                                date = null;
                            }


                            //Throws error but still runs correctly... weird
                            tempRooms.add(new ChatRoom(
                                    room.getString("id"),
                                    type,
                                    name,
                                    image,
                                    usersList,
                                    lastMessage,
                                    hasUnreadMessage,
                                    date == null ? 0 : date.getTime(),
                                    isMuted,
                                    mutedUntil
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
                        //Log.i(TAG, "onResponse: "+jsonObj.toString(4));

                        JSONArray rooms = jsonObj.getJSONArray("rooms");

                        //ArrayList<ChatHead> chatHeads = new ArrayList<>();
                        String lastMessage = "";
                        boolean hasUnreadMessage;

                        JSONObject room;
                        JSONArray users;
                        JSONArray messages;

                        JSONObject message;
                        JSONArray readArray;

                        Date date;

                        final ArrayList<ChatRoom> tempRooms = new ArrayList<>();
                        SimpleDateFormat format = Utils.getDateFormat();

                        for (int i = rooms.length() - 1; i >= 0; i--) {
                            hasUnreadMessage = true;

                            room = rooms.getJSONObject(i);
                            users = room.getJSONArray("users");

                            ArrayList<User> usersList = new ArrayList<User>();
                            for (int u = 0; u < users.length(); u++) {
                                JSONObject user = users.getJSONObject(u);
                                usersList.add(new User(
                                        user.getString("id"),
                                        user.getString("firstName"),
                                        user.getString("lastName"),
                                        user.getString("profileImage")
                                ));
                            }

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
                            tempRooms.add(new ChatRoom(
                                    room.getString("id"),
                                    usersList,
                                    lastMessage,
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
                final ChatRoom tempRoom = new ChatRoom(event.getRoomId(), 0, "", "", null, event.getMessage(), true, new Date().getTime(), false, 0);
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


    private void deleteRoom(final int position, final ChatRoom room) {
        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            JSONObject object = new JSONObject();
            try {
                if (activity.socketConnected()) {
                    object.put("room", room.roomId);
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
                                    if (mRoomsList.isEmpty()) {
                                        mRoomsAdapter.notifyDataSetChanged();
                                        mEmptyText.setVisibility(View.VISIBLE);
                                    } else {
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

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, final ChatRoom room, final int position, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle(room.getRoomName());
        MenuItem delete = contextMenu.add("Delete");
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                deleteRoom(position, room);
                return true;
            }
        });
        MenuItem mute = contextMenu.add(room.isMuted ? "Unmute" : "Mute");
        mute.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!room.isMuted) {
                    new RadioButtonDialog<>(getContext(), ChatSettingsFragment.MUTE_OPTIONS_TEXT, ChatSettingsFragment.MUTE_OPTIONS_VALUES)
                            .setDurationSelectedListener(new RadioButtonDialog.DurationSelectedListener<Integer>() {
                                @Override
                                public void onDurationSelected(Integer item) {
                                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                    if (activity != null) {
                                        try {
                                            JSONObject jsonParams = new JSONObject();
                                            jsonParams.put("mute", true);
                                            jsonParams.put("room", room.roomId);
                                            jsonParams.put("time", item);
                                            activity.emitSocket(API_Methods.VERSION + ":rooms:mute", jsonParams);
//                                            mMuteRelease = System.currentTimeMillis() + item * 60 /*sec*/ * 1000 /*milli*/;
//                                            updateNotificationView();
                                            room.setMute(true, System.currentTimeMillis() + item * 60 /*sec*/ * 1000 /*milli*/);
                                            mRoomsAdapter.notifyItemChanged(position);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .create().show();
                } else {

                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity != null) {
                        try {
                            JSONObject jsonParams = new JSONObject();
                            jsonParams.put("mute", false);
                            jsonParams.put("room", room.roomId);
                            jsonParams.put("time", 0);
                            activity.emitSocket(API_Methods.VERSION + ":rooms:mute", jsonParams);
                            room.setMute(false, 0);
                            mRoomsAdapter.notifyItemChanged(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

            }

            return true;
        }
    }

    );
}


}
