package com.linute.linute.MainContent.Global;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleAndSingleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by QiFeng on 5/14/16.
 */
public class TrendingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int UNDEFINED = 666;

    List<Post> mPosts;
    Context mContext;
    SingleVideoPlaybackManager mSingleVideoPlaybackManager;

    String mUserId;
    String mImageSignature;
    String mCollege;
    String mTrendId;

    ScrollToPosition mScrollToPosition;

    short mFooterState = 0;


    LoadMoreViewHolder.OnLoadMore mOnLoadMore;


    TrendingItemAdapter(List<Post> posts, Context context, SingleVideoPlaybackManager manager, LoadMoreViewHolder.OnLoadMore o, String trendId) {
        mContext = context;
        mPosts = posts;
        mSingleVideoPlaybackManager = manager;
        mOnLoadMore = o;
        mTrendId = trendId;

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");
        mCollege = mSharedPreferences.getString("collegeId", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Post.POST_TYPE_VIDEO) {
            return new VideoTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item_video, parent, false));
        } else if (viewType == Post.POST_TYPE_IMAGE) {
            return new BaseTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item, parent, false));
        } else if (viewType == LoadMoreViewHolder.FOOTER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_footer, parent, false),
                    "",
                    "Come back later for more!"
            );
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseTrendViewHolder) {
            ((BaseTrendViewHolder) holder).bindView(mPosts.get(position));
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mFooterState);
        }

        if (position == mPosts.size() - 1 && mOnLoadMore != null) mOnLoadMore.loadMore();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mPosts.size()) {
            return LoadMoreViewHolder.FOOTER;
        } else if (mPosts.get(position).isVideoPost()) {
            return Post.POST_TYPE_VIDEO;
        } else if (mPosts.get(position).isImagePost()) {
            return Post.POST_TYPE_IMAGE;
        } else {
            return UNDEFINED;
        }
    }


    public boolean setFooterState(short footerState) {
        if (mFooterState != footerState) {
            mFooterState = footerState;
            return true;
        }
        return false;
    }

    public short getFooterState() {
        return mFooterState;
    }

    public void setScrollToPosition(ScrollToPosition s) {
        mScrollToPosition = s;
    }

    @Override
    public int getItemCount() {
        return mPosts.size() == 0 ? 0 : mPosts.size() + 1;
    }


    public class BaseTrendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView vPostUserName;
        protected TextView vLikesText; //how many likes we have
        protected TextView vCommentText; //how many comments we have

        protected RoundedImageView vUserImage;
        protected ImageView vImageView;

        protected Post mPost;

        protected View vTop;
        protected View vBottom;

        protected View vLikeLayer;


        public BaseTrendViewHolder(View itemView) {
            super(itemView);
            vPostUserName = (TextView) itemView.findViewById(R.id.profile_name);
            vLikesText = (TextView) itemView.findViewById(R.id.like);
            vCommentText = (TextView) itemView.findViewById(R.id.comment);
            vUserImage = (RoundedImageView) itemView.findViewById(R.id.profile_image);
            vImageView = (ImageView) itemView.findViewById(R.id.image);

            vTop = itemView.findViewById(R.id.topPanel);
            vBottom = itemView.findViewById(R.id.bottom);
            vLikeLayer = itemView.findViewById(R.id.red_view);

            vPostUserName.setOnClickListener(this);
            vUserImage.setOnClickListener(this);
            vLikesText.setOnClickListener(this);
            vCommentText.setOnClickListener(this);

            itemView.setOnClickListener(new DoubleAndSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    singleClick();
                }

                @Override
                public void onDoubleClick(View v) {
                    doubleClick();
                }
            });
        }

        public void singleClick() {
            if (vTop.getVisibility() == View.GONE) {
                activate();
            } else {
                deactivate();
            }
        }

        public void doubleClick() {
            animateView();
            if (!mPost.isPostLiked()) {
                like();
            }
        }

        public void gainedFocus() {
        }

        public void lostFocus() {
            deactivate();
        }


        public void activate() {
            if (vTop.getVisibility() == View.VISIBLE) return;
            vTop.setVisibility(View.VISIBLE);
            vBottom.setVisibility(View.VISIBLE);
        }

        public void deactivate() {
            if (vTop.getVisibility() != View.GONE) {
                vTop.setVisibility(View.GONE);
                vBottom.setVisibility(View.GONE);
            }
        }


        public void bindView(Post post) {
            mPost = post;
            if (post.getPrivacy() == 0) {
                Glide.with(mContext)
                        .load(post.getUserImage())
                        .dontAnimate()
                        .signature(new StringSignature(mImageSignature))
                        .placeholder(R.drawable.image_loading_background)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                        .into(vUserImage);
                vPostUserName.setText(post.getUserName());
            } else {
                Glide.with(mContext)
                        .load(post.getAnonImage())
                        .dontAnimate()
                        .placeholder(R.drawable.image_loading_background)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                        .into(vUserImage);

                vPostUserName.setText("Anonymous");
            }

            vLikesText.setText("Like (" + post.getNumLike() + ")");
            vCommentText.setText("Comment (" + post.getNumOfComments() + ")");

            Glide.with(mContext)
                    .load(post.getImage())
                    .dontAnimate()
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vImageView);

            vTop.setVisibility(View.GONE);
            vBottom.setVisibility(View.GONE);

            sendImpressionsAsync(mPost.getPostId());
        }

        @Override
        public void onClick(View v) {
            if (v == vPostUserName || v == vUserImage) {

                if (mPost.getPrivacy() == 1) return;

                MainActivity activity = (MainActivity) mContext;
                if (activity != null) {
                    activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mPost.getUserName(), mPost.getUserId()));
                }
            } else if (v == vLikesText) {
                if (mPost.isPostLiked()) {
                    unlike();
                } else {
                    animateView();
                    like();
                }
            } else if (v == vCommentText) {
                MainActivity activity = (MainActivity) mContext;
                if (activity != null) {
                    activity.addFragmentToContainer(FeedDetailPage.newInstance(mPost));
                }
            }
        }


        public void unlike() {
            MainActivity activity = (MainActivity) mContext;
            if (activity == null) return;

            try {
                JSONObject body = new JSONObject();
                body.put("user", mUserId);
                body.put("room", mPost.getPostId());

                activity.emitSocket(API_Methods.VERSION + ":posts:like", body);

                mPost.setPostLiked(false);
                mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) - 1);
                vLikesText.setText("Like (" + mPost.getNumLike() + ")");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void like() {
            MainActivity activity = (MainActivity) mContext;
            if (activity == null) return;

            try {
                JSONObject body = new JSONObject();
                body.put("user", mUserId);
                body.put("room", mPost.getPostId());
                activity.emitSocket(API_Methods.VERSION + ":posts:like", body);
                mPost.setPostLiked(true);
                mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) + 1);
                vLikesText.setText("Like (" + mPost.getNumLike() + ")");
            } catch (JSONException e) {
                Log.i("test", "onClick: ");
                e.printStackTrace();
            }
        }

        public void animateView() {
            AlphaAnimation a = new AlphaAnimation(0.0f, 0.75f);
            a.setDuration(400);

            final AlphaAnimation a2 = new AlphaAnimation(0.75f, 0.0f);
            a2.setDuration(200);

            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    vLikeLayer.startAnimation(a2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            vLikeLayer.startAnimation(a);
        }
    }


    public class VideoTrendViewHolder extends BaseTrendViewHolder {
        public VideoTrendViewHolder(View itemView) {
            super(itemView);
        }

//        protected View vPlayView;
//        protected SquareVideoView vSquareVideoView;
//        private boolean videoProcessing = false;
//
//        public VideoTrendViewHolder(View itemView) {
//            super(itemView);
//            vPlayView = itemView.findViewById(R.id.play);
//            vSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.video);
//
//            vSquareVideoView.setCustomSurfaceTextureListener(new TextureVideoView.CustomSurfaceTextureListener() {
//                @Override
//                public void onSurfaceDestroyed() {
//                    //when video surface destroyed, hide the video and show image
//                    vImageView.setVisibility(View.VISIBLE);
//                    vSquareVideoView.setVisibility(View.GONE);
//                    vPlayView.setVisibility(View.VISIBLE);
//                }
//            });
//
//            vSquareVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //when video ready to be played
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    videoProcessing = false;
//                    vImageView.setVisibility(View.GONE);
//                    vPlayView.clearAnimation();
//                    vPlayView.setVisibility(View.GONE);
//                }
//            });
//
//            vSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    //if video is paused AND finishes at the same time, video won't pause
//                    //if icon is showing, then user has paused video
//                    if (vPlayView.getVisibility() == View.GONE) {
//                        vSquareVideoView.start();
//                    }
//                }
//            });
//
//            vSquareVideoView.setHideVideo(new TextureVideoView.HideVideo() {
//                @Override
//                public void hideVideo() {
//                    videoProcessing = false;
//                    vImageView.setVisibility(View.VISIBLE);
//                    vSquareVideoView.setVisibility(View.GONE);
//                    vPlayView.clearAnimation();
//                    vPlayView.setVisibility(View.VISIBLE);
//                }
//            });
//        }
//
//        @Override
//        public void bindView(Post post) {
//            super.bindView(post);
//            vPlayView.clearAnimation();
//            vPlayView.setVisibility(View.VISIBLE);
//            vImageView.setVisibility(View.VISIBLE);
//            vSquareVideoView.setVisibility(View.GONE);
//            videoProcessing = false;
//        }
//
//        @Override
//        public void singleClick() {
//            if (videoProcessing) {
//                activate();
//                mSingleVideoPlaybackManager.stopPlayback();
//            } else if (!mSingleVideoPlaybackManager.hasVideo()) {
//                deactivate();
//                videoProcessing = true;
//                vSquareVideoView.setVisibility(View.VISIBLE);
//                vPlayView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fade_out));
//                mSingleVideoPlaybackManager.playNewVideo(vSquareVideoView, Uri.parse(mPost.getVideoUrl()));
//            } else if (vSquareVideoView.isPlaying()) {
//                activate();
//                vPlayView.clearAnimation();
//                vPlayView.setVisibility(View.VISIBLE);
//                vSquareVideoView.pause();
//            } else {
//                deactivate();
//                vPlayView.setVisibility(View.GONE);
//                vSquareVideoView.start();
//            }
//        }
//
//        @Override
//        public void gainedFocus() {
//            if (videoProcessing) return;
//            videoProcessing = true;
//            vSquareVideoView.setVisibility(View.VISIBLE);
//            vPlayView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fade_out));
//            mSingleVideoPlaybackManager.playNewVideo(vSquareVideoView, Uri.parse(mPost.getVideoUrl()));
//        }
//
//        @Override
//        public void lostFocus() {
//            deactivate();
//            mSingleVideoPlaybackManager.stopPlayback();
//        }
    }

    public interface ScrollToPosition {
        void scrollToPosition(int position);
    }


    private void sendImpressionsAsync(final String id) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject body = new JSONObject();

                    body.put("college", mCollege);
                    body.put("user", mUserId);

                    JSONArray mEventIds = new JSONArray();
                    mEventIds.put(id);
                    body.put("events", mEventIds);
                    body.put("trend",mTrendId);

                    BaseTaptActivity activity = (BaseTaptActivity) mContext;

                    if (activity != null) {
                        activity.emitSocket(API_Methods.VERSION + ":posts:impressions", body);
                        //Log.i(TAG, "run: impression sent");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



}
