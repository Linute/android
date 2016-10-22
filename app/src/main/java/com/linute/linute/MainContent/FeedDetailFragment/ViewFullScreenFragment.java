package com.linute.linute.MainContent.FeedDetailFragment;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.ProgressBarAnimation;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by QiFeng on 6/13/16.
 */
public class ViewFullScreenFragment extends BaseFragment {


    private static final String URI_KEY = "uri_key";
    private static final String TYPE_KEY = "type_key";
    private static final String POST_ID_KEY = "post_id_key";
    private static final String PROGRESS_THRESHOLD = "progress_thresh";

    private ZoomImageView vImage;
    private View vVideoParent;
    private TextureVideoView vTextureVideoView;
    private View vVideoLoadingIndicator;
    private View vLoadingText;

    private Uri mLink;
    private int mPostType;

    //stuff for impressions
    //if postId is not null, we will send impression
    private String mPostId;
    private String mCollegeId;
    private String mUserId;

    private ProgressBar vProgressBar;

    public ViewFullScreenFragment() {

    }

    /**
     * Use this if no impessions are needed
     *
     * @param link - url of post
     * @param type - status/image/video
     * @return fragment
     */
    public static ViewFullScreenFragment newInstance(Uri link, int type, int progressThreshold) {
        ViewFullScreenFragment fragment = new ViewFullScreenFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_KEY, link);
        args.putInt(TYPE_KEY, type);
        args.putInt(PROGRESS_THRESHOLD, progressThreshold);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Use this if we want to send impressions
     *
     * @param postId - id of post
     * @param link   - url of post
     * @param type   - status/video/image
     * @return fragment
     */
    public static ViewFullScreenFragment newInstance(String postId, Uri link, int type, int progressThreshold) {
        ViewFullScreenFragment fragment = new ViewFullScreenFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI_KEY, link);
        args.putInt(TYPE_KEY, type);
        args.putString(POST_ID_KEY, postId);
        args.putInt(PROGRESS_THRESHOLD, progressThreshold);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if not null, we will send impression
        mPostId = getArguments().getString(POST_ID_KEY, null);

        if (mPostId != null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
            mCollegeId = sharedPreferences.getString("collegeId", "");
            mUserId = sharedPreferences.getString("userID", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_full_screen, container, false);
        getActivity().onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0));
        vImage = (ZoomImageView) root.findViewById(R.id.imageView);
        vVideoParent = root.findViewById(R.id.video_parent);
        vTextureVideoView = (TextureVideoView) vVideoParent.findViewById(R.id.video);

        vProgressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        vProgressBar.setIndeterminate(false);

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            int threshold = getArguments().getInt(PROGRESS_THRESHOLD, 0);
            if (threshold == 0) vProgressBar.setVisibility(View.GONE);
            else {
                vProgressBar.setMax(threshold);
                ProgressBarAnimation animation = new ProgressBarAnimation(vProgressBar, threshold, 0);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        vProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                animation.setDuration(threshold);
                vProgressBar.startAnimation(animation);
            }

            setFragmentState(FragmentState.FINISHED_UPDATING);
        } else {
            vProgressBar.setVisibility(View.GONE);
        }

        /*root.findViewById(R.id.touch_layer).setOnTouchListener(
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
                });*/

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
                    .asBitmap()
                    .listener(new RequestListener<Uri, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            //hide loading when finished loading image
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            vLoadingText.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .into(vImage);
        } else if (mPostType == Post.POST_TYPE_VIDEO) {

            VideoPlayerSingleton.getSingleVideoPlaybackManager().playNewVideo(vTextureVideoView, mLink);
            vVideoParent.setVisibility(View.VISIBLE);
            vImage.setVisibility(View.GONE);
            vLoadingText.setVisibility(View.VISIBLE);
            //vTextureVideoView.stop();
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
                        if (mPostId != null)
                            sendImpressionsAsync(mPostId);
                    }
                }
            });
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

        vProgressBar.clearAnimation();
        vProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //sends info on how many times looped
    private void sendImpressionsAsync(final String id) {
        if (id == null) return;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity == null) return;
                    JSONObject body = new JSONObject();
                    body.put("college", mCollegeId);
                    body.put("user", mUserId);
                    body.put("room", id);
                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:loops", body);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
