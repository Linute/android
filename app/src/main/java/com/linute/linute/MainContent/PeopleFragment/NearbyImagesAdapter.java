package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by QiFeng on 4/18/16.
 */
public class NearbyImagesAdapter extends PagerAdapter {
    private LayoutInflater mLayoutInflater;
    private List<People.PersonRecentPost> mRecentPosts;
    private Context mContext;
    private String mImageOfUsers;
    private String mImageSignature;
    private String mPersonName;
    private String mPersonsId;

    public NearbyImagesAdapter(Context context,
                               List<People.PersonRecentPost> recentPosts,
                               String imageOfUser,
                               String personName,
                               String personId,
                               String imageSignature
    ) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRecentPosts = recentPosts;
        mImageOfUsers = imageOfUser;
        mImageSignature = imageSignature;
        mPersonName = personName;
        mPersonsId = personId;
    }

    @Override
    public int getCount() {
        return mRecentPosts.size() + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);

        if (position == 0) {
            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(mImageOfUsers))
                    .placeholder(R.drawable.image_loading_background)
                    .signature(new StringSignature(mImageSignature))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) mContext;
                    if (activity != null){
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(
                                mPersonName,
                                mPersonsId));
                    }
                }
            });
        } else {
            final People.PersonRecentPost post = mRecentPosts.get(position - 1);
            Glide.with(mContext)
                    .load(Utils.getEventImageURL(post.getImage()))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) mContext;
                    if (activity != null) {
                        activity.addFragmentToContainer(FeedDetailPage.newInstance(
                                new Post(post.getImage(),
                                        post.getPostId(),
                                        mPersonsId,
                                        mPersonName)
                        ));
                    }
                }
            });
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
