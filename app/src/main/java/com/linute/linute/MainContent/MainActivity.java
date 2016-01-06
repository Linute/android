package com.linute.linute.MainContent;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.SlidingTab.SlidingTabLayout;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TextView mTitle;
    private FloatingActionButton fab;
    private PostContentPage newFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get toolbar
        mToolbar = (Toolbar) findViewById(R.id.mainactivity_toolbar);
        setSupportActionBar(mToolbar);
        mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) findViewById(R.id.mainactivity_viewpager);
        mViewPager.setAdapter(new LinuteFragmentAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.mainactivity_tabbar);

        tabLayout.setSelectedIndicatorColors(R.color.tabsScrollColor);

        tabLayout.setViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                //newPost();
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(i);
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
        mTitle.setText(title);
    }

    public void newPost() {
        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
        newFragment = PostContentPage.newInstance(mViewPager.getCurrentItem());
//        newFragment.show(ft, "dialog");
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(R.id.postContainer, newFragment)
                .addToBackStack(null).commit();
    }

    public void resetToolbar() {
//        CoordinatorLayout coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
//        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        appbar.setExpanded(true, true);
//        behavior.onNestedFling(coordinator, appbar, null, 0, -1000, true);
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
    public void onBackPressed() {
        if (newFragment.isVisible()) {
            newFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
