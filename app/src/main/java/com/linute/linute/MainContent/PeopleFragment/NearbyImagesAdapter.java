package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by QiFeng on 4/18/16.
 */
public class NearbyImagesAdapter extends RecyclerView.Adapter<NearbyImagesAdapter.ImageViewHolder> {

    People mPerson;
    Context mContext;
    String mImageSignature;


    public NearbyImagesAdapter(Context context) {
        mContext = context;
        mImageSignature = mContext
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("imageSigniture", "000");
    }

    public void setPerson(People per) {
        mPerson = per;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_image_view_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        if (position == 0) {
            holder.bindView();
        } else {
            holder.bindView(mPerson.getPersonRecentPosts().get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return mPerson.getPersonRecentPosts().size() + 1;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView vImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            vImageView = (ImageView) itemView.findViewById(R.id.image);
        }


        public void bindView(final People.PersonRecentPost post) {
            Glide.with(mContext)
                    .load(Utils.getEventImageURL(post.getImage()))
                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(vImageView);
            vImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(
                                FeedDetailPage.newInstance(
                                        new Post(
                                                post.getImage(),
                                                post.getPostId(),
                                                mPerson.getID(),
                                                mPerson.getName()))
                        );
                    }
                }
            });
        }

        public void bindView() {
            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(mPerson.getProfileImage()))
                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .signature(new StringSignature(mImageSignature))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(vImageView);
            vImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mPerson.getName(), mPerson.getID()));
                    }
                }
            });
        }
    }

}
