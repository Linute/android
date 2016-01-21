package com.linute.linute.MainContent;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverFragment;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.FindFriends.FindFriendsActivity;
import com.linute.linute.MainContent.PeopleFragment.PeopleFragment;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.SlidingTab.SlidingTabLayout;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.security.GuardedObject;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    private AppBarLayout mAppBarLayout;
    private ActionBar mActionBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    public ViewPager mViewPager;
    private FloatingActionButton fab;
    private PostCreatePage mPostCreatePage;
    //public TaptUserProfileFragment mTaptUserProfileFragment;
    //public FeedDetailPage mFeedDetailPage;

    private CoordinatorLayout parentView;
    private FloatingActionsMenu fam;

    private Fragment[] mFragments; //holds our fragments
    public static final int PROFILE_FRAGMENT_INDEX = 0;

    public static class FRAGMENT_INDEXES {
        public static final int PROFILE = 0;
        public static final int FEED = 1;
        public static final int PEOPLE = 2;
        public static final int ACTIVITY = 3;
    }


    private int mAppBarElevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mAppBarElevation = getResources().getDimensionPixelSize(R.dimen.main_app_bar_elevation);

        mFragments = new Fragment[4];

        parentView = (CoordinatorLayout) findViewById(R.id.coordinator);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainActivity_drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.mainActivity_navigation_view);

        //get toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.mainactivity_toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }


        //only loads one feed for now
        mFragments[FRAGMENT_INDEXES.FEED] = new DiscoverHolderFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivity_fragment_holder, mFragments[FRAGMENT_INDEXES.FEED])
                .commit();


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
                newPost();
            }
        });


        //profile image and header setup
        loadProfileImage();

        mNavigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: link to profile fragment
                Log.i(TAG, "onClick: connect to profile");
                Toast.makeText(MainActivity.this, "Will link to Profile", Toast.LENGTH_LONG).show();

            }
        });

        //setNavigationView action
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                Toast.makeText(MainActivity.this, "Will link to " + item.getTitle(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        //Keep track of which tab currently on
        outState.putInt(getString(R.string.current_tab), mViewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Restore tab user was on
        mViewPager.setCurrentItem(savedInstanceState.getInt(getString(R.string.current_tab)));
    }

    public void setTitle(String title) {
        mActionBar.setTitle(title);
    }

    private void loadProfileImage(){
        //profile image on the left
        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        int size = getResources().getDimensionPixelSize(R.dimen.nav_header_profile_side);

        Glide.with(this)
                .load(Utils.getImageUrlOfUser(sharedPreferences.getString("profileImage", "")))
                .asBitmap()
                .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000")))
                .override(size, size) //change image to the size we want
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into((CircularImageView)mNavigationView.getHeaderView(0).findViewById(R.id.navigation_header_profile_image));
    }

    public void showFAB(boolean show) {
        fam.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void newPost() {
        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
        mPostCreatePage = PostCreatePage.newInstance(mViewPager.getCurrentItem());
//        mPostCreatePage.show(ft, "dialog");
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(R.id.postContainer, mPostCreatePage)
                .addToBackStack(null).commit();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (R.id.mainactivity_action_settings == id){
            Intent i = new Intent(MainActivity.this, LinuteSettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //NOTE: uncomment if using post as fragment
//        if (mPostCreatePage != null && mPostCreatePage.isVisible()) {
//            mPostCreatePage.onBackPressed();
//        }

        //if there is a profile view or feedDetailView
       if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }

//        else if (mTaptUserProfileFragment != null && mTaptUserProfileFragment.isVisible()) {
//            mTaptUserProfileFragment.dismiss();
//        } else if (mFeedDetailPage != null && mFeedDetailPage.isVisible()) {
//            mFeedDetailPage.dismiss();
//        }

        else if(mDrawerLayout.isDrawerOpen(mNavigationView)){
           mDrawerLayout.closeDrawers();
        }
        else {
            super.onBackPressed();
        }
    }

    public Fragment[] getFragments() {
        return mFragments;
    }

    //@param type - 0 search by name ; 1 search facebook ; 2 search contacts
    public void startFindFriendsActivity(int type) {
        Intent i = new Intent(this, FindFriendsActivity.class);
        i.putExtra(FindFriendsActivity.SEARCH_TYPE_KEY, type);
        startActivity(i);
    }

    public void toggleFam() {
        if (fam.isExpanded())
            fam.toggle();
    }

    //turn on notification
    private void setNavItemNotification(@IdRes int itemId, int count) {
        TextView view = (TextView) mNavigationView.getMenu().findItem(itemId).getActionView();

        //show notification
        if (count > 0){
            view.setText(count);
            if (view.getVisibility() == View.GONE){
                view.setVisibility(View.VISIBLE);
            }
        }
        //hide notification if less 0 or less
        else if (view.getVisibility() == View.VISIBLE){
            view.setVisibility(View.GONE);
        }
    }
}
