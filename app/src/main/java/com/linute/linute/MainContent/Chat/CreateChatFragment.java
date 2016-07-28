package com.linute.linute.MainContent.Chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by mikhail on 7/28/16.
 */
public class CreateChatFragment extends Fragment {


    private RecyclerView mSelectedUsersRV;
    private RecyclerView mSearchRV;

    private SelectedUsersAdapter mSelectedUsersAdapter;
    private UserGroupSearchAdapter mSearchAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_create_chat, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchRV = (RecyclerView)view.findViewById(R.id.list_search);




//        new LSDKChat(getContext()).getUsersAndRooms()
    }
}
