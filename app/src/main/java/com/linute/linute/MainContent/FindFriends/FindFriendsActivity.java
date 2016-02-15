package com.linute.linute.MainContent.FindFriends;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 1/25/16.
 */
public class FindFriendsActivity extends BaseTaptActivity {

    private static final String TAG = FindFriendsActivity.class.getSimpleName();
    private ActionBar mActionBar;
    private CoordinatorLayout parentView;
    private AppBarLayout mAppBarLayout;
    private int mAppBarElevation;
    private Toolbar mToolbar;
    private Socket mSocket;
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarElevation = getResources().getDimensionPixelSize(R.dimen.main_app_bar_elevation);

        parentView = (CoordinatorLayout) findViewById(R.id.coordinator);

        mToolbar = (Toolbar) findViewById(R.id.findFriends_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);

        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.findFriends_fragment_holder, new FindFriendsChoiceFragment())
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {

        //if there is a profile view or feedDetailView
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();

        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //raise and lower appBar
    //we have to lower appBar in fragments that have tablayouts or there will be a shadow above the tabs
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void raiseAppBarLayoutElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mAppBarLayout.setElevation(mAppBarElevation);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void lowerAppBarElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mAppBarLayout.setElevation(0);
    }

    @Override
    public void replaceContainerWithFragment(final Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.findFriends_fragment_holder, fragment)
                .commit();
    }


    public static final String PROFILE_OR_EVENT_NAME = "Prof_or_event";

    @Override
    public void addFragmentToContainer(final Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.findFriends_fragment_holder, fragment)
                .addToBackStack(PROFILE_OR_EVENT_NAME)
                .commit();
    }


    @Override
    public void setTitle(String title) {
        mActionBar.setTitle(title);
    }


    @Override
    public void resetToolbar() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior == null) return;

        mAppBarLayout.setExpanded(true, true);
        behavior.onNestedFling(parentView, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void enableBarScrolling(boolean enabled) {

        if (enabled) {
            ((CoordinatorLayout.LayoutParams) findViewById(R.id.findFriends_fragment_holder)
                    .getLayoutParams())
                    .setBehavior(new AppBarLayout.ScrollingViewBehavior());
            ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams())
                    .setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        } else {
            ((CoordinatorLayout.LayoutParams) findViewById(R.id.findFriends_fragment_holder)
                    .getLayoutParams())
                    .setBehavior(null);
            ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams())
                    .setScrollFlags(0);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;
            {
                try {
                    mSocket = IO.socket(getString(R.string.SOCKET_URL));//R.string.DEV_SOCKET_URL
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            if (mSocket == null) return;

            mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on("authorization", authorization);
            mSocket.connect();
            mConnecting = false;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", mSharedPreferences.getString("userID", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("authorization", jsonObject);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSocket == null) return;

        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("authorization", authorization);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: failed socket connection");
        }
    };

    private Emitter.Listener authorization = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "runAuthorization: " + ((JSONObject) args[0]).toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    public void setToolbarOnClickListener(View.OnClickListener listener){
        mToolbar.setOnClickListener(listener);
    }
}
