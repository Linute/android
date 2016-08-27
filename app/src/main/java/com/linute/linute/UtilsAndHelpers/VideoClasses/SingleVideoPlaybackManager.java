package com.linute.linute.UtilsAndHelpers.VideoClasses;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by QiFeng on 3/11/16.
 */

/**
 *
 * Manager that makes sure only one video is being played at a time
 *
 */

public class SingleVideoPlaybackManager {

    private ScalableVideoView mTextureVideoView;

    public SingleVideoPlaybackManager(){

    }

    public void playNewVideo(Context context, ScalableVideoView videoView, Uri link){
        if (mTextureVideoView != null && videoView != mTextureVideoView){
            mTextureVideoView.stop();
            mTextureVideoView.runHideVideo();
        }

        try {
            videoView.setDataSource(context,link);
            mTextureVideoView = videoView;
        }catch (IOException e){
            e.printStackTrace();
            videoView.stop();
        }

    }

    public void stopPlayback(){
        if (mTextureVideoView != null){
            mTextureVideoView.stop();
            mTextureVideoView.reset();
            mTextureVideoView.runHideVideo();
            mTextureVideoView = null;
        }
    }

    public boolean hasVideo(){
        return mTextureVideoView != null;
    }
}
