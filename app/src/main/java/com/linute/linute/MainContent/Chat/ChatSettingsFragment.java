package com.linute.linute.MainContent.Chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.UtilsAndHelpers.BaseFragment;

/**
 * Created by mikhail on 7/8/16.
 */
public class ChatSettingsFragment extends BaseFragment {

    public static final String TAG = "ChatSettingsFragment";

    private static final String ARG_ROOM_ID = "roomId";

    private static String mRoomId;

    public static ChatSettingsFragment newInstance(String roomId){
        ChatSettingsFragment fragment = new ChatSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ARG_ROOM_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }




}
