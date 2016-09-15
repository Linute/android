package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.RequestManager;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.linute.linute.MainContent.DiscoverFragment.StatusFeedHolder;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;

/**
 * Created by QiFeng on 2/4/16.
 */
public class FeedDetailHeaderStatusViewHolder extends StatusFeedHolder {


    public static final String TAG = FeedDetailHeaderStatusViewHolder.class.getSimpleName();

    public FeedDetailHeaderStatusViewHolder(View view, Context context, RequestManager manager, BaseFeedAdapter.PostAction action){
        super(view, context, manager, action);
        view.findViewById(R.id.more).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {

        final BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null || mPost == null || mPost.getUserId() == null) return;

        //tap image or name
        if ((v == vUserImage || v == vPostUserName) && mPost.getPrivacy() == 0) {
            activity.addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(
                            mPost.getUserName()
                            , mPost.getUserId())
            );
        }

        //like button pressed
        else if (v == vLikeButton) {
            vLikesHeart.toggle();
        }

        else if(v == vShareButton){
            vShareButton.findViewById(R.id.shareProgress).setVisibility(View.VISIBLE);
            vShareButton.findViewById(R.id.postShare).setVisibility(View.GONE);
            mPostAction.startShare(mPost, new BaseFeedAdapter.ShareProgressListener() {
                @Override
                public void updateShareProgress(final int progress) {
                    vShareButton.post(new Runnable() {
                        @Override
                        public void run() {
                            final DonutProgress donutProgress = (DonutProgress) vShareButton.findViewById(R.id.shareProgress);
                            donutProgress.setProgress(progress);
                            if(progress == 100 || progress == -1){
                                if(progress == -1){
                                    CustomSnackbar.make(activity.findViewById(android.R.id.content), "Share Failed", CustomSnackbar.LENGTH_SHORT).setBackgroundColor(R.color.white).show();
//                                            Toast.makeText(mContext, "Share failed", Toast.LENGTH_SHORT).show();
                                    donutProgress.setProgress(100);
                                    donutProgress.setText("X");
                                    int red = mContext.getResources().getColor(R.color.red);
                                    donutProgress.setTextColor(red);
                                    donutProgress.setFinishedStrokeColor(red);
                                }else{
                                    donutProgress.setProgress(100);
                                    donutProgress.setText("✓");
                                    int green = mContext.getResources().getColor(R.color.green_color);
                                    donutProgress.setTextColor(green);
                                    donutProgress.setFinishedStrokeColor(green);

                                }
                                vShareButton.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        vShareButton.findViewById(R.id.shareProgress).setVisibility(View.GONE);
                                        donutProgress.setProgress(0);
                                        donutProgress.setText(null);
                                        int blue = mContext.getResources().getColor(R.color.blue_color);
                                        donutProgress.setTextColor(blue);
                                        donutProgress.setFinishedStrokeColor(blue);
                                        vShareButton.findViewById(R.id.postShare).setVisibility(View.VISIBLE);
                                    }
                                },1500);
                            }
                        }
                    });
                }
            });
        }
    }

}
