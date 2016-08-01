package com.linute.linute.MainContent.Chat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.GalleryFragment;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 7/8/16.
 */
public class ChatSettingsFragment extends BaseFragment {

    public static final String TAG = "ChatSettingsFragment";

    private static final String ARG_ROOM_ID = "roomId";
    public static final String ARG_USER = "userId";
    public static final int MENU_USER_DELETE = 0;
    public static final int NO_MUTE = 0;
    public static final int REQUEST_PHOTO = 1;

    private String mRoomId;
    private String mUserId;


    private int mType = ChatRoom.ROOM_TYPE_GROUP;
    private String mRoomName;
    private String mRoomImage;
    private User mUser;
    private ArrayList<User> mParticipants = new ArrayList<>();
    private long mMuteRelease = 0;


    private ChatParticipantsAdapter mParticipantsAdapter;
    public static final String[] MUTE_OPTIONS_TEXT = new String[]{"1 Hour", "8 Hours", "24 Hours", "Until I Unmute"};
    public static final Integer[] MUTE_OPTIONS_VALUES = new Integer[]{60, 8 * 60, 24 * 60, 0};
    private TextView mNotificationSettingsView;
    private TextView mNotificationSettingsIndicatorView;

    public static ChatSettingsFragment newInstance(String roomId, String userId) {
        ChatSettingsFragment fragment = new ChatSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_USER, userId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ARG_ROOM_ID);
            mUserId = getArguments().getString(ARG_USER);
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


//                    mChatRoom = ChatRoom.fromJSON(chat.getJSONObject("room"));

                    JSONObject room = chat.getJSONObject("room");

                    mRoomName = room.getString("name");
                    mRoomImage = room.getJSONObject("profileImage").getString("original");
                    mType = room.getInt("type");

                    /*JSONArray muteList = room.getJSONArray("mute");
                    JSONObject mute = null;

                    if(muteList != null)
                    for(int i = 0; i < muteList.length();i++){
                        if(muteList.getJSONObject(i).getString("user").equals(mUserId)){
                            mute = muteList.getJSONObject(i);
                            break;
                        }
                    }
                    if (mute != null) {
                        mMuteRelease = mute.getLong("time");
                    }*/

                    Object mute = room.get("unMuteAt");
                    if (mute != null) {
                        try {
                            mMuteRelease = Long.valueOf(mute.toString());
                        } catch (NumberFormatException e) {

                        }
                    }

                    JSONArray users = room.getJSONArray("users");

                    mParticipants.clear();
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        if (user.getString("id").equals(mUserId)) {
                            mUser = new User(
                                    user.getString("id"),
                                    user.getString("fullName"),
                                    user.getString("profileImage"),
                                    user.getJSONObject("college").getString("name")

                            );
                        } else {
                            mParticipants.add(new User(
                                    user.getString("id"),
                                    user.getString("fullName"),
                                    user.getString("profileImage"),
                                    user.getJSONObject("college").getString("name")

                            ));
                        }
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
        display();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_settings, container, false);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.getSupportFragmentManager().popBackStack();
                }
            }
        });
        display();
    }

    /*
    * i added version + ':rooms:mute’ for disable push notification for each room

{
mute: true|false,
room: id of room
}
    *
    * */

   /* @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }*/

    public void display() {
        View view = getView();
        if (view == null) return;

        RecyclerView participantsRV = (RecyclerView) view.findViewById(R.id.list_participants);
        View leaveGroupView = view.findViewById(R.id.setting_leave_group);
        mNotificationSettingsView = (TextView) view.findViewById(R.id.setting_notifications_button);
        mNotificationSettingsIndicatorView = (TextView) view.findViewById(R.id.setting_notifications_indicator);
        View DMHeader = view.findViewById(R.id.dm_header);
        View DMDivider = view.findViewById(R.id.dm_divider);


        if (mType == ChatRoom.ROOM_TYPE_DM) {
            participantsRV.setVisibility(View.GONE);
            leaveGroupView.setVisibility(View.GONE);
            DMHeader.setVisibility(View.VISIBLE);
            DMDivider.setVisibility(View.VISIBLE);
        } else {
            participantsRV.setVisibility(View.VISIBLE);
            leaveGroupView.setVisibility(View.VISIBLE);
            DMHeader.setVisibility(View.GONE);
            DMDivider.setVisibility(View.GONE);
        }
        if (mParticipants != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getContext()) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            llm.setAutoMeasureEnabled(true);
            participantsRV.setLayoutManager(llm);
            mParticipantsAdapter = new ChatParticipantsAdapter(mParticipants);
            participantsRV.setAdapter(mParticipantsAdapter);
            mParticipantsAdapter.notifyDataSetChanged();

            mParticipantsAdapter.setUserClickListener(new ChatParticipantsAdapter.OnUserClickListener() {
                @Override
                public void OnUserClick(User user) {
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    TaptUserProfileFragment fragment = TaptUserProfileFragment.newInstance(user.userName, user.userId);
                    activity.addFragmentToContainer(fragment);
                }

                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, final User user, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    contextMenu.setHeaderTitle(user.userName);
                    MenuItem item = contextMenu.add(0, MENU_USER_DELETE, 0, "Delete");
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Toast.makeText(getContext(), "Remove", Toast.LENGTH_SHORT).show();
                            try {
                                deleteUser(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;

                        }
                    });
                }
            });

            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitle(mRoomName);

            mParticipantsAdapter.setAddPeopleListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    SelectUsersFragment selectUserFragment = SelectUsersFragment.newInstance(mParticipants);
                    selectUserFragment.setOnUsersSelectedListener(new SelectUsersFragment.OnUsersSelectedListener() {
                        @Override
                        public void onUsersSelected(ArrayList<User> users) {
                            try {
                                addUsers(users);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    activity.replaceContainerWithFragment(selectUserFragment);
                }
            });
        }

        updateRoomPhoto(view);
        updateRoomName(view);


        updateNotificationView();
        mNotificationSettingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (mMuteRelease == NO_MUTE) {
                    new RadioButtonDialog<>(view.getContext(), MUTE_OPTIONS_TEXT, MUTE_OPTIONS_VALUES)
                            .setDurationSelectedListener(new RadioButtonDialog.DurationSelectedListener<Integer>() {
                                @Override
                                public void onDurationSelected(Integer item) {
                                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                    if (activity != null) {
                                        try {
                                            JSONObject jsonParams = new JSONObject();
                                            jsonParams.put("mute", true);
                                            jsonParams.put("room", mRoomId);
                                            jsonParams.put("time", item);
                                            activity.emitSocket(API_Methods.VERSION + ":rooms:mute", jsonParams);
                                            mMuteRelease = System.currentTimeMillis() + item * 60 /*sec*/ * 1000 /*milli*/;
                                            updateNotificationView();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .create().show();
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Unmute this chat?")
                            .setPositiveButton("Unmute", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                    if (activity != null) {
                                        try {
                                            JSONObject jsonParams = new JSONObject();
                                            jsonParams.put("mute", false);
                                            jsonParams.put("room", mRoomId);
                                            jsonParams.put("time", 0);
                                            activity.emitSocket(API_Methods.VERSION + ":rooms:mute", jsonParams);
                                            mMuteRelease = 0;
                                            updateNotificationView();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create().show();
                }
            }
        });


        leaveGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Leave Group")
                        .setMessage("If you leave the group, you will lose access to the chat log. You can be added back to the group")
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    deleteUser(mUser);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                getActivity().getSupportFragmentManager().popBackStack();
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create().show();
            }
        });


    }

    private void updateRoomName(View view) {
        View groupNameSettingView = view.findViewById(R.id.setting_group_name);
        if (mType == ChatRoom.ROOM_TYPE_DM) {
            view.findViewById(R.id.setting_group_name_container).setVisibility(View.GONE);
            User u = mParticipants.get(0);

            ((TextView) view.findViewById(R.id.dm_user_name)).setText(u.userName);
            ((TextView) view.findViewById(R.id.dm_user_college)).setText(u.collegeName);
        } else {
            groupNameSettingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditTextDialog editTextDialog = new EditTextDialog(getContext());
                    editTextDialog
                            .setValue(mRoomName)
                            .setTitle("Set Group Name")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
//                                mRoomName = editTextDialog.getValue();
                                    setGroupNameAndPhoto(editTextDialog.getValue(), null);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create().show();
                }
            });
        }
    }

    private void updateRoomPhoto(View view) {
        ImageView groupImageSettingView = (ImageView) view.findViewById(R.id.setting_group_image);
        if (mType == ChatRoom.ROOM_TYPE_DM) {
            view.findViewById(R.id.setting_group_image_container).setVisibility(View.GONE);
            User u = mParticipants.get(0);

            Glide.with(getContext())
                    .load(Utils.getImageUrlOfUser(u.userImage))
                    .into(((ImageView) view.findViewById(R.id.dm_user_icon)));
        } else {
            Glide.with(getContext())
                    .load(mRoomImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(groupImageSettingView);

            groupImageSettingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent getPhotoIntent = new Intent();
                    getPhotoIntent.setType("image/*");
                    getPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);
                    getPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(getPhotoIntent, REQUEST_PHOTO);
                }
            });
        }
    }

    private void updateNotificationView() {
        if (mNotificationSettingsIndicatorView == null) return;
        mNotificationSettingsIndicatorView.setText(mMuteRelease == NO_MUTE ? "On" : "Off");
    }

    private void deleteUser(User user) throws JSONException {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        JSONObject paramsJSON = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        usersJSON.put(user.userId);
        paramsJSON.put("room", mRoomId);
        paramsJSON.put("users", usersJSON);


        activity.emitSocket(API_Methods.VERSION + ":rooms:delete users", paramsJSON);
    }

    private void addUsers(ArrayList<User> users) throws JSONException {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        JSONObject paramsJSON = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        for (User user : users) {
            usersJSON.put(user.userId);
        }
        paramsJSON.put("room", mRoomId);
        paramsJSON.put("users", usersJSON);

        activity.emitSocket(API_Methods.VERSION + ":rooms:add users", paramsJSON);
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        activity.connectSocket("add users", onAddUsers);
        activity.connectSocket("delete users", onDeleteUsers);


    }

    @Override
    public void onPause() {
        super.onPause();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        activity.disconnectSocket("add users", onAddUsers);
        activity.disconnectSocket("delete users", onDeleteUsers);
    }

    private Emitter.Listener onAddUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject event = new JSONObject(args[0].toString());
                            JSONArray users = event.getJSONArray("users");
//                            final int oldSize = mParticipants.size();
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                mParticipants.add(new User(
                                        user.getString("id"),
                                        user.getString("fullName"),
                                        user.getString("profileImage"),
                                        user.getJSONObject("college").getString("name")
                                ));
                            }

//                            mParticipantsAdapter.notifyItemRangeInserted(oldSize, mParticipants.size() - 1);
                            mParticipantsAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private Emitter.Listener onDeleteUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject event = new JSONObject(args[0].toString());
                            JSONArray users = event.getJSONArray("users");

                            final int[] removedPositions = new int[users.length()];
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(0);
                                for (int j = 0; j < mParticipants.size(); j++) {
                                    if (user.getString("id").equals(mParticipants.get(j).userId)) {
                                        removedPositions[i] = j;
                                        mParticipants.remove(j);
                                        break;
                                    }
                                }
                            }

                            for (int i : removedPositions) {
                                mParticipantsAdapter.notifyItemRemoved(i + 1);
                            }
//                            mParticipantsAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PHOTO) {

            new Thread(new Runnable() {
                //do this in a new thread because lots of processing
                @Override
                public void run() {
                    Uri uri = data.getData();
                    Uri path = Uri.parse(GalleryFragment.getPath(getActivity(), uri));
                    try {
                        Bitmap bmp = BitmapFactory.decodeFile(path.getPath());
                        FileOutputStream fos = new FileOutputStream(path.getPath());
                        bmp.compress(Bitmap.CompressFormat.JPEG, 10, fos);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    setGroupNameAndPhoto(null, path.getPath());
                }
            }).start();
        }


    }

    private void setGroupNameAndPhoto(String name, String photo) {
        new LSDKChat(getContext()).setGroupNameAndPhoto(mRoomId, name, photo, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Utils.showServerErrorToast(getContext());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {

                    JSONObject object = new JSONObject(response.body().string());

                    Log.d(TAG, object.toString(4));

                    mRoomImage = object.getString("image");
                    mRoomName = object.getString("name");
                    Activity activity = getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateRoomName(view);
                                updateRoomPhoto(view);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //:room:add users
    //:room:delete users

    //room:
    //users: [;    ]


}
