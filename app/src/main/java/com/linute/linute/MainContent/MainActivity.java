package com.linute.linute.MainContent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.MainContent.Chat.NewChatEvent;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverHolderFragment;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.PeopleFragment.PeopleFragmentsHolder;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.MainContent.UpdateFragment.Update;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class MainActivity extends BaseTaptActivity {

    public static String TAG = "MainActivity";

    public static final int PHOTO_STATUS_POSTED = 19;

    //private AppBarLayout mAppBarLayout;
    //private ActionBar mActionBar;
    //private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private MainDrawerListener mMainDrawerListener;
    private NavigationView mNavigationView;

    //private CoordinatorLayout parentView;
    //private FloatingActionsMenu fam;
    private UpdatableFragment[] mFragments; //holds our fragments

    public static final String PROFILE_OR_EVENT_NAME = "profileOrEvent";
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting;

    private SocketErrorResponse mSocketErrorResponse;

    private boolean mHasMessage = false;

    public static class FRAGMENT_INDEXES {
        public static final short PROFILE = 0;
        public static final short FEED = 1;
        public static final short PEOPLE = 2;
        public static final short ACTIVITY = 3;
        public static final short SETTINGS = 5;
    }


    //private int mAppBarElevation;
    private MenuItem mPreviousItem;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        //mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
       // mAppBarElevation = getResources().getDimensionPixelSize(R.dimen.main_app_bar_elevation);

        mFragments = new UpdatableFragment[4];

        //parentView = (CoordinatorLayout) findViewById(R.id.coordinator);

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

                clearBackStack();

                if (mPreviousItem != null) { //profile doesn't get checked
                    mPreviousItem.setChecked(false);
                    mPreviousItem = null;
                }

                replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PROFILE));
            }
        });

        //setNavigationView action
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                //if there are a lot of other user profile/ events in mainActivity, clear them
                clearBackStack();

                switch (item.getItemId()) {
                    case R.id.navigation_item_feed:
                        if (mPreviousItem != null && mPreviousItem != item) {
                            mPreviousItem.setChecked(false);
                        }
                        item.setChecked(true);
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.FEED));
                        mPreviousItem = item;
                        break;
                    case R.id.navigation_item_activity:
                        if (mPreviousItem != null && mPreviousItem != item) {
                            mPreviousItem.setChecked(false);
                        }
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.ACTIVITY));
                        item.setChecked(true);
                        mPreviousItem = item;
                        break;
                    case R.id.navigation_item_people:
                        if (mPreviousItem != null && mPreviousItem != item) {
                            mPreviousItem.setChecked(false);
                        }
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PEOPLE));
                        item.setChecked(true);
                        mPreviousItem = item;
                        break;
                    case R.id.navigation_item_settings:
                        startActivityForResults(SettingActivity.class, SETTINGS_REQUEST_CODE);
                        break;
//                    case R.id.navigation_item_find_friends:
//                        startNewActivity(FindFriendsActivity.class);
//                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

//        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu);
        clearBackStack();
        //regular start
        if (savedInstanceState == null) {
            //only loads one fragment
            mFragments[FRAGMENT_INDEXES.FEED] = new DiscoverHolderFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainActivity_fragment_holder, mFragments[FRAGMENT_INDEXES.FEED])
                    .commit();
            mPreviousItem = mNavigationView.getMenu().findItem(R.id.navigation_item_feed);
            mPreviousItem.setChecked(true);
        }

        Intent intent = getIntent();
        if (intent != null){
            onNewIntent(intent);
        }
    }


    private Fragment getFragment(short index) {
        if (mFragments[index] == null) { //if fragment haven't been created yet, create it
            UpdatableFragment fragment;

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
                case FRAGMENT_INDEXES.PEOPLE:
                    fragment = new PeopleFragmentsHolder();
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
        mMainDrawerListener.setChangeFragmentOrActivityAction(new Runnable() {
            @Override
            public void run() {
                if (mSafeForFragmentTransaction) {
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainActivity_fragment_holder, fragment)
                            .commit();
                }
            }
        });
    }

    public static final int SETTINGS_REQUEST_CODE = 13;

    public void startActivityForResults(final Class activity, final int requestCode) {
        mMainDrawerListener.setChangeFragmentOrActivityAction(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, activity);
                startActivityForResult(i, requestCode);
            }
        });
    }

    public void startEditProfileActivity(Class activity){
        Intent i = new Intent(MainActivity.this, activity);
        startActivityForResult(i, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) { //came back from settings
            setFragmentOfIndexNeedsUpdating(true, FRAGMENT_INDEXES.PROFILE);
            loadDrawerHeader(); //reload drawer header
        } else if (requestCode == PHOTO_STATUS_POSTED && resultCode == RESULT_OK) { //posted new pic or status
            setFragmentOfIndexNeedsUpdating(true, FRAGMENT_INDEXES.FEED);
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

    public void clearBackStack() {
        //if there are a lot of other user profile/ events in mainActivity, clear them
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate(PROFILE_OR_EVENT_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    //sets needsUpdating for fragment at index
    @Override
    public void setFragmentOfIndexNeedsUpdating(boolean needsUpdating, int index) {
        if (mFragments[index] != null) {
            mFragments[index].setFragmentNeedUpdating(needsUpdating);
        }
    }

//    @Override
//    public void setTitle(String title) {
//        mToolbar.setTitle(title);
//    }

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
                .asBitmap()
                .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000")))
                .override(size, size) //change image to the size we want
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into((CircleImageView) header.findViewById(R.id.navigation_header_profile_image));
    }

    //public void showFAB(boolean show) {
        //fam.setVisibility(show ? View.VISIBLE : View.GONE);
    //}

    //pops back toolbar back out
//    @Override
//    public void resetToolbar() {
////        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
////        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
////
////        if (behavior == null) return;
//
//        mAppBarLayout.setExpanded(true, true);
////        behavior.onNestedFling(parentView, mAppBarLayout, null, 0, -1000, true);
//    }

    //raise and lower appBar
    //we have to lower appBar in fragments that have tablayouts or there will be a shadow above the tabs
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void raiseAppBarLayoutElevation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            mAppBarLayout.setElevation(mAppBarElevation);
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void lowerAppBarElevation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            mAppBarLayout.setElevation(0);
//    }

    public void noInternet() {
        CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, "Could not find internet connection", CustomSnackbar.LENGTH_LONG);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        sn.getView().setAlpha(0.8f);

        sn.show();
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }


//    public void toggleFam() {
//        if (fam.isExpanded())
//            fam.toggle();
//    }

    //the items in navigation view
    //this sets the number to the right of it
    public void setNavItemNotification(@IdRes int itemId, int count, int oldCount) {

        View main = mNavigationView.getMenu().findItem(itemId).getActionView();

        if (count > 0) {
            ((TextView) main.findViewById(R.id.nav_item_notification_counter)).setText(count > 99 ? "+" : String.valueOf(count));
            (main.findViewById(R.id.nav_item_notification_background)).setVisibility(View.VISIBLE);
        } else {
            if (oldCount != 0) {
                ((TextView) main.findViewById(R.id.nav_item_notification_counter)).setText("");
                (main.findViewById(R.id.nav_item_notification_background)).setVisibility(View.GONE);
            }
        }
    }

    public void setFeedNotification(int count) {
        setNavItemNotification(R.id.navigation_item_feed, count, mNumNewPostsInDiscover);
    }

    public void setUpdateNotification(int count) {
        setNavItemNotification(R.id.navigation_item_activity, count, mNumNewActivities);
    }

    public void setNumNewPostsInDiscover(int count) {
        mNumNewPostsInDiscover = count;
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
                                    "&version=" + device.getVersonName() +
                                    "&build=" + device.getVersionCode() +
                                    "&os=" + device.getOS() +
                                    "&type=" + device.getType() +
                                    "&api=" + API_Methods.VERSION +
                                    "&model=" + device.getModel();

                    op.reconnectionDelay = 5;
                    op.secure = true;
                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(getString(R.string.SOCKET_URL), op);/*R.string.DEV_SOCKET_URL*/

                    mSocket.on("activity", newActivity);
                    mSocket.on("new post", newPostListener);
                    mSocket.on("unread", haveUnread);
                    mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                    mSocket.on(Socket.EVENT_ERROR, onEventError);
                    mSocket.connect();
                    mConnecting = false;

                    JSONObject object = new JSONObject();
                    emitSocket(API_Methods.VERSION + ":messages:unread", object);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        mSafeForFragmentTransaction = false;

        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("activity", newActivity);
            mSocket.off("new post", newPostListener);
            mSocket.on("unread", haveUnread);

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


    private int mNumNewPostsInDiscover = 0; //new posts in discover fragment
    private int mNumNewActivities = 0;


    public void setNumNewActivities(int num) {
        mNumNewActivities = num;
    }

    private Emitter.Listener newPostListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mFragments[FRAGMENT_INDEXES.FEED] != null) {
                final Object post = args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (((DiscoverHolderFragment) mFragments[FRAGMENT_INDEXES.FEED])
                                .addPostToFeed(post)) {
                            setFeedNotification(++mNumNewPostsInDiscover);
                        }
                    }
                });

            }
        }
    };

    private Emitter.Listener newActivity = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject activity = new JSONObject(args[0].toString());
                //// TODO: 4/19/16  check action

                final Update update = new Update(activity);
                if (update.getUpdateType() != Update.UpdateType.UNDEFINED) {
                    if (update.hasEventInformation()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mFragments[FRAGMENT_INDEXES.ACTIVITY] != null) {
                                    ((UpdatesFragment) mFragments[FRAGMENT_INDEXES.ACTIVITY]).addItemToRecents(update);
                                }
                                newEventSnackbar(update.getDescription(), update.getPost());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mFragments[FRAGMENT_INDEXES.ACTIVITY] != null) {
                                    ((UpdatesFragment) mFragments[FRAGMENT_INDEXES.ACTIVITY]).addItemToRecents(update);
                                }
                                newProfileSnackBar(update);
                            }
                        });
                    }
                    setUpdateNotification(++mNumNewActivities);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private void newEventSnackbar(String text, final Post post) {
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, text, CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.notification_color));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentToContainer(FeedDetailPage.newInstance(post));
                sn.dismiss();
            }
        });
        sn.show();
    }

    private void newProfileSnackBar(final Update update){
        final CustomSnackbar sn = CustomSnackbar.make(mDrawerLayout, update.getDescription(), CustomSnackbar.LENGTH_SHORT);
        sn.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.notification_color));
        sn.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        int type = intent.getIntExtra("NOTIFICATION", LinuteConstants.MISC);
        if (type == LinuteConstants.FEED_DETAIL) {
            String id = intent.getStringExtra("event");
            if (id != null) {
                mSafeForFragmentTransaction = true;
                addFragmentToContainer(FeedDetailPage.newInstance(
                        new Post("", id, null, "")
                ));
            }
        }else if (type == LinuteConstants.PROFILE){
            String id = intent.getStringExtra("user");
            if (id != null){
                mSafeForFragmentTransaction = true;
                addFragmentToContainer(TaptUserProfileFragment.newInstance("", id));
            }
        }
    }

    private Emitter.Listener haveUnread = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHasMessage = (boolean) args[0];
            EventBus.getDefault().post(new NewChatEvent(mHasMessage));
        }
    };

    public  boolean hasMessage(){
        return mHasMessage;
    }

    public void openDrawer(){
        mDrawerLayout.openDrawer(GravityCompat.START);
    }
}

