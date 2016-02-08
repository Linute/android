package com.linute.linute.MainContent.FindFriends;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by QiFeng on 1/25/16.
 */
public class FindFriendsActivity extends BaseTaptActivity {

    private ActionBar mActionBar;
    private CoordinatorLayout parentView;
    private AppBarLayout mAppBarLayout;
    private int mAppBarElevation;
    private Toolbar mToolbar;


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
}
