package com.linute.linute.MainContent.FindFriends;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by QiFeng on 1/25/16.
 */

public class FindFriendsChoiceFragment extends BaseFragment {

    public static final String TAG = FindFriendsChoiceFragment.class.getSimpleName();
    public static final String ONLY_FRAGMENT_IN_STACK = "key_only_frag_in_stack";

    private EditText mSearchView;
    private ViewPager mViewPager;
    private int mCurrentTab = 0;
    private Toolbar mToolbar;

    // if this is the only item only item in stack:
    //      navigation icon will be hamburger
    //      pressing hamburger opens drawer
    private boolean mOnlyFragmentInStack = false;

    private FindFriendsFragment[] mFindFriendsFragments;

    public static FindFriendsChoiceFragment newInstance(boolean onlyFragmentInStack) {
        Bundle b = new Bundle();
        b.putBoolean(ONLY_FRAGMENT_IN_STACK, onlyFragmentInStack);
        FindFriendsChoiceFragment f = new FindFriendsChoiceFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOnlyFragmentInStack = getArguments() != null &&
                getArguments().getBoolean(ONLY_FRAGMENT_IN_STACK, false);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_findfriends_choices, container, false);

        if (mFindFriendsFragments == null) {
            mFindFriendsFragments = new FindFriendsFragment[]{FindFriendsFragment.newInstance(0),
                    FindFriendsFragment.newInstance(1), FindFriendsFragment.newInstance(2)};
        }

        FragmentPagerAdapter fragmentPagerAdapter = new FindFriendsFragmentAdapter(getChildFragmentManager(), mFindFriendsFragments);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mSearchView = (EditText) rootView.findViewById(R.id.search_view);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(2);

        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (getActivity() == null) return false;
                (mFindFriendsFragments[mViewPager.getCurrentItem()]).searchWithQuery(mSearchView.getText().toString());
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

                return true;
            }
        });

        mToolbar.setNavigationIcon(
                mOnlyFragmentInStack ?
                        R.drawable.ic_action_navigation_menu :
                        R.drawable.ic_action_navigation_arrow_back_inverted
        );

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    if (mOnlyFragmentInStack) activity.openDrawer();
                    else getFragmentManager().popBackStack();
                }
            }
        });

        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mCurrentTab != position) {
                    //changed fragments. if that fragment has already updated, then search using new query string
                    //else do nothing
                    if (mFindFriendsFragments[position] != null
                            && mFindFriendsFragments[position].getFragmentState() != BaseFragment.FragmentState.NEEDS_UPDATING) {
                        mFindFriendsFragments[position].searchWithQuery(mSearchView.getText().toString());
                        //Log.i(TAG, "onPageSelected: ");
                    }

                    mCurrentTab = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.i("TEST", "afterTextChanged: "+s.toString());
                (mFindFriendsFragments[mViewPager.getCurrentItem()]).searchWithQuery(s.toString());
            }
        });

        ((TabLayout) rootView.findViewById(R.id.tabs)).setupWithViewPager(mViewPager);

        mSearchView.post(new Runnable() {
            @Override
            public void run() {
                BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (activity != null) {
                    //hide keyboard
                    mSearchView.requestFocus();
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mSearchView, 0);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOnlyFragmentInStack) {
            mNotificationSubscription = NotificationEventBus
                    .getInstance()
                    .getObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mNotificationEventAction1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            //hide keyboard
            if (mSearchView.hasFocus()) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }
    }

    private Subscription mNotificationSubscription;
    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (mOnlyFragmentInStack && notificationEvent.getType() == NotificationEvent.DISCOVER) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            }
        }
    };
}
