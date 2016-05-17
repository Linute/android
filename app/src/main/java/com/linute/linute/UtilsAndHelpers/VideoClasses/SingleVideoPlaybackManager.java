package com.linute.linute.UtilsAndHelpers.VideoClasses;

import android.net.Uri;
import android.util.Log;

/**
 * Created by QiFeng on 3/11/16.
 */

/*
 *
 * Manager that makes sure only one video is being played at a time
 *
 */

public class SingleVideoPlaybackManager {

    private TextureVideoView mTextureVideoView;

    public SingleVideoPlaybackManager(){

    }

    public void playNewVideo(TextureVideoView videoView, Uri link){
        if (mTextureVideoView != null && videoView != mTextureVideoView){
            mTextureVideoView.stopPlayback();
            mTextureVideoView.runHideVideo(); //// TODO: 3/11/16 test without
        }

        videoView.setVideoURI(link);
        videoView.start();
        mTextureVideoView = videoView;
    }

    public void stopPlayback(){
        if (mTextureVideoView != null){
            Log.i("vid", "stopPlayback: 1");
            mTextureVideoView.stopPlayback();
            Log.i("vid", "stopPlayback: 3");
            mTextureVideoView.runHideVideo();
            mTextureVideoView = null;
            Log.i("vid", "stopPlayback: 2");
        }
    }

    public boolean hasVideo(){
        return mTextureVideoView != null;
    }
}
