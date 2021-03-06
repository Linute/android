package com.linute.linute.MainContent;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
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
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.Chat.ChatRoom;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.Chat.User;
import com.linute.linute.MainContent.DiscoverFragment.BlockedUsersSingleton;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverHolderFragment;
import com.linute.linute.MainContent.DiscoverFragment.PollsSingleton;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.Global.GlobalFragment;
import com.linute.linute.MainContent.ProfileFragment.EmptyProfileHolder;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.MainContent.UpdateFragment.Update;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.MainContent.Uploading.UploadIntentService;
import com.linute.linute.ModesDisabled;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;
import com.linute.linute.UtilsAndHelpers.FiveStarRater.FiveStarsDialog;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.NetworkUtil;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends BaseTaptActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
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

    private boolean mShowSnackbar = true; //show snackbar for new activity

    public static class FRAGMENT_INDEXES {
        public static final short PROFILE = 0;
        public static final short FEED = 1;
        public static final short GLOBAL = 2;
        public static final short FIND_FRIENDS = 3;
        public static final short ACTIVITY = 4;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        API_Methods.USER_ID = mSharedPreferences.getString("userID", null);

        TaptSocket.initSocketConnection(this);
        PollsSingleton.init();

        EmptyProfileHolder.activity = this;

        mFragments = new BaseFragment[5];
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
                if (mPreviousItem != null) { //profile doesn't get checked
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) clearBackStack();
                    mPreviousItem.setChecked(false);
                    mPreviousItem = null;
                    replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PROFILE));
                } else {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                        clearBackStack();
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PROFILE));
                    } else {
                        getFragment(FRAGMENT_INDEXES.PROFILE).resetFragment();
                    }
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


        mNavigationView.findViewById(R.id.item_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(i, SETTINGS_REQUEST_CODE);

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
                .setRateText("How are we doing?")
                .setRateText("Wasup! We see you come here often, how are you liking it so far?")
                .setUpperBound(4)
                .showAfter(10);

        new Thread(new Runnable() {
            @Override
            public void run() {
                preloadFragments();
            }
        }).start();
    }

    private void preloadFragments(){
        GlobalFragment global = GlobalFragment.getInstance();
        RoomsActivityFragment rooms = RoomsActivityFragment.getInstance();
        getSupportFragmentManager().beginTransaction().attach(global).attach(rooms).commit();
    }


    public static final int LOCATION_PERM = 82;


    public void getPermission() {

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERM);

        }/*else {
            *//*new AlertDialog.Builder(this)
                    .setTitle("Need location permission")
                    .setMessage("Tapt needs access to your location for geo filters")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERM);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                     }}).show();*//*
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERM){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                sendLocation();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void sendLocation(){
        try {
            JSONObject loca = new JSONObject();
            Location lastLoca = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(lastLoca != null) {
                loca.put("longitude", lastLoca.getLongitude());
                loca.put("latitude", lastLoca.getLatitude());
                TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:geo", loca);
            }
        }catch (SecurityException|JSONException e){
            e.printStackTrace();
        }
    }


    private void sendNetworkInformation(){
        try{
            JSONObject object = new JSONObject();
            object.put("carrier", NetworkUtil.getProvider(this));
            object.put("speed", NetworkUtil.getNetworkClass(this));
            TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:network", object);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void selectDrawerItem(int pos) {
        navItemSelected((short) pos, mNavigationView.getMenu().getItem(pos - 1));
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


    public BaseFragment getFragment(short index) {
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
                    fragment = GlobalFragment.getInstance();
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
            mMainDrawerListener.setOnDrawerClosedRunnable(new Runnable() {
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
                        //.setCustomAnimations(R.anim.frag_fade_in, R.anim.hold)
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

        String college = sharedPreferences.getString("collegeName", "");
        ((TextView) header.findViewById(R.id.drawerHeader_name)).setText(name);
        ((TextView) header.findViewById(R.id.drawerHeader_college)).setText(college);
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
            if (!mSafeForFragmentTransaction) return;
            if (mPreviousItem == null || mPreviousItem.getItemId() != R.id.navigation_item_feed) {
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

        public void setOnDrawerClosedRunnable(Runnable action) {
            mChangeFragmentOrActivityAction = action;
        }
    }


    private boolean mSafeForFragmentTransaction = true;

    @Override
    protected void onStart() {
        super.onStart();

        //Log.i(TAG, "onStart: ");

        TaptSocket socket = TaptSocket.getInstance();
        socket.on("activity", newActivity);
        socket.on("new post", newPostListener);
        socket.on("user banned", userBanned);
        socket.on("update", update);
        socket.on("badge", badge);
        socket.on("unread", haveUnread);
        socket.on("posts refresh", refresh);
        socket.on("memes", meme);
        socket.on("filters", filter);
        socket.on("status colors", statusColors);
        socket.on("blocked", blocked);
        socket.on("message", message);
        socket.on("alert", alert);
        socket.on("modes disabled", modesDisabled);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on(Socket.EVENT_ERROR, onEventError);
        socket.on(Socket.EVENT_RECONNECT, onReconnect);
        socket.connectSocket();

        //get friends list
        try {
            JSONObject object = new JSONObject();
            object.put("timestamp", mSharedPreferences.getLong("timestamp", 0));
            socket.emit(API_Methods.VERSION + ":users:sync contacts", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendNetworkInformation();

        //try to send location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            sendLocation();
        }else {
            getPermission();
        }
    }

    /******
     * Socket stuff
     ********/


    @Override
    protected void onResume() {
        super.onResume();
        mSafeForFragmentTransaction = true;
    }


    @Override
    protected void onPause() {
        super.onPause();

        mWatchForRefresh = true;
        mSafeForFragmentTransaction = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        TaptSocket socket = TaptSocket.getInstance();
        if (socket != null) {
            socket.disconnectSocket();
            socket.off("activity", newActivity);
            socket.off("new post", newPostListener);
            socket.off("user banned", userBanned);
            socket.off("update", update);
            socket.off("badge", badge);
            socket.off("unread", haveUnread);
            socket.off("posts refresh", refresh);
            socket.off("memes", meme);
            socket.off("filters", filter);
            socket.off("status colors", statusColors);
            socket.off("blocked", blocked);
            socket.off("message", message);
            socket.off("alert", alert);
            socket.off("modes disabled", modesDisabled);
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onSocketTimeOut);
            socket.off(Socket.EVENT_ERROR, onEventError);
            socket.off(Socket.EVENT_RECONNECT, onReconnect);
        }

        //Log.i(TAG, "onStop: ");
    }


    @Override
    public void setSocketErrorResponse(SocketErrorResponse error) {
        mSocketErrorResponse = error;
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "call: failed socket connection");
        }
    };

    private Emitter.Listener onEventError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "ERROR: " + args[0]);

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
                    final JSONObject arg = (JSONObject) args[0];
                    final Post postObj = new Post(arg);
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

                                DiscoverHolderFragment fragment = (DiscoverHolderFragment) mFragments[FRAGMENT_INDEXES.FEED];
                                if (fragment != null && !fragment.addPostToFeed(postObj)) {
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

    private String getStringFromJSON(JSONObject obj, String key) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    private Emitter.Listener newActivity = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject activity = new JSONObject(args[0].toString());
                //Log.d(TAG, "call: " + activity.toString(4));
                //message
                final NotificationsCounterSingleton notifCounter = NotificationsCounterSingleton.getInstance();
                if (activity.getString("action").equals("messager")) {
                    /*
                    *
                    * room: room.id,
                   roomType: room.type,
                   roomUsers: room.users,
                   roomProfileImage: room.profileImage,
                   roomNameOfGroup: room.name,
                   messageType: message.type, (edited)
                    *
                    * */

                    JSONArray users = activity.getJSONArray("roomUsers");
                    ArrayList<User> usersList = new ArrayList<>(users.length());
                    //Log.d("AAA", users.toString(4));
                    for (int u = 0; u < users.length(); u++) {
                        JSONObject userJson = users.getJSONObject(u);
                        usersList.add(new User(
                                userJson.getString("id"),
                                userJson.getString("firstName"),
                                "",
                                ""
                        ));
                    }

                    final ChatRoom chat = new ChatRoom(
                            activity.getString("room"),
                            activity.getInt("roomType"),
                            getStringFromJSON(activity, "roomName"),
                            activity.getJSONObject("roomProfileImage").getString("original"),
                            usersList,
                            "",
                            true,
                            0,
                            false,
                            0
                    );

                    final String message = activity.getString("text");

                    if (mShowSnackbar) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newMessageSnackbar(chat, message);
                            }
                        });
                    }


                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":badge");
                    final NewMessageEvent chatEvent = new NewMessageEvent(true);
                    chatEvent.setRoomId(chat.roomId);Utils.stripUnsupportedCharacters(activity.getString("messageText"));
                    chatEvent.setMessage(Utils.stripUnsupportedCharacters(activity.getString("messageText")));
                    NewMessageBus.getInstance().setNewMessage(chatEvent);

                } else {
                    final Update update = new Update(activity);
                    if (update.getUpdateType() != Update.UpdateType.UNDEFINED) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifCounter.incrementActivities();

                                if (mFragments[FRAGMENT_INDEXES.ACTIVITY] != null)
                                    ((UpdatesFragment) mFragments[FRAGMENT_INDEXES.ACTIVITY]).addItemToRecents(update);

                                NotificationEventBus
                                        .getInstance()
                                        .setNotification(new NotificationEvent(NotificationEvent.ACTIVITY, true));

                                if (update.hasEventInformation()) {
                                    //String text = (update.isAnon() ? "Anon " : update.getUserFullName() + " ") + update.getDescription();
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

    private void newMessageSnackbar(final ChatRoom chat, String message) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, message, CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryColor));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: NOT TESTED - Drawer might lag while closing if we add a fragment before it is fully closed
                // to fix : use mMainDrawerListener.setOnDrawerClosedRunnable();
                if (mDrawerLayout.isDrawerOpen(mNavigationView))
                    mDrawerLayout.closeDrawers();

                addFragmentToContainer(ChatFragment.newInstance(chat));
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

                //TODO: NOT TESTED - Drawer might lag while closing if we add a fragment before it is fully closed
                // to fix : use mMainDrawerListener.setOnDrawerClosedRunnable();
                if (mDrawerLayout.isDrawerOpen(mNavigationView))
                    mDrawerLayout.closeDrawers();


                if (getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
                    addActivityFragment();

                addFragmentToContainer(FeedDetailPage.newInstance(post));
                sn.dismiss();
            }
        });
        sn.show();
    }

    private void newProfileSnackBar(final Update update) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, update.getUserFullName() + " " + update.getDescription(), CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryColor));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: NOT TESTED - Drawer might lag while closing if we add a fragment before it is fully closed
                // to fix : use mMainDrawerListener.setOnDrawerClosedRunnable();
                if (mDrawerLayout.isDrawerOpen(mNavigationView))
                    mDrawerLayout.closeDrawers();


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
                boolean showUpdateFrag = intent.getBooleanExtra("show_update", true);
                if (showUpdateFrag && getSupportFragmentManager().findFragmentByTag(UpdatesFragment.TAG) == null)
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
            /*String room = intent.getStringExtra("room");
            String userId = intent.getStringExtra("ownerID");
            String firstName = intent.getStringExtra("ownerFistName");
            String lastName = intent.getStringExtra("ownerLastName");*/
            mSafeForFragmentTransaction = true;

            ChatRoom room = intent.getParcelableExtra("chatRoom");

            //boolean empty = room.roomId == null || room.roomId.isEmpty();
            if (getSupportFragmentManager().findFragmentByTag(RoomsActivityFragment.TAG) == null)
                addFragmentToContainer(RoomsActivityFragment.getInstance(), RoomsActivityFragment.TAG);
            addFragmentToContainer(ChatFragment.newInstance(room));
            /*addFragmentToContainer(ChatFragment.newInstance(
                    empty ? null : room,
                    empty ? firstName : "",
                    empty ? lastName : "",
                    userId.isEmpty() ? null : userId));*/
        }
    }

    private Emitter.Listener userBanned = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject body = new JSONObject(args[0].toString());
                //Log.i(TAG, "call: " + body.toString(4));
                final String text = body.getString("text");

                TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:logout", new JSONObject());
                Utils.resetUserInformation(getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE));
                Utils.deleteTempSharedPreference(getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE));
                if (AccessToken.getCurrentAccessToken() != null) //log out facebook if logged in
                    LoginManager.getInstance().logOut();

                TaptSocket.getInstance().forceDisconnect();
                TaptSocket.clear();

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
                //Log.i(TAG, "call: "+badge.toString(4));
                int messagesCount = badge.getInt("messages");
                NotificationsCounterSingleton.getInstance().setNumMessages(messagesCount);
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
                    final JSONObject obj = new JSONObject(args[0].toString());
                    if (posts > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFeedNotification(NotificationsCounterSingleton.getInstance().setNumOfNewPosts(posts));
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
            NotificationsCounterSingleton notCounter = NotificationsCounterSingleton.getInstance();
            notCounter.setNumMessages((boolean) args[0] ? 1 : 0);
            NewMessageBus.getInstance().setNewMessage(new NewMessageEvent(notCounter.hasMessage()));
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


                if (memes == null) return;

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
                        } catch (IOException | InterruptedException | ExecutionException ioe) {
                            ioe.printStackTrace();
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

                if (filters == null) return;

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

    private Emitter.Listener statusColors = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                try {
                    SharedPreferences sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    SharedPreferences.Editor prefs = sharedPrefs.edit();
                    JSONArray colors = object.getJSONArray("colors");

                    if (colors == null) return;

                    for (int i = 0; i < colors.length(); i++) {
                        JSONObject color = colors.getJSONObject(i);
                        int tColor = Integer.valueOf(color.getString("text"), 16);
                        int bColor = Integer.valueOf(color.getString("background"), 16);
                        prefs.putInt("status_color_" + i + "_text", /*(tColor <= 0x01000000 ? */0xFF000000 + tColor/* + 0xFF000000 : tColor)*/);
                        prefs.putInt("status_color_" + i + "_bg", /*(bColor <= 0x01000000 ? */0xFF000000 + bColor /*+ 0xFF000000 : bColor)*/);
                    }
                    prefs.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mSocketListener != null) {
                mSocketListener.onReconnect();
            }
        }
    };

    //when max sends message (blue dropdown)
    private Emitter.Listener message = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                //Log.i(TAG, "call: "+args[0].toString());
                JSONObject message = new JSONObject(args[0].toString());
                final String text = message.getString("text");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomSnackbar.make(mDrawerLayout, text, CustomSnackbar.LENGTH_LONG)
                                .setBackgroundColor(R.color.secondaryColor)
                                .show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //when max sends alert (dialog popup)
    private Emitter.Listener alert = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                //Log.i(TAG, "call: "+args[0].toString());
                JSONObject alert = new JSONObject(args[0].toString());
                final String title = alert.getString("title");
                final String text = alert.getString("text");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(title)
                                .setMessage(text)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener modesDisabled = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject object = new JSONObject(args[0].toString()).getJSONObject("modesDisabled");
                //Log.d(TAG, "call: "+object.toString(4));
                JSONObject temp;
                ModesDisabled modesDisabled = ModesDisabled.getInstance();

                temp = object.getJSONObject("real");
                modesDisabled.setRealComments(temp.getBoolean("comments"));
                modesDisabled.setRealPosts(temp.getBoolean("posts"));

                temp = object.getJSONObject("anonymous");
                modesDisabled.setAnonComments(temp.getBoolean("comments"));
                modesDisabled.setAnonPosts(temp.getBoolean("posts"));


                //test
                //modesDisabled.setAnonPosts(true);
                //modesDisabled.setRealPosts(true);
                //modesDisabled.setRealComments(true);
                //modesDisabled.setAnonComments(true);

            }catch (JSONException e){
                e.printStackTrace();
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

    public void setShowSnackbar(boolean show) {
        mShowSnackbar = show;
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void lockDrawer(int lock) {
        mDrawerLayout.setDrawerLockMode(lock);
    }


    public void addActivityFragment() {
        addFragmentToContainer(getFragment(FRAGMENT_INDEXES.ACTIVITY), UpdatesFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}