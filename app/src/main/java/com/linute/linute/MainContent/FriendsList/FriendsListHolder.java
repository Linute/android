package com.linute.linute.MainContent.FriendsList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.linute.linute.MainContent.FindFriends.FindFriendsFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;

/**
 * Created by QiFeng on 4/20/16.
 */
public class FriendsListHolder extends UpdatableFragment {

    private Spinner mSpinner;
    private String mUserId;
    private int currentFragment = 0;
    private String[] mFragments;
    private FriendsListFragment[] mFriendsListFragments = new FriendsListFragment[2];

    public FriendsListHolder() {

    }
    /**
     * @param userId - id of the user who you want the friends list of
     * @return
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

        mFragments = viewIsOwner ? new String[]{"Followers", "Following"} : new String[]{"Followers"};

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner);

        mSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_text,
                        mFragments
                );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != currentFragment) {
                    currentFragment = position;
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, getFragment(position))
                            .commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if (fragmentNeedsUpdating()) {
            setFragmentNeedUpdating(false);
            currentFragment = 0;
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, getFragment(0))
                    .commit();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return rootView;
    }


    private FriendsListFragment getFragment(int position){
        if (mFriendsListFragments[position] == null){
            mFriendsListFragments[position] = FriendsListFragment.newInstance(position == 1, mUserId);
        }
        return mFriendsListFragments[position];
    }
}
