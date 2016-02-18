package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    protected CircularImageView vCommentUserImage;
    protected TextView vCommentUserName;
    protected TextView vCommentUserText;

    private String mCommenterUserId;
    private String mUserName;

    private String mViewerUserId; //userId of person currently viewing page
    private String mImageSignature; //
    private boolean mIsAnon;


    public FeedDetailViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mViewerUserId = mSharedPreferences.getString("userID", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        vCommentUserImage = (CircularImageView) itemView.findViewById(R.id.comment_user_image);
        vCommentUserName = (TextView) itemView.findViewById(R.id.comment_user_name);
        vCommentUserText = (TextView) itemView.findViewById(R.id.comment);


    }

    void bindModel(Comment comment) {
        if (comment.isAnon()) {
            setAnonImage(comment.getAnonImage());
            vCommentUserName.setText("Anonymous");
        } else {
            setProfileImage(comment.getCommentUserProfileImage());
            vCommentUserName.setText(comment.getCommentUserName());
        }

        if (comment.getCommentUserId().equals(mViewerUserId)) {
            vCommentUserName.setTextColor(ContextCompat.getColor(mContext, R.color.user_name_blue));
        } else {
            vCommentUserName.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        mCommenterUserId = comment.getCommentUserId();
        mUserName = comment.getCommentUserName();

        mIsAnon = comment.isAnon();

        vCommentUserName.setOnClickListener(this);
        vCommentUserImage.setOnClickListener(this);

        //setting mentions and comment text
        if (comment.getMentionedPeople() != null && !comment.getMentionedPeople().isEmpty()){
            setUpMentionedOnClicks(comment);
        }
        else { //set text to comment's text
            vCommentUserText.setText(comment.getCommentUserPostText());
        }
    }

    private void setUpMentionedOnClicks(Comment comment){
        String commentText = comment.getCommentUserPostText();
        SpannableString commentSpannable = new SpannableString(commentText);

        int startSearchAtIndex = 0; //start search from

        //NOTE: assumes the list comes back in the order people were tagged.
        //NOTE: i.e. if text is: "@AndrewBee @JonathanI hello there" , the List will have the order {Andrew, Jonathan}
        //NOTE: this way we won't look for @JonathanI at index 0 when we know it will come after @Andrew

        final BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity == null) {
            vCommentUserText.setText(comment.getCommentUserPostText());
            return;
        };


        ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.secondaryColor)); //color of span

        for (final Comment.MentionedPersonLight person : comment.getMentionedPeople()){
            int start = commentText.indexOf(person.getFormatedFullName(), startSearchAtIndex);

            if (start != -1){ //-1 if string not found

                int end = person.getFormatedFullName().length() + start;
                startSearchAtIndex = end; //next mention will come after the end of this one

                ClickableSpan clickableSpan = new ClickableSpan() { //what happens when clicked
                    @Override
                    public void onClick(View widget) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(person.getFullName(), person.getId()));
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };

                commentSpannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                commentSpannable.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        vCommentUserText.setText(commentSpannable);
        vCommentUserText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (!mIsAnon) {
            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mUserName, mCommenterUserId));
            }
        }
    }


    public void setProfileImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .signature(new StringSignature(mImageSignature))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vCommentUserImage);
    }

    public void setAnonImage(String image) {
        Glide.with(mContext)
                .load(Utils.getAnonImageUrl(image))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vCommentUserImage);
    }
}
