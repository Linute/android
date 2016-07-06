package com.linute.linute.MainContent.DiscoverFragment;

import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

/**
 * Created by QiFeng on 6/23/16.
 */
public class VideoPlayerSingleton {

    private static SingleVideoPlaybackManager mSingleVideoPlaybackManager;


    public static SingleVideoPlaybackManager getSingleVideoPlaybackManager() {
        if (mSingleVideoPlaybackManager == null)
            mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();

        return mSingleVideoPlaybackManager;
    }
}
