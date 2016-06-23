package com.linute.linute.SquareCamera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.MainContent.PostCreatePage;
import com.linute.linute.R;


public class NeedPermissionsFragment extends Fragment {

    public static final String TAG = "NeedPermissionsFragment";


    public NeedPermissionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_need_permissions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.needPermission_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity main = (CameraActivity) getActivity();
                if (main != null)
                    main.requestPermissions();
            }
        });

        view.findViewById(R.id.new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity cameraActivity = (CameraActivity) getActivity();
                if (cameraActivity != null)
                    cameraActivity.launchFragment(new PostCreatePage(), PostCreatePage.TAG);
            }
        });
    }
}

