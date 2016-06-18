package com.linute.linute.MainContent.FeedDetailFragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

/**
 * Created by QiFeng on 6/13/16.
 */
public class ViewFullScreenFragment extends BaseFragment {

    private static final String URI_KEY = "uri_key";
    private static final String TYPE_KEY = "type_key";

    private ImageView vImage;
    private View vVideoParent;
    private TextureVideoView vTextureVideoView;
    private View vVideoLoadingIndicator;
    private View vLoadingText;

    private Uri mLink;
    private int mPostType;

    private ViewFullScreenFragment() {

    }

    public static ViewFullScreenFragment newInstance(Uri link, int type) {
        ViewFullScreenFragment fragment = new ViewFullScreenFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_KEY, link);
        args.putInt(TYPE_KEY, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_full_screen, container, false);

        vImage = (ImageView) root.findViewById(R.id.imageView);
        vVideoParent = root.findViewById(R.id.video_parent);
        vTextureVideoView = (TextureVideoView) vVideoParent.findViewById(R.id.video);

        root.findViewById(R.id.touch_layer).setOnTouchListener(
                new View.OnTouchListener() {
                    float totalMovement = 0;
                    float x;
                    float y;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                totalMovement = 0;
                                x = event.getX();
                                y = event.getY();
                                break;
                            case MotionEvent.ACTION_UP:
                                if (totalMovement >= 150) {
                                    getFragmentManager().popBackStack();
                                    return true;
                                } else {
                                    if (mPostType == Post.POST_TYPE_VIDEO) {
                                        if (vTextureVideoView.isPlaying()) {
                                            vVideoLoadingIndicator.setVisibility(View.VISIBLE);
                                            vTextureVideoView.pause();
                                        } else {
                                            vVideoLoadingIndicator.setVisibility(View.GONE);
                                            vTextureVideoView.start();
                                        }
                                    }
                                    return true;
                                }
                            case MotionEvent.ACTION_MOVE:
                                totalMovement += Math.abs(event.getX() - x);
                                totalMovement += Math.abs(event.getY() - y);
                                x = event.getX();
                                y = event.getY();
                                break;
                        }
                        return true;
                    }
                });

        vVideoLoadingIndicator = root.findViewById(R.id.play);
        vLoadingText = root.findViewById(R.id.loading);

        mPostType = getArguments().getInt(TYPE_KEY);
        mLink = getArguments().getParcelable(URI_KEY);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPostType == Post.POST_TYPE_IMAGE || mPostType == Post.POST_TYPE_STATUS) {
            vVideoParent.setVisibility(View.GONE);
            vImage.setVisibility(View.VISIBLE);
            vVideoLoadingIndicator.setVisibility(View.GONE);

            vLoadingText.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(mLink)
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            //hide loading when finished loading image
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            vLoadingText.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(vImage);
        } else if (mPostType == Post.POST_TYPE_VIDEO) {
            vVideoParent.setVisibility(View.VISIBLE);
            vImage.setVisibility(View.GONE);
            vLoadingText.setVisibility(View.VISIBLE);
            vTextureVideoView.stopPlayback();
            vTextureVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setFragmentState(FragmentState.FINISHED_UPDATING);
                    vLoadingText.setVisibility(View.GONE);
                    mp.start();
                }
            });

            vTextureVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (vVideoLoadingIndicator.getVisibility() == View.GONE) {
                        mp.start();
                    }
                }
            });

            vTextureVideoView.setVideoURI(mLink);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPostType == Post.POST_TYPE_VIDEO) {
            if (getFragmentState() == FragmentState.FINISHED_UPDATING) {
                vTextureVideoView.pause();
                vVideoLoadingIndicator.setVisibility(View.VISIBLE);
                vLoadingText.setVisibility(View.GONE);
            } else {
                vTextureVideoView.stopPlayback();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (vTextureVideoView != null) vTextureVideoView.stopPlayback();
        if (getActivity() != null) getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
