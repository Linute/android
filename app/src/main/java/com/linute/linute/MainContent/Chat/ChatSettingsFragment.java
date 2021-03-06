package com.linute.linute.MainContent.Chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.DiscoverFragment.BlockedUsersSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.ProfileCamera.ProfileCameraActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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
    public static final int REQUEST_PERMISSION_STORAGE = 5;

    private String mUserId;
    private ChatRoom mChatRoom;


    private ChatParticipantsAdapter mParticipantsAdapter;
    public static final String[] MUTE_OPTIONS_TEXT = new String[]{"1 Hour", "8 Hours", "24 Hours", "Until I Unmute"};
    public static final Integer[] MUTE_OPTIONS_VALUES = new Integer[]{60, 8 * 60, 24 * 60, 0};
    private TextView mNotificationSettingsView;
    private TextView mNotificationSettingsIndicatorView;

    private TaptSocket mSocket = TaptSocket.getInstance();

    public static ChatSettingsFragment newInstance(ChatRoom chatRoom) {
        ChatSettingsFragment fragment = new ChatSettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ROOM_ID, chatRoom);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChatRoom = getArguments().getParcelable(ARG_ROOM_ID);
        }
        mUserId = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", null);
        Map<String, Object> params = new HashMap<>();

        if (mChatRoom.roomImage == null)
            new LSDKChat(getContext()).getRoom(mChatRoom.roomId, params, getRoomCallback);


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
        View createGroupView = view.findViewById(R.id.dm_create_group);
        mNotificationSettingsView = (TextView) view.findViewById(R.id.setting_notifications_button);
        mNotificationSettingsIndicatorView = (TextView) view.findViewById(R.id.setting_notifications_indicator);


        View DMHeader = view.findViewById(R.id.dm_header);

//        View DMDivider = view.findViewById(R.id.dm_divider);
        View blockView = view.findViewById(R.id.dm_block);


        if (mChatRoom.isDM()) {
            participantsRV.setVisibility(View.GONE);
            leaveGroupView.setVisibility(View.GONE);
            DMHeader.setVisibility(View.VISIBLE);
            blockView.setVisibility(View.VISIBLE);

//            DMDivider.setVisibility(View.VISIBLE);
        } else {
            participantsRV.setVisibility(View.VISIBLE);
            leaveGroupView.setVisibility(View.VISIBLE);
            DMHeader.setVisibility(View.GONE);
            blockView.setVisibility(View.GONE);
//            DMDivider.setVisibility(View.GONE);
        }
        LinearLayoutManager llm = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        llm.setAutoMeasureEnabled(true);
        participantsRV.setLayoutManager(llm);
        mParticipantsAdapter = new ChatParticipantsAdapter(mChatRoom.users);
        mParticipantsAdapter.setRequestManager(Glide.with(this));
        participantsRV.setAdapter(mParticipantsAdapter);
        mParticipantsAdapter.notifyDataSetChanged();

        mParticipantsAdapter.setUserClickListener(new ChatParticipantsAdapter.OnUserClickListener() {
            @Override
            public void OnUserClick(User user) {
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                TaptUserProfileFragment fragment = TaptUserProfileFragment.newInstance(user.firstName + " " + user.lastName, user.userId);
                activity.addFragmentToContainer(fragment);
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, final User user, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    /*contextMenu.setHeaderTitle(user.firstName + " " + user.lastName);
                    MenuItem item = contextMenu.add(0, MENU_USER_DELETE, 0, "Delete");
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Toast.makeText(getContext(), "Remove", Toast.LENGTH_SHORT).show();
                            try {
                                leaveGroup();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;

                        }
                    });*/
            }
        });

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle((mChatRoom.isDM() ? "Message" : "Group") + " Settings");
//            toolbar.addView(LayoutInflater.from(getContext()).inflate(R.layout.toolbar_chat, toolbar, false));
//            ((TextView)toolbar.findViewById(R.id.toolbar_chat_name)).setText(mType == ChatRoom.ROOM_TYPE_DM ? "Message" : "Group" +  " Settings");
//            toolbar.findViewById(R.id.space_actions_item_balancer).setVisibility(View.GONE);
        mParticipantsAdapter.setAddPeopleListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                SelectUsersFragment selectUserFragment = SelectUsersFragment.newInstance(mChatRoom.users);
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

        updateRoomPhoto(view);
        updateRoomName(view);


        updateNotificationView();

        DMHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (activity != null) {
                    User user = mChatRoom.users.get(0);
                    TaptUserProfileFragment profile = TaptUserProfileFragment.newInstance(user.getFullName(), user.userId);
                    activity.addFragmentToContainer(profile);
                }
            }
        });

        getView().findViewById(R.id.setting_notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (mChatRoom.mutedUntil == NO_MUTE) {
                    new RadioButtonDialog<>(view.getContext(), MUTE_OPTIONS_TEXT, MUTE_OPTIONS_VALUES)
                            .setDurationSelectedListener(new RadioButtonDialog.DurationSelectedListener<Integer>() {
                                @Override
                                public void onDurationSelected(Integer item) {
                                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                                    if (activity != null) {
                                        try {
                                            JSONObject jsonParams = new JSONObject();
                                            jsonParams.put("mute", true);
                                            jsonParams.put("room", mChatRoom.roomId);
                                            jsonParams.put("time", item);
                                            mSocket.emit(API_Methods.VERSION + ":rooms:mute", jsonParams);
                                            mChatRoom.setMute(true, System.currentTimeMillis() + item * 60 /*sec*/ * 1000 /*milli*/);
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
                                            jsonParams.put("room", mChatRoom.roomId);
                                            jsonParams.put("time", 0);
                                            mSocket.emit(API_Methods.VERSION + ":rooms:mute", jsonParams);
                                            mChatRoom.setMute(false, 0);
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
                        .setMessage(R.string.leave_group_chat_dialog_text)
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    leaveGroup();
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

        createGroupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (activity != null) {

                    ArrayList<User> lockedUsers = new ArrayList<>();
                    lockedUsers.addAll(mChatRoom.users);
                    ArrayList<User> selectedUsers = new ArrayList<>();
                    selectedUsers.addAll(lockedUsers);
                    CreateChatFragment frag = CreateChatFragment.newInstance(lockedUsers, selectedUsers);
                    frag.setOnUsersSelectedListener(new SelectUsersFragment.OnUsersSelectedListener() {
                        @Override
                        public void onUsersSelected(ArrayList<User> users) {
                            activity.replaceContainerWithFragment(ChatFragment.newInstance(null, users));
                        }
                    });
                    frag.setOnRoomSelectedListener(new UserGroupSearchAdapter2.OnRoomClickListener() {
                        @Override
                        public void onRoomClick(ChatRoom room) {
                            activity.replaceContainerWithFragment(ChatFragment.newInstance(room));
                        }
                    });
                    activity.replaceContainerWithFragment(frag);
                }
            }
        });

        blockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block();
            }
        });


    }

    private void updateRoomName(final View view) {
        final TextView groupNameSettingTextView = (TextView) view.findViewById(R.id.setting_group_name_text);
        if (!"".equals(mChatRoom.getRoomName()) && mChatRoom.getRoomName() != null) {
            groupNameSettingTextView.setText(mChatRoom.getRoomName());
        }

        final View groupNameSettingView = view.findViewById(R.id.setting_group_name);
        if (mChatRoom.isDM()) {
            view.findViewById(R.id.setting_group_name_container).setVisibility(View.GONE);
            view.findViewById(R.id.dm_create_group).setVisibility(View.VISIBLE);
            User u = mChatRoom.users.get(0);

            ((TextView) view.findViewById(R.id.dm_user_name)).setText(u.getFullName());
            ((TextView) view.findViewById(R.id.dm_user_college)).setText(u.collegeName);
        } else {
            view.findViewById(R.id.setting_group_name_container).setVisibility(View.VISIBLE);
            groupNameSettingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {


                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    final EditTextDialog editTextDialog = new EditTextDialog(getContext());
                    final TextView editText = editTextDialog.mEditText;
                    editTextDialog
                            .setValue(mChatRoom.getRoomName())
                            .setTitle("Set Group Name")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
//                                roomName = editTextDialog.getValue();
                                    setGroupNameAndPhoto(editTextDialog.getValue(), null);
//                                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", null);
                            /*.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                            })*/
                            /*.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    *//*inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(),0, new ResultReceiver(new Handler()){
                                        @Override
                                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                                            super.onReceiveResult(resultCode, resultData);
                                        }
                                    });*//*
                                }
                            })
                    ;*/

                    editTextDialog.getEditText().setImeActionLabel("Ok", EditorInfo.IME_ACTION_DONE);

                    final AlertDialog dialog = editTextDialog.create();

                    editTextDialog.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                            if (i == EditorInfo.IME_ACTION_DONE) {
                                setGroupNameAndPhoto(editTextDialog.getValue(), null);
//                                inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                                dialog.dismiss();

                                return true;
                            }
                            return false;
                        }
                    });


                    dialog.show();
                    editText.requestFocus();
//                    inputMethodManager.toggleSoftInput(0, 0);
                }
            });
        }
    }

    private void updateRoomPhoto(View view) {
        final ImageView groupImageSettingView = (ImageView) view.findViewById(R.id.setting_group_image);
        final View groupImageProgressBar = view.findViewById(R.id.group_image_progress_bar);

        groupImageSettingView.setImageBitmap(null);
        groupImageProgressBar.setVisibility(View.VISIBLE);


        if (mChatRoom.isDM()) {
            view.findViewById(R.id.setting_group_image_container).setVisibility(View.GONE);
            User u = mChatRoom.users.get(0);

            Glide.with(this)
                    .load(Utils.getImageUrlOfUser(u.userImage))
                    .into(((ImageView) view.findViewById(R.id.dm_user_icon)));
        } else {
            Log.d(TAG, "updateRoomPhoto: "+mChatRoom.roomImage);
            Glide.with(this)
                    .load(mChatRoom.roomImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .signature(new StringSignature(getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, getContext().MODE_PRIVATE).getString("imageSigniture", "000")))
                    //random signature to invalidate cache
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Activity a = getActivity();
                            if (a != null) {
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        groupImageProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                            return false;
                        }
                    })
                    .signature(new StringSignature("" + Math.random()))
                    .into(groupImageSettingView);


            groupImageSettingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION_STORAGE);
                    } else {
                        startGetImageActivity();
                    }
                }
            });
        }
    }

    private void block() {

        new AlertDialog.Builder(getContext()).setMessage(R.string.block_confirmation_text)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                        if (activity == null) return;

                        if (!Utils.isNetworkAvailable(activity) || !mSocket.socketConnected()) {
                            Utils.showBadConnectionToast(activity);
                            return;
                        }

                        JSONObject emit = new JSONObject();
                        try {
                            emit.put("block", true);
                            emit.put("user", mChatRoom.users.get(0).userId);
                            mSocket.emit(API_Methods.VERSION + ":users:block:real", emit);

                            String message;
                            message = "You will no longer see this user and they won't be able to see you";
                            BlockedUsersSingleton.getBlockedListSingletion().add(mChatRoom.users.get(0).userId);

                            ((MainActivity) getActivity()).setFragmentOfIndexNeedsUpdating(
                                    FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);

                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            getFragmentManager().popBackStack();
                            getFragmentManager().popBackStack();

                        } catch (JSONException e) {
                            Utils.showServerErrorToast(activity);
                            e.printStackTrace();
                        }
                    }
                })
                .create().show();

    }

    protected void startGetImageActivity() {
        if (getActivity() == null) return;
        String[] options = {"Camera", "Photo Gallery"};
        new android.app.AlertDialog.Builder(getActivity())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int type = 0;
                        switch (which) {
                            case 0:
                                type = ProfileCameraActivity.TYPE_CAMERA;
                                break;
                            case 1:
                                type = ProfileCameraActivity.TYPE_GALLERY;
                                break;
                            default:
                                break;
                        }

                        Intent getPhotoIntent = new Intent(getContext(), ProfileCameraActivity.class);
                        getPhotoIntent.putExtra(ProfileCameraActivity.TYPE_KEY, type);
                        startActivityForResult(getPhotoIntent, REQUEST_PHOTO);
                    }
                })
                .show();
    }


    private void updateNotificationView() {
        if (mNotificationSettingsIndicatorView == null) return;
        mNotificationSettingsIndicatorView.setText(mChatRoom.mutedUntil == NO_MUTE ? "On" : "Off");
    }

    private void leaveGroup() throws JSONException {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        JSONObject paramsJSON = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        usersJSON.put(mUserId);
        paramsJSON.put("room", mChatRoom.roomId);
        paramsJSON.put("users", usersJSON);


        mSocket.emit(API_Methods.VERSION + ":rooms:delete users", paramsJSON);
    }

    private void addUsers(ArrayList<User> users) throws JSONException {
        mChatRoom.users.addAll(users);


        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        JSONObject paramsJSON = new JSONObject();
        JSONArray usersJSON = new JSONArray();
        for (User user : users) {
            usersJSON.put(user.userId);
        }
        paramsJSON.put("room", mChatRoom.roomId);
        paramsJSON.put("users", usersJSON);


        mSocket.emit(API_Methods.VERSION + ":rooms:add users", paramsJSON);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSocket.on("add users", onAddUsers);
        mSocket.on("delete users", onDeleteUsers);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGetImageActivity();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSocket.off("add users", onAddUsers);
        mSocket.off("delete users", onDeleteUsers);
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

                                mChatRoom.users.add(new User(
                                        user.getString("id"),
                                        user.getString("firstName"),
                                        user.getString("lastName"),
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
                                for (int j = 0; j < mChatRoom.users.size(); j++) {
                                    if (user.getString("id").equals(mChatRoom.users.get(j).userId)) {
                                        removedPositions[i] = j;
                                        mChatRoom.users.remove(j);
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
                    Uri path = uri;
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
        new LSDKChat(getContext()).setGroupNameAndPhoto(mChatRoom.roomId, name, photo, new Callback() {
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

                    mChatRoom.roomImage = object.getJSONObject("profileImage").getString("original");
                    mChatRoom.roomName = object.getString("name");
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

    Callback getRoomCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            try {
                JSONObject obj = new JSONObject(response.body().string());
                Log.i("AAA", obj.toString(4));

                mChatRoom.roomImage = obj.getJSONObject("profileImage").getString("original");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() != null)
                            updateRoomPhoto(getView());
                    }
                });


            } catch (JSONException e) {

            }

        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mParticipantsAdapter.getRequestManager() != null)
            mParticipantsAdapter.getRequestManager().onDestroy();
    }
}
