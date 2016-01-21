package com.linute.linute.MainContent;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverFragment;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.PeopleFragment.PeopleFragment;
import com.linute.linute.MainContent.ProfileFragment.Profile;
import com.linute.linute.MainContent.SlidingTab.SlidingTabLayout;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    public ViewPager mViewPager;
    private Toolbar mToolbar;
    private TextView mTitle;
    private FloatingActionButton fab;
    private PostCreatePage mPostCreatePage;
    public TaptUserProfileFragment mTaptUserProfileFragment;
    public FeedDetailPage mFeedDetailPage;

    private CoordinatorLayout parentView;
    private FloatingActionsMenu fam;
    private FloatingActionButton fabImage;
    private FloatingActionButton fabText;

    private Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragments = new Fragment[4];
        mFragments[0] = new DiscoverFragment();
        mFragments[1] = new PeopleFragment();
        mFragments[2] = new UpdatesFragment();
        mFragments[3] = new Profile();

        parentView = (CoordinatorLayout) findViewById(R.id.coordinator);

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
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                resetToolbar();
                switch (position) {
                    case 0:
                        setTitle("My Campus");
                        break;
                    case 1:
                        setTitle("People");
                        break;
                    case 2:
                        setTitle("Updates");
                        break;
                    case 3:
                        ((Profile) mFragments[3]).hasSetTitle = false;
                        ((Profile) mFragments[3]).updateAndSetHeader();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setTitle("My Campus");

        mViewPager.setAdapter(new LinuteFragmentAdapter(getSupportFragmentManager(),
                MainActivity.this, mFragments));

        // Give the TabLayout the ViewPager
        SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.mainactivity_tabbar);
        tabLayout.setSelectedIndicatorColors(R.color.white);
        tabLayout.setViewPager(mViewPager);

//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                newPost();
//            }
//        });

        fam = (FloatingActionsMenu) findViewById(R.id.fabmenu);
        fabImage = (FloatingActionButton) findViewById(R.id.fabImage);
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.toggle();
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(i);
            }
        });
        fabText = (FloatingActionButton) findViewById(R.id.fabText);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.toggle();
                newPost();
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

    public void resetToolbar() {
//        CoordinatorLayout coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
//        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        appbar.setExpanded(true, true);
//        behavior.onNestedFling(coordinator, appbar, null, 0, -1000, true);
    }

    public void noInternet() {
        Snackbar.make(parentView, "Seems like you don't have internet, might wanna fix that...", Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    public void toggleFam() {
        if (fam.isExpanded())
            fam.toggle();
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
        if (mPostCreatePage != null && mPostCreatePage.isVisible()) {
            mPostCreatePage.onBackPressed();
        } else if (mTaptUserProfileFragment != null && mTaptUserProfileFragment.isVisible()) {
            mTaptUserProfileFragment.dismiss();
        } else if (mFeedDetailPage != null && mFeedDetailPage.isVisible()) {
            mFeedDetailPage.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    public Fragment[] getFragments() {
        return mFragments;
    }
}
