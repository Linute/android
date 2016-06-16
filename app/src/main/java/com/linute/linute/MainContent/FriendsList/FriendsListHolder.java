package com.linute.linute.MainContent.FriendsList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.BaseFragment;

/**
 * Created by QiFeng on 4/20/16.
 */
public class FriendsListHolder extends BaseFragment {

    private String mUserId;
    private int currentFragment = 0;
    private FriendsListFragment[] mFriendsListFragments = new FriendsListFragment[2];

    public FriendsListHolder() {

    }
    /**
     * @param userId - id of the user who you want the friends list of
     * @return holder
     */
    public static FriendsListHolder newInstance(String userId) {
        FriendsListHolder friendsListFragment = new FriendsListHolder();
        Bundle b = new Bundle();
        b.putString(FriendsListFragment.USER_ID_KEY, userId);
        friendsListFragment.setArguments(b);
        return friendsListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getString(FriendsListFragment.USER_ID_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends_list_holder, container, false);

        boolean viewIsOwner = getActivity()
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("userID", "")
                .equals(mUserId);


        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ViewPager viewPager = (ViewPager)rootView.findViewById(R.id.frame);
        viewPager.setAdapter(new FriendsFragmentAdapter(getActivity().getSupportFragmentManager(), mUserId , viewIsOwner));

        TabLayout tabLayout = (TabLayout)rootView.findViewById(R.id.friends_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            setFragmentState(FragmentState.FINISHED_UPDATING);
            currentFragment = 0;
            viewPager.setCurrentItem(0);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    private static class FriendsFragmentAdapter extends FragmentStatePagerAdapter {

        Fragment[] mFriendsListFragments;
        String mUserId;

        public FriendsFragmentAdapter(FragmentManager fm, String userId, boolean isUser) {
            super(fm);
            this.mFriendsListFragments = new Fragment[(isUser ? 2 : 1)];
            this.mUserId = userId;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return "Followers";
                case 1: return "Following";
                default: return "";
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (mFriendsListFragments[position] == null){
                mFriendsListFragments[position] = FriendsListFragment.newInstance(position == 1, mUserId);
            }
            return mFriendsListFragments[position];
        }

        @Override
        public int getCount() {
            return mFriendsListFragments.length;
        }
    }

}
