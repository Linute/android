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
            mTextureVideoView.stopPlayback();
            mTextureVideoView.runHideVideo();
            mTextureVideoView = null;
        }
    }
}
