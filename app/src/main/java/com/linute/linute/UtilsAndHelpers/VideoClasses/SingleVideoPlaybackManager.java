package com.linute.linute.UtilsAndHelpers.VideoClasses;

import android.net.Uri;


/**
 * Created by QiFeng on 3/11/16.
 */

/**
 * Manager that makes sure only one video is being played at a time
 * When playing a video, ALWAYS use this to start the video
 * We don't want multiple videos being played at once
 */

public class SingleVideoPlaybackManager {

    private TextureVideoView mTextureVideoView;

    private static SingleVideoPlaybackManager mSingleVideoPlaybackManager;

    public static SingleVideoPlaybackManager getInstance() {
        if (mSingleVideoPlaybackManager == null) {
            mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();
        }

        return mSingleVideoPlaybackManager;
    }

    private SingleVideoPlaybackManager() {

    }

    // returns true if new Uri was set
    public boolean playNewVideo(TextureVideoView videoView, Uri link) {
        if (videoView == null || link == null) return false;

        if (mTextureVideoView != null && mTextureVideoView != videoView) {
            mTextureVideoView.stopPlayback();
            mTextureVideoView.runHideVideo();
        }

        mTextureVideoView = videoView;

        if (!videoView.isPlaying()){
            videoView.setVideoURI(link);
            return true;
        }

        return false;
    }

    public void stopPlayback() {
        if (mTextureVideoView != null) {
            mTextureVideoView.stopPlayback();
            mTextureVideoView.runHideVideo();
            mTextureVideoView = null;
        }
    }
}
