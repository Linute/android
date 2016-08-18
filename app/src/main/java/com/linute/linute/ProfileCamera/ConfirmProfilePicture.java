package com.linute.linute.ProfileCamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
/**
 * Created by QiFeng on 7/5/16.
 */
public class ConfirmProfilePicture extends Fragment {

    public static final String IMAGE_URI = "profile_cam_image_uri";
    public static final String TAG = ConfirmProfilePicture.class.getSimpleName();

    private Uri mImageUri;


    public static ConfirmProfilePicture newInstance(Uri uri){
        Bundle b = new Bundle();
        ConfirmProfilePicture frag = new ConfirmProfilePicture();
        b.putParcelable(IMAGE_URI, uri);
        frag.setArguments(b);
        return frag;
    }


    public ConfirmProfilePicture(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null){
            mImageUri = args.getParcelable(IMAGE_URI);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_confirm_profile, container, false);
        ImageView vImageView = (ImageView) root.findViewById(R.id.imageView);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().setResult(RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        });

        root.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && mImageUri != null){
                    Intent i = new Intent();
                    i.setData(mImageUri);
                    getActivity().setResult(RESULT_OK, i);
                    getActivity().finish();
                }
            }
        });

        if (mImageUri != null){
            Glide.with(this)
                    .load(mImageUri)
                    .dontAnimate()
                    .placeholder(android.R.color.black)
                    .into(vImageView);
        }

        return root;
    }
}
