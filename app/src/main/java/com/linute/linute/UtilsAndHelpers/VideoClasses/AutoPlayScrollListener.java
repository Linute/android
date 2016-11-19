package com.linute.linute.UtilsAndHelpers.VideoClasses;

import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.linute.linute.MainContent.DiscoverFragment.VideoFeedHolder;

/**
 * Created by QiFeng on 11/19/16.
 */

public class AutoPlayScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = AutoPlayScrollListener.class.getSimpleName();


    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (recyclerView.getContext() == null || newState != RecyclerView.SCROLL_STATE_IDLE) return;

        //need to check if network is currently wifi
        NetworkInfo mWifiInfo = ((ConnectivityManager) recyclerView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();


        if (mWifiInfo == null || !mWifiInfo.isConnected() || mWifiInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            return;
        }


        //play video when stop scrolling
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (manager == null) return;

        //get first completely visible
        int firstHolderPos = manager.findFirstCompletelyVisibleItemPosition();

        //if valid position and is videoView, play it
        //if not videoFeedHolder, return; videoview isn't the main focus of the screen atm
        if (firstHolderPos != RecyclerView.NO_POSITION) {
            if (!attemptToPlay(recyclerView, firstHolderPos))
                SingleVideoPlaybackManager.getInstance().stopPlayback();
            return;
        }

        // nothing is completely visible
        // see how many items are currently in view

        firstHolderPos = manager.findFirstVisibleItemPosition();
        int lastViewHolderPos = manager.findLastVisibleItemPosition();

        if (firstHolderPos == RecyclerView.NO_POSITION || lastViewHolderPos == RecyclerView.NO_POSITION)
            return;

        boolean played = false;

        //same item (only view on screen), try to play it
        int diff = lastViewHolderPos - firstHolderPos;
        if (diff < 0) { //check negatives
            return;
        }else if (diff == 0) {
            played = attemptToPlay(recyclerView, firstHolderPos);
        } else if (diff == 1) { //are the only 2 views on screen; find the one that's more visible
            Rect firstRect = new Rect();
            Rect lastRect = new Rect();

            manager.findViewByPosition(firstHolderPos).getGlobalVisibleRect(firstRect);
            manager.findViewByPosition(lastViewHolderPos).getGlobalVisibleRect(lastRect);

            played = attemptToPlay(recyclerView, lastRect.height() > firstRect.height() ?
                    lastViewHolderPos : firstHolderPos);

        } else{ //more than 2 views on screen; try to find video in between first and last
            //last > first is always true since we already checked for negative
            for (int i = firstHolderPos + 1; i < lastViewHolderPos; i++) {
                if (attemptToPlay(recyclerView, i)) { //found a video to play
                    played = true;
                    break;
                }
            }
        }


        //no videos to play were found. stop all videos that are already playing
        if (!played)
            SingleVideoPlaybackManager.getInstance().stopPlayback();
    }


    private boolean attemptToPlay(RecyclerView recyclerView, int pos) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(pos);
        if (holder == null || !(holder instanceof VideoFeedHolder)) return false;

        ((VideoFeedHolder) holder).playVideo();
        return true;
    }
}
