package com.linute.linute.MainContent;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.FindFriends.FindFriendsFragment;
import com.linute.linute.MainContent.PeopleFragment.PeopleFragment;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.Settings.MainSettingsFragment;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    private AppBarLayout mAppBarLayout;
    private ActionBar mActionBar;
    private DrawerLayout mDrawerLayout;
    private MainDrawerListener mMainDrawerListener;
    private NavigationView mNavigationView;

    private FloatingActionButton fab;
    //public TaptUserProfileFragment mTaptUserProfileFragment;
    //public FeedDetailPage mFeedDetailPage;

    private CoordinatorLayout parentView;
    private FloatingActionsMenu fam;

    private UpdatableFragment[] mFragments; //holds our fragments
    private int mCurrentlyCheckedItem;

    public static final String PROFILE_OR_EVENT_NAME = "profileOrEvent";

    public static class FRAGMENT_INDEXES {
        public static final short PROFILE = 0;
        public static final short FEED = 1;
        public static final short PEOPLE = 2;
        public static final short ACTIVITY = 3;

        public static final short SETTINGS = 5;
    }


    private int mAppBarElevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarElevation = getResources().getDimensionPixelSize(R.dimen.main_app_bar_elevation);

        mFragments = new UpdatableFragment[4];

        parentView = (CoordinatorLayout) findViewById(R.id.coordinator);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainActivity_drawerLayout);
        mMainDrawerListener = new MainDrawerListener();
        mDrawerLayout.setDrawerListener(mMainDrawerListener);
        mNavigationView = (NavigationView) findViewById(R.id.mainActivity_navigation_view);

        //get toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.mainactivity_toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();


        //this arrow changes from navigation to back arrow
        final DrawerArrowDrawable arrowDrawable = new DrawerArrowDrawable(this);
        arrowDrawable.setColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setNavigationIcon(arrowDrawable);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                boolean drawer = getSupportFragmentManager().getBackStackEntryCount() == 0;
                ObjectAnimator.ofFloat(arrowDrawable, "progress", drawer ? 0 : 1).start();
            }
        });


        //only loads one fragment
        mFragments[FRAGMENT_INDEXES.FEED] = new DiscoverHolderFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivity_fragment_holder, mFragments[FRAGMENT_INDEXES.FEED])
                .commit();
        mCurrentlyCheckedItem = R.id.navigation_item_feed;


        //floating action button setup
        fam = (FloatingActionsMenu) findViewById(R.id.fabmenu);
        FloatingActionButton fabImage = (FloatingActionButton) findViewById(R.id.fabImage);
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.toggle();
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(i);
            }
        });
        FloatingActionButton fabText = (FloatingActionButton) findViewById(R.id.fabText);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.toggle();
                Intent i = new Intent(MainActivity.this, PostCreatePage.class);
                startActivity(i);
            }
        });


        //profile image and header setup
        loadDrawerHeader();

        //set click listener for header - taken to profile
        mNavigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();

                clearBackStack();

                if (mCurrentlyCheckedItem != FRAGMENT_INDEXES.PROFILE) { //profile doesn't get checked
                    mNavigationView.getMenu().findItem(mCurrentlyCheckedItem).setChecked(false);
                    mCurrentlyCheckedItem = FRAGMENT_INDEXES.PROFILE;
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
                        mCurrentlyCheckedItem = R.id.navigation_item_feed;
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.FEED));
                        item.setChecked(true);
                        break;
                    case R.id.navigation_item_activity:
                        mCurrentlyCheckedItem = R.id.navigation_item_activity;
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.ACTIVITY));
                        item.setChecked(true);
                        break;
                    case R.id.navigation_item_people:
                        mCurrentlyCheckedItem = R.id.navigation_item_people;
                        replaceContainerWithFragment(getFragment(FRAGMENT_INDEXES.PEOPLE));
                        item.setChecked(true);
                        break;
                    case R.id.navigation_item_settings:
                        mCurrentlyCheckedItem = R.id.navigation_item_settings;
                        replaceContainerWithFragment(new MainSettingsFragment());
                        item.setChecked(true);
                        break;
                    default:
                        break;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });
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
                    fragment = new PeopleFragment();
                    break;
                default:
                    fragment = null;
                    break;
            }

            mFragments[index] = fragment;
        }

        return mFragments[index];
    }

    public void replaceContainerWithFragment(final Fragment fragment) {
        //this will only run after drawer is fully closed
        //lags if we don't do this
        mMainDrawerListener.setChangeFragmentOrActivityAction(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainActivity_fragment_holder, fragment)
                        .commit();
            }
        });
    }


    //use this when you need to add another users profile view
    //or load image or status
    public void addFragmentToContainer(final Fragment fragment) {
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
    public void setFragmentOfIndexNeedsUpdating(boolean needsUpdating, int index){
        if (mFragments[index] != null){
            mFragments[index].setFragmentNeedUpdating(needsUpdating);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        //TODO: track which fragment we're in
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //TODO: save state
    }


    public void setTitle(String title) {
        mActionBar.setTitle(title);
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

        ((TextView) header.findViewById(R.id.drawerHeader_college_name)).setText(sharedPreferences.getString("collegeName", ""));

        Glide.with(this)
                .load(Utils.getImageUrlOfUser(sharedPreferences.getString("profileImage", "")))
                .asBitmap()
                .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000")))
                .override(size, size) //change image to the size we want
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into((CircularImageView) header.findViewById(R.id.navigation_header_profile_image));
    }

    public void showFAB(boolean show) {
        fam.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    //pops back toolbar back out
    public void resetToolbar() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        mAppBarLayout.setExpanded(true, true);
        behavior.onNestedFling(parentView, mAppBarLayout, null, 0, -1000, true);
    }

    //raise and lower appBar
    //we have to lower appBar in fragments that have tablayouts or there will be a shadow above the tabs
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void raiseAppBarLayoutElevation() {
        mAppBarLayout.setElevation(mAppBarElevation);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void lowerAppBarElevation() {
        mAppBarLayout.setElevation(0);
    }

    public void noInternet() {
        Snackbar.make(parentView, "Seems like you don't have internet, might wanna fix that...", Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getSupportFragmentManager().popBackStack();


                else mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        //if there is a profile view or feedDetailView
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();

        else if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    //@param type - 0 search by name ; 1 search facebook ; 2 search contacts
    public void startFindFriendsActivity(int type) {
        Intent i = new Intent(this, FindFriendsFragment.class);
        i.putExtra(FindFriendsFragment.SEARCH_TYPE_KEY, type);
        startActivity(i);
    }


    public void toggleFam() {
        if (fam.isExpanded())
            fam.toggle();
    }

    //the items in navigation view
    //this sets the number to the right of it
    public void setNavItemNotification(@IdRes int itemId, int count) {
        TextView view = (TextView) mNavigationView.getMenu().findItem(itemId).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);
    }


    //So we change fragments or activities only after the drawer closes
    private class MainDrawerListener extends DrawerLayout.SimpleDrawerListener{

        private Runnable mChangeFragmentOrActivityAction;

        @Override
        public void onDrawerClosed(View drawerView) {
            if (mChangeFragmentOrActivityAction != null) {
                mChangeFragmentOrActivityAction.run();
                mChangeFragmentOrActivityAction = null;
            }
        }

        public void setChangeFragmentOrActivityAction(Runnable action){
            mChangeFragmentOrActivityAction = action;
        }

    }


}
