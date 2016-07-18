package com.linute.linute.MainContent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.DiscoverFragment.BlockedUsersSingleton;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverHolderFragment;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.Global.GlobalFragment;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.MainContent.UpdateFragment.Update;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.MainContent.Uploading.UploadIntentService;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;
import com.linute.linute.UtilsAndHelpers.FiveStarRater.FiveStarsDialog;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class MainActivity extends BaseTaptActivity {

    public static String TAG = MainActivity.class.getSimpleName();
    public static final int PHOTO_STATUS_POSTED = 19;
    public static final String PROFILE_OR_EVENT_NAME = "profileOrEvent";

    private DrawerLayout mDrawerLayout;

    //don't change fragment until the drawer is closed, or there will be slight lag
    private MainDrawerListener mMainDrawerListener;
    private NavigationView mNavigationView;
    private MenuItem mPreviousItem;

    // array of our fragments
    // the fragment is not created until it is needed.
    private BaseFragment[] mFragments; //holds our fragments


    private boolean mWatchForRefresh = false;
    private SharedPreferences mSharedPreferences;

    private SocketErrorResponse mSocketErrorResponse;

    public static class FRAGMENT_INDEXES {
        public static final short PROFILE = 0;
        public static final short FEED = 1;
        public static final short GLOBAL = 2;
        public static final short ACTIVITY = 3;
        public static final short FIND_FRIENDS = 4;
    }


    private Socket mSocket;
    private boolean mConnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mFragments = new BaseFragment[4];
        mWatchForRefresh = false;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainActivity_drawerLayout);
        mMainDrawerListener = new MainDrawerListener();
        mDrawerLayout.addDrawerListener(mMainDrawerListener);
        mNavigationView = (NavigationView) findViewById(R.id.mainActivity_navigation_view);

        //profile image and header setup
        loadDrawerHeader();

        //set click listener for header - taken to profile
        mNavigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();

                if (getSupportFragmentManager().getBackStackEntryCount() > 1) clearBackStack();

                if (mPreviousItem != null) { //profile doesn't get checked
                    mPreviousItem.setChecked(false);
                    mPreviousItem = null;
                    replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PROFILE));
                } else {
                    getFragment(FRAGMENT_INDEXES.PROFILE).resetFragment();
                }
            }
        });

        //setNavigationView action
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_item_feed:
                        navItemSelected(FRAGMENT_INDEXES.FEED, item);
                        break;
                    case R.id.navigation_item_global:
                        navItemSelected(FRAGMENT_INDEXES.GLOBAL, item);
                        break;
                    case R.id.navigation_item_find_friends:
                        navItemSelected(FRAGMENT_INDEXES.FIND_FRIENDS, item);
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        clearBackStack();

        if (mPreviousItem != null) mPreviousItem.setChecked(false);
        mPreviousItem = mNavigationView.getMenu().findItem(R.id.navigation_item_feed);
        mPreviousItem.setChecked(true);

        //only loads one fragment
        addFragmentToContainer(getFragment(FRAGMENT_INDEXES.FEED));

        Intent intent = getIntent();
        if (intent != null) {
            checkIntent(intent);
        }

        new FiveStarsDialog(this, "support@tapt.io")
                .setRateText("how are we doing?")
                .setRateText("wasup! we see you come here often, how are you liking it so far?")
                .setUpperBound(4)
                .showAfter(10);
    }


    private void navItemSelected(short position, MenuItem item) {
        int backstack = getSupportFragmentManager().getBackStackEntryCount();

        if (mPreviousItem != null) {
            if (mPreviousItem != item || backstack > 1) {
                mPreviousItem.setChecked(false);
            } else {
                if (position != FRAGMENT_INDEXES.FIND_FRIENDS)
                    getFragment(position).resetFragment();
                return;
            }
        }

        mPreviousItem = item;
        mPreviousItem.setChecked(true);
        replaceContainerWithFragment(
                position == FRAGMENT_INDEXES.FIND_FRIENDS ?
                        FindFriendsChoiceFragment.newInstance(true) :
                        getFragment(position));

        clearBackStack();
    }


    private BaseFragment getFragment(short index) {
        if (mFragments[index] == null) { //if fragment haven't been created yet, create it
            BaseFragment fragment;

            switch (index) {
                case FRAGMENT_INDEXES.FEED:
                    fragment = new DiscoverHolderFragment();
                    break;
                case FRAGMENT_INDEXES.ACTIVITY:
                    fragment = new UpdatesFragment();
                    break;
                case FRAGMENT_INDEXES.PROFILE:
                    fragment = new Profile();
                    break;
                case FRAGMENT_INDEXES.GLOBAL:
                    fragment = new GlobalFragment();
                    break;
                default:
                    fragment = null;
                    break;
            }
            mFragments[index] = fragment;
        }

        return mFragments[index];
    }

    @Override
    public void replaceContainerWithFragment(final Fragment fragment) {
        //this will only run after drawer is fully closed
        //lags if we don't do this
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mMainDrawerListener.setChangeFragmentOrActivityAction(new Runnable() {
                @Override
                public void run() {
                    if (mSafeForFragmentTransaction) {
                        MainActivity.this.getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold)
                                .replace(R.id.mainActivity_fragment_holder, fragment)
                                .addToBackStack(PROFILE_OR_EVENT_NAME)
                                .commit();
                    }
                }
            });
        } else {
            if (mSafeForFragmentTransaction) {
                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold)
                        .replace(R.id.mainActivity_fragment_holder, fragment)
                        .addToBackStack(PROFILE_OR_EVENT_NAME)
                        .commit();
            }
        }
    }


    public static final int SETTINGS_REQUEST_CODE = 13;

    public void startEditProfileActivity(Class activity) {
        Intent i = new Intent(MainActivity.this, activity);
        startActivityForResult(i, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) { //came back from settings
            setFragmentOfIndexNeedsUpdating(BaseFragment.FragmentState.NEEDS_UPDATING, FRAGMENT_INDEXES.PROFILE);
            loadDrawerHeader(); //reload drawer header
        } else if (requestCode == PHOTO_STATUS_POSTED && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, UploadIntentService.class);
            intent.putExtra(PendingUploadPost.PENDING_POST_KEY, data.getParcelableExtra(PendingUploadPost.PENDING_POST_KEY));
            startService(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //use this when you need to add another users profile view
    //or load image or status
    @Override
    public void addFragmentToContainer(final Fragment fragment) {
        if (!mSafeForFragmentTransaction) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainActivity_fragment_holder, fragment)
                .addToBackStack(PROFILE_OR_EVENT_NAME)
                .commit();
    }

    @Override
    public void addFragmentToContainer(final Fragment fragment, String tag) {
        if (!mSafeForFragmentTransaction) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainActivity_fragment_holder, fragment, tag)
                .addToBackStack(PROFILE_OR_EVENT_NAME)
                .commit();
    }

    @Override
    public void addFragmentOnTop(Fragment fragment, String tag) {
        if (!mSafeForFragmentTransaction) return;
        hideKeyboard();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold, R.anim.hold, R.anim.frag_fade_out)
                .add(R.id.mainActivity_fragment_holder, fragment, tag)
                .addToBackStack(PROFILE_OR_EVENT_NAME)
                .commit();
    }


    public void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null && v instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    public void clearBackStack() {
        //if there are a lot of other user profile/ events in mainActivity, clear them
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate(PROFILE_OR_EVENT_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    //sets needsUpdating for fragment at index
    @Override
    public void setFragmentOfIndexNeedsUpdating(BaseFragment.FragmentState state, int index) {
        if (mFragments[index] != null) {
            mFragments[index].setFragmentState(state);
        }
    }

    //loads the header of our drawer
    public void loadDrawerHeader() {
        //profile image
        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        int size = getResources().getDimensionPixelSize(R.dimen.nav_header_profile_side); //radius

        View header = mNavigationView.getHeaderView(0);

        //set name text
        String name = sharedPreferences.getString("firstName", "") + " " + sharedPreferences.getString("lastName", "");
        ((TextView) header.findViewById(R.id.drawerHeader_name)).setText(name);

        Glide.with(this)
                .load(Utils.getImageUrlOfUser(sharedPreferences.getString("profileImage", "")))
                .dontAnimate()
                .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000")))
                .override(size, size) //change image to the size we want
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into((CircleImageView) header.findViewById(R.id.navigation_header_profile_image));
    }

    public void noInternet() {
        Utils.showBadConnectionToast(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            if ((mPreviousItem == null || mPreviousItem.getItemId() != R.id.navigation_item_feed)) {
                clearBackStack();
                if (mPreviousItem != null) mPreviousItem.setChecked(false);
                mPreviousItem = mNavigationView.getMenu().findItem(R.id.navigation_item_feed);
                mPreviousItem.setChecked(true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainActivity_fragment_holder, getFragment(FRAGMENT_INDEXES.FEED))
                        .addToBackStack(PROFILE_OR_EVENT_NAME)
                        .commit();
            } else {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Stuff for drawer notification indicator
     */

    public void setNavItemNotification(@IdRes int itemId, int count) {
        View main = mNavigationView.getMenu().findItem(itemId).getActionView();

        if (count > 0) {
            ((TextView) main.findViewById(R.id.text)).setText(count > 99 ? "+" : String.valueOf(count));
            main.setVisibility(View.VISIBLE);
        } else {
            main.setVisibility(View.GONE);
        }
    }

    public void setFeedNotification(int count) {
        setNavItemNotification(R.id.navigation_item_feed, count);
    }


    //So we change fragments or activities only after the drawer closes
    private class MainDrawerListener extends DrawerLayout.SimpleDrawerListener {

        private Runnable mChangeFragmentOrActivityAction;
        private boolean mKeyboardHasBeenClosed = false;

        @Override
        public void onDrawerClosed(View drawerView) {
            //will go to next activity or fragment after drawer is closed
            if (mChangeFragmentOrActivityAction != null) {
                mChangeFragmentOrActivityAction.run();
                mChangeFragmentOrActivityAction = null;
            }
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

            super.onDrawerSlide(drawerView, slideOffset);

            if (!mKeyboardHasBeenClosed && slideOffset > 0) { //close keyboard
                mKeyboardHasBeenClosed = true;
                View focused = getCurrentFocus();
                if (focused != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                }
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (newState == DrawerLayout.STATE_IDLE) mKeyboardHasBeenClosed = false;
            super.onDrawerStateChanged(newState);
        }

        public void setChangeFragmentOrActivityAction(Runnable action) {
            mChangeFragmentOrActivityAction = action;
        }
    }


    private boolean mSafeForFragmentTransaction = true;

    /******
     * Socket stuff
     ********/

    @Override
    protected void onResume() {
        super.onResume();

        mSafeForFragmentTransaction = true;

        if ((mSocket == null || !mSocket.connected()) && !mConnecting) {
            mConnecting = true;

            {
                try {
                    IO.Options op = new IO.Options();
                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(this);
                    op.query =
                            "token=" + mSharedPreferences.getString("userToken", "") +
                                    "&deviceToken=" + device.getDeviceToken() +
                                    "&udid=" + device.getUdid() +
                                    "&version=" + device.getVersionName() +
                                    "&build=" + device.getVersionCode() +
                                    "&os=" + device.getOS() +
                                    "&platform=" + device.getType() +
                                    "&api=" + API_Methods.VERSION +
                                    "&model=" + device.getModel();

                    op.reconnectionDelay = 5;
                    op.secure = true;
                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/

                    mSocket.on("activity", newActivity);
                    mSocket.on("new post", newPostListener);
                    mSocket.on("user banned", userBanned);
                    mSocket.on("update", update);
                    mSocket.on("badge", badge);
                    mSocket.on("unread", haveUnread);
                    mSocket.on("posts refresh", refresh);
                    mSocket.on("memes", meme);
                    mSocket.on("filters", filter);
                    mSocket.on("status colors", statusColors);
                    mSocket.on("blocked", blocked);
                    mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                    mSocket.on(Socket.EVENT_ERROR, onEventError);
                    mSocket.connect();
                    mConnecting = false;

                    mSocket.emit(API_Methods.VERSION + ":posts:refresh", new JSONObject());
                    //emitSocket(API_Methods.VERSION + ":messages:unread", new JSONObject());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        mWatchForRefresh = true;
        mSafeForFragmentTransaction = false;

        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("activity", newActivity);
            mSocket.off("new post", newPostListener);
            mSocket.off("user banned", userBanned);
            mSocket.off("update", update);
            mSocket.off("badge", badge);
            mSocket.off("unread", haveUnread);
            mSocket.off("posts refresh", refresh);
            mSocket.off("memes", meme);
            mSocket.off("filters", filter);
            mSocket.off("status colors", statusColors);
            mSocket.off("blocked", blocked);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onSocketTimeOut);
            mSocket.off(Socket.EVENT_ERROR, onEventError);
        }
    }


    @Override
    public void connectSocket(String event, Emitter.Listener emitter) {
        if (mSocket != null) {
            mSocket.on(event, emitter);
        }
    }

    @Override
    public void emitSocket(String event, Object arg) {
        if (mSocket != null)
            mSocket.emit(event, arg);
    }

    @Override
    public void disconnectSocket(String event, Emitter.Listener emitter) {
        if (mSocket != null) {
            mSocket.off(event, emitter);
        }
    }

    @Override
    public boolean socketConnected() {
        return mSocket.connected();
    }


    @Override
    public void setSocketErrorResponse(SocketErrorResponse error) {
        mSocketErrorResponse = error;
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: failed socket connection");
        }
    };

    private Emitter.Listener onEventError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "ERROR: " + args[0]);

            //our fragments might have something they must do if an error occurs with socket
            //i.e. if a progressbar is shown, we should hide it if error occured
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSocketErrorResponse != null) {
                        mSocketErrorResponse.runSocketError();
                    }

                    Utils.showServerErrorToast(MainActivity.this);
                }
            });
        }
    };


    private Emitter.Listener onSocketTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSocketErrorResponse != null) {
                        mSocketErrorResponse.runSocketError();
                    }
                    Utils.showBadConnectionToast(MainActivity.this);
                }
            });
        }
    };

    private Emitter.Listener newPostListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mFragments[FRAGMENT_INDEXES.FEED] != null) {
                try {
                    final Post postObj = new Post((JSONObject) args[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //the owner of the post is not blocked
                            if (!BlockedUsersSingleton.getBlockedListSingletion().contains(postObj.getUserId())) {
                                if (!NotificationsCounterSingleton.getInstance().hasNewPosts()) {
                                    NotificationEventBus
                                            .getInstance()
                                            .setNotification(new NotificationEvent(NotificationEvent.DISCOVER, true));
                                }

                                setFeedNotification(NotificationsCounterSingleton.getInstance().incrementPosts());

                                if (!((DiscoverHolderFragment) mFragments[FRAGMENT_INDEXES.FEED])
                                        .addPostToFeed(postObj)) {
                                    NotificationsCounterSingleton.getInstance().setDiscoverNeedsRefreshing(true);
                                }
                            }
                        }
                    });
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener newActivity = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject activity = new JSONObject(args[0].toString());
                //message
                if (activity.getString("action").equals("messager")) {
                    final NewMessageEvent chat = new NewMessageEvent(true);
                    chat.setRoomId(activity.getString("room"));
                    chat.setMessage(activity.getString("text"));
                    chat.setOtherUserId(activity.getString("ownerID"));
                    chat.setOtherUserName(activity.getString("ownerFullName"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newMessageSnackbar(chat);
                        }
                    });

                    NewMessageBus.getInstance().setNewMessage(chat);
                } else {
                    final Update update = new Update(activity);
                    if (update.getUpdateType() != Update.UpdateType.UNDEFINED) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsCounterSingleton.getInstance().incrementActivities();

                                if (mFragments[FRAGMENT_INDEXES.ACTIVITY] != null)
                                    ((UpdatesFragment) mFragments[FRAGMENT_INDEXES.ACTIVITY]).addItemToRecents(update);

                                NotificationEventBus
                                        .getInstance()
                                        .setNotification(new NotificationEvent(NotificationEvent.ACTIVITY, true));

                                if (update.hasEventInformation()) {
                                    newEventSnackbar(update.getDescription(), update.getPost());
                                } else {
                                    newProfileSnackBar(update);
                                }
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void newMessageSnackbar(final NewMessageEvent chatEvent) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, chatEvent.getMessage(), CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryColor));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToContainer(ChatFragment.newInstance(chatEvent.getRoomId(),
                        chatEvent.getOtherUserName(), chatEvent.getOtherUserId()));

                sn.dismiss();
            }
        });
        sn.show();
    }


    private void newEventSnackbar(String text, final Post post) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, text, CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryColor));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
                    addActivityFragment();

                addFragmentToContainer(FeedDetailPage.newInstance(post));
                sn.dismiss();
            }
        });
        sn.show();
    }

    private void newProfileSnackBar(final Update update) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, update.getDescription(), CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryColor));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
                    addActivityFragment();

                addFragmentToContainer(TaptUserProfileFragment.newInstance(update.getUserFullName(), update.getUserId()));
                sn.dismiss();
            }
        });
        sn.show();
    }


    //when notification is pressed, takes you to updates fragment
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(Intent intent) {
        int type = intent.getIntExtra("NOTIFICATION", LinuteConstants.MISC);
        if (type == LinuteConstants.FEED_DETAIL) {
            String id = intent.getStringExtra("event");
            if (id != null) {
                mSafeForFragmentTransaction = true;
                if (getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
                    addFragmentToContainer(getFragment(FRAGMENT_INDEXES.ACTIVITY));
                addFragmentToContainer(FeedDetailPage.newInstance(
                        new Post("", id, null, "")
                ));
            }
        } else if (type == LinuteConstants.PROFILE) {
            String id = intent.getStringExtra("user");
            if (id != null) {
                mSafeForFragmentTransaction = true;
                if (getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
                    addFragmentToContainer(getFragment(FRAGMENT_INDEXES.ACTIVITY));
                addFragmentToContainer(TaptUserProfileFragment.newInstance("", id));
            }
        } else if (type == LinuteConstants.MESSAGE) {
            String room = intent.getStringExtra("room");
            String userId = intent.getStringExtra("ownerID");
            String userName = intent.getStringExtra("ownerFullName");
            mSafeForFragmentTransaction = true;
            addFragmentToContainer(new RoomsActivityFragment());
            addFragmentToContainer(ChatFragment.newInstance(room == null || room.isEmpty() ? null : room,
                    userName, userId.isEmpty() ? null : userId));
        }
    }

    private Emitter.Listener userBanned = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject body = new JSONObject(args[0].toString());
                Log.i(TAG, "call: " + body.toString(4));
                final String text = body.getString("text");

                emitSocket(API_Methods.VERSION + ":users:logout", new JSONObject());
                Utils.resetUserInformation(getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE));
                Utils.deleteTempSharedPreference(getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE));
                if (AccessToken.getCurrentAccessToken() != null) //log out facebook if logged in
                    LoginManager.getInstance().logOut();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //start new
                        Intent i = new Intent(MainActivity.this, PreLoginActivity.class);
                        i.putExtra("BANNED", text);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //don't let them come back
                        startActivity(i);
                        finish();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //badge
    private Emitter.Listener badge = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject badge = new JSONObject(args[0].toString());
                NotificationsCounterSingleton.getInstance().setHasMessage(badge.getInt("messages") > 0);
                NewMessageBus.getInstance().setNewMessage(new NewMessageEvent(NotificationsCounterSingleton.getInstance().hasMessage()));

                int activities = badge.getInt("activities");
                NotificationsCounterSingleton.getInstance().setNumOfNewActivities(activities);
                if (activities > 0) {
                    NotificationEventBus.getInstance().setNotification(new NotificationEvent(NotificationEvent.ACTIVITY, true));
                    NotificationsCounterSingleton.getInstance().setUpdatesNeedsRefreshing(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    //posts:refresh
    private Emitter.Listener refresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mWatchForRefresh) {
                try {
                    final int posts = new JSONObject(args[0].toString()).getInt("posts");
                    if (posts > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFeedNotification(NotificationsCounterSingleton.getInstance().incrementPosts(posts));
                            }
                        });

                        NotificationEventBus.getInstance().setNotification(new NotificationEvent(NotificationEvent.DISCOVER, true));
                        NotificationsCounterSingleton.getInstance().setDiscoverNeedsRefreshing(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mWatchForRefresh = true;
            }
        }
    };


    private Emitter.Listener haveUnread = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            NotificationsCounterSingleton.getInstance().setHasMessage((boolean) args[0]);
            NewMessageBus.getInstance().setNewMessage(new NewMessageEvent(NotificationsCounterSingleton.getInstance().hasMessage()));
        }
    };

    private Emitter.Listener update = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject body = new JSONObject(args[0].toString());
                final String text = body.getString("text");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUpdateSnackbar(text);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener meme = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject body = new JSONObject(args[0].toString());
                JSONArray memes = body.getJSONArray("memes");

                File memeDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");
                memeDir.mkdirs();


                for (File f : memeDir.listFiles()) {
                    for (int i = 0; i < memes.length(); i++) {
                        String fileName = memes.getString(i);
                        if (fileName.equals(f)) break;
                        if (i == memes.length() - 1)
                            f.delete();
                    }
                }

                for (int i = 0; i < memes.length(); i++) {
                    final String fileName = memes.getString(i);
                    final File file = new File(memeDir, fileName);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                            File res = Glide.with(MainActivity.this)
                                    .load(Utils.getMemeImageUrl(fileName))
                                    .downloadOnly(1080, 1920).get();

                            FileOutputStream fos = new FileOutputStream(file);
                            FileInputStream fis = new FileInputStream(res);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = fis.read(buf)) > 0) {
                                fos.write(buf, 0, len);
                            }
                            fis.close();
                            fos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } catch (ExecutionException ee) {
                            ee.printStackTrace();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener filter = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject body = new JSONObject(args[0].toString());
                JSONArray filters = body.getJSONArray("filters");

                File filtersDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filters/");
                filtersDir.mkdirs();


                for (File f : filtersDir.listFiles()) {
                    for (int i = 0; i < filters.length(); i++) {
                        String fileName = filters.getString(i);
                        if (fileName.equals(f)) break;
                        if (i == filters.length() - 1)
                            f.delete();
                    }
                }

                for (int i = 0; i < filters.length(); i++) {
                    final String fileName = filters.getString(i);
                    final File file = new File(filtersDir, fileName);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                            File res = Glide.with(MainActivity.this)
                                    .load(Utils.getFilterImageUrl(fileName))
                                    .downloadOnly(1080, 1920).get();

                            FileOutputStream fos = new FileOutputStream(file);
                            FileInputStream fis = new FileInputStream(res);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = fis.read(buf)) > 0) {
                                fos.write(buf, 0, len);
                            }
                            fis.close();
                            fos.close();
                        } catch (IOException | ExecutionException ioe) {
                            ioe.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    };

    private Emitter.Listener blocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                try {
                    BlockedUsersSingleton
                            .getBlockedListSingletion()
                            .setBlockedList(object.getJSONArray("real"), object.getJSONArray("anonymous"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener statusColors = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                try {
                    SharedPreferences sharedPrefs = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
                    SharedPreferences.Editor prefs = sharedPrefs.edit();
                    JSONArray colors = object.getJSONArray("colors");
                    for(int i = 0;i<colors.length();i++){
                        JSONObject color = colors.getJSONObject(i);
                        int tColor = Integer.valueOf(color.getString("text"),16);
                        int bColor = Integer.valueOf(color.getString("background"), 16);
                        prefs.putInt("status_color_"+i+"_text", /*(tColor <= 0x01000000 ? */0xFF000000+ tColor/* + 0xFF000000 : tColor)*/);
                        prefs.putInt("status_color_"+i+"_bg", /*(bColor <= 0x01000000 ? */0xFF000000+ bColor /*+ 0xFF000000 : bColor)*/);
                    }
                    prefs.commit();

                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };


    private void showUpdateSnackbar(String text) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, text, CustomSnackbar.LENGTH_LONG);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "market://details?id=" + getApplicationContext().getPackageName();
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        sn.show();
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void addActivityFragment() {
        addFragmentToContainer(getFragment(FRAGMENT_INDEXES.ACTIVITY), UpdatesFragment.TAG);
    }
}

