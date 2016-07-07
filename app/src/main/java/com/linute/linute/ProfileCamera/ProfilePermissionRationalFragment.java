package com.linute.linute.ProfileCamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 7/7/16.
 */
public class ProfilePermissionRationalFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ProfilePermissionRationalFragment.class.getSimpleName();
    private int mType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = ((ProfileCameraActivity) getActivity()).getType();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_camera_need_permission, container, false);

        TextView textView = (TextView) root.findViewById(R.id.text);
        Button button = (Button) root.findViewById(R.id.button);

        button.setOnClickListener(this);
        textView.setText(mType == ProfileCameraActivity.TYPE_CAMERA ?
                "Tapt needs permission to use your phone's camera and storage to take pictures" :
                "Tapt needs permission to user your phone's storage to access your pictures"
        );

        return root;
    }


    @Override
    public void onClick(View v) {
        ProfileCameraActivity activity = (ProfileCameraActivity) getActivity();
        if (activity != null) activity.requestPermissions();
    }
}
