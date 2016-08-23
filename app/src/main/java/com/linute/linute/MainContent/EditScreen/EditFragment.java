package com.linute.linute.MainContent.EditScreen;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;

/**
 * Created by mikhail on 8/22/16.
 */
public class EditFragment extends BaseFragment {


    private static final String ARG_URI = "content_uri";
    private static final String ARG_TYPE = "content_type";

    public enum ContentType {
        Photo, Video, UploadedPhoto, UploadedVideo
    }

    private Uri mUri;
    private ContentType mContentType;

    private EditContentTool[] mTools;
    private View[] mToolViews;


    public static EditFragment newInstance(Uri uri, ContentType contentType) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putInt(ARG_TYPE, contentType.ordinal());
        EditFragment fragment = new EditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUri = args.getParcelable(ARG_URI);
        mContentType = ContentType.values()[args.getInt(ARG_TYPE)];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_edit_content, container, false);

        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);

        View finalContentV = root.findViewById(R.id.final_content);
        View contentV = root.findViewById(R.id.base_content);


        ViewGroup overlaysV = (ViewGroup) root.findViewById(R.id.overlays);

        mTools = setupTools(overlaysV);
        mToolViews = new View[mTools.length];

        RecyclerView toolsListRV = (RecyclerView) root.findViewById(R.id.list_tools);
        final ViewGroup toolOptions = (ViewGroup) root.findViewById(R.id.layout_tools_menu);

        //Set up adapter that controls tool selection
        EditContentToolAdapter toolsAdapter = new EditContentToolAdapter();
        toolsAdapter.setTools(mTools);
        toolsAdapter.setOnItemSelectedListener(new EditContentToolAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                toolOptions.removeAllViews();
                if (mToolViews[i] == null) {
                    mToolViews[i] = mTools[i].createToolOptionsView(LayoutInflater.from(toolOptions.getContext()), toolOptions);
                }
                toolOptions.addView(mToolViews[i]);
            }
        });


        LinearLayoutManager toolsLLM = new LinearLayoutManager(root.getContext());
        toolsLLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        toolsListRV.setAdapter(toolsAdapter);
        toolsListRV.setLayoutManager(toolsLLM);


        return root;
    }

    private static EditContentTool[] setupTools(ViewGroup overlay) {
        return new EditContentTool[]{
            new PrivacySettingTool(overlay)
        };
    }

}
