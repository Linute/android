package com.linute.linute.MainContent.FeedDetailFragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private TextureVideoView vTextureVideoView;

    private Uri mLink;

    private int mPostType;

    private ViewFullScreenFragment(){

    }

    public static ViewFullScreenFragment newInstance(Uri link, int type){
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
        vTextureVideoView = (TextureVideoView) root.findViewById(R.id.video);

        mPostType = getArguments().getInt(TYPE_KEY);
        mLink = getArguments().getParcelable(URI_KEY);

        if (mPostType == Post.POST_TYPE_IMAGE){

        }

        return root;
    }
}
