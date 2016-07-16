package com.linute.linute.MainContent.SendTo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;

import java.util.ArrayList;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToFragment extends BaseFragment {

    public static final String TAG = SendToFragment.class.getSimpleName();
    public static final String POST_ID_KEY = "send_post_id_key";

    private ArrayList<SendToItem> mSendToItems = new ArrayList<>();
    private SendToAdapter mSendToAdapter;
    private String mPostId;


    /**
     * Use this constructor!
     * @param postId - the id of the post being shared
     * @return fragment
     */

    public static SendToFragment newInstance(String postId){
        SendToFragment fragment = new SendToFragment();
        Bundle bundle = new Bundle();
        bundle.putString(POST_ID_KEY, postId);
        fragment.setArguments(bundle);
        return fragment;
    }


    /**
     * Please don't use this constructor,
     * use newInstance(postId) instead
     */
    public SendToFragment(){

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mPostId = getArguments().getString(POST_ID_KEY);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_to, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        final Button vSendButton = (Button) root.findViewById(R.id.send_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (mSendToAdapter == null){
            mSendToAdapter = new SendToAdapter(getContext(), Glide.with(this), mSendToItems);
        }else {
            mSendToAdapter.setRequestManager(Glide.with(this));
        }

        mSendToAdapter.setButtonAction(new SendToAdapter.ButtonAction() {
            @Override
            public void turnOnButton(boolean turnOn) {
                vSendButton.setAlpha(turnOn ? 1f : 0.5f);
            }
        });

        vSendButton.setAlpha(mSendToAdapter.getCheckedItems().isEmpty() ? 0.5f : 1f);

        vSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendItems();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getFragmentState() == FragmentState.NEEDS_UPDATING){
            getSendToList();
        }
    }

    private void getSendToList() {
        //// TODO: 7/15/16 API CALL
    }

    public void sendItems(){
        if (mSendToAdapter == null || mSendToAdapter.getCheckedItems().isEmpty()) return;

        Log.d(TAG, "sendItems: sent");
        //// TODO: 7/15/16 send items
    }



}
