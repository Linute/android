package com.linute.linute.MainContent.CreateContent.Gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 9/5/16.
 */
public class PermissionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_need_permissions, container, false);

        ((TextView) root.findViewById(R.id.title)).setText("Select image from gallery");
        ((TextView) root.findViewById(R.id.text)).setText("Tapt needs permission to view images in your gallery");

        root.findViewById(R.id.needPermission_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryActivity activity = (GalleryActivity) getActivity();
                if (activity != null) activity.getReadPermission();
            }
        });

        return root;
    }
}
