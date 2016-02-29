package com.linute.linute.MainContent.FindFriends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by QiFeng on 1/25/16.
 */
public class FindFriendsChoiceFragment extends Fragment {

    private View mContacts;
    private View mFacebook;
    private View mName;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_findfriends_choices, container, false);

        getActivity().invalidateOptionsMenu();

        mContacts = rootView.findViewById(R.id.findFriends_search_contacts);
        mFacebook = rootView.findViewById(R.id.findFriends_search_facebook);
        mName = rootView.findViewById(R.id.findFriends_search_name);

        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        mContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.addFragmentToContainer(FindFriendsFragment.newInstance(2));
            }
        });

        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.addFragmentToContainer(FindFriendsFragment.newInstance(1));
            }
        });

        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.addFragmentToContainer(FindFriendsFragment.newInstance(0));
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null){
            activity.setTitle("Find Friends");
            activity.resetToolbar();
        }
    }
}
