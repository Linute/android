package com.linute.linute.MainContent.ProfileFragment;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

/**
 * Created by QiFeng on 3/19/16.
 */

public class EnlargePhotoViewer extends DialogFragment {

    public static final String URL = "item_url";
    public static final String TYPE = "type_enlarge";
    public static final int VIDEO = 2;
    public static final int IMAGE = 1;

    private String mItemUrl;
    private int mItemType;
    private ImageView mImageView;

    private TextureVideoView mTextureVideoView;
    private View mVideoLoadingIndicator;

    public static EnlargePhotoViewer newInstance(int type, String url){
        EnlargePhotoViewer frag = new EnlargePhotoViewer();
        Bundle args = new Bundle();

        args.putInt(TYPE, type);
        args.putString(URL, url);
        frag.setArguments(args);

        return frag;
    }

    public EnlargePhotoViewer() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemType = getArguments().getInt(TYPE);
        mItemUrl = getArguments().getString(URL);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mItemType == VIDEO)
            mTextureVideoView.stopPlayback();
        if (isVisible()){
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enlarge_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextureVideoView = (TextureVideoView) view.findViewById(R.id.video);
        mVideoLoadingIndicator = view.findViewById(R.id.icon);
        mImageView = (ImageView) view.findViewById(R.id.photo);

        if (mItemType == VIDEO){
            mTextureVideoView.setVisibility(View.VISIBLE);
            mVideoLoadingIndicator.setVisibility(View.VISIBLE);
        }else {
            mTextureVideoView.setVisibility(View.GONE);
            mVideoLoadingIndicator.setVisibility(View.GONE);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        //make dialog size of window width
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (mItemType == IMAGE) {
            Glide.with(getActivity())
                    .load(Utils.getMessageImageURL(mItemUrl))
                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mImageView);

        }else if (mItemType == VIDEO){

            mVideoLoadingIndicator.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_fade_out));

            mTextureVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoLoadingIndicator.clearAnimation();
                    mVideoLoadingIndicator.setAlpha(0);
                    mp.start();
                }
            });
            mTextureVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mVideoLoadingIndicator.getAlpha() != 1){
                        mp.start();
                    }
                }
            });

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemType != VIDEO) return;

                    if (mTextureVideoView.isPlaying()){
                        mVideoLoadingIndicator.setAlpha(1);
                        mTextureVideoView.pause();
                    }else {
                        mVideoLoadingIndicator.setAlpha(0);
                        mTextureVideoView.start();
                    }
                }
            });

            mTextureVideoView.setVideoPath(Utils.getMessageVideoURL(mItemUrl));
        }
    }

}
