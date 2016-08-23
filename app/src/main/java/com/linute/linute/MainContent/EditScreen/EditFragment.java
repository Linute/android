package com.linute.linute.MainContent.EditScreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.bson.types.ObjectId;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by mikhail on 8/22/16.
 */
public class EditFragment extends BaseFragment {


    private static final String ARG_URI = "content_uri";
    private static final String ARG_CONTENT_TYPE = "content_type";
    private static final String ARG_RETURN_TYPE = "return_type";
    private static final String ARG_CAMERA_TYPE = "camera_type";
    private ViewGroup mContentView;
    private View mFinalContentView;
    private ViewGroup mToolOptionsView;

    public enum ContentType {
        Photo, Video, UploadedPhoto, UploadedVideo
    }

    private Uri mUri;
    private ContentType mContentType;

    private EditContentTool[] mTools;
    private View[] mToolViews;

    private int mReturnType;
//    private int mCameraType;

    Subscription mSubscription;

    private String mCollegeId;
    private String mUserId;
    private String mUserToken;


    public static EditFragment newInstance(Uri uri, ContentType contentType, int returnType/*, int cameraType*/) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putInt(ARG_CONTENT_TYPE, contentType.ordinal());
        args.putInt(ARG_RETURN_TYPE, returnType);
//        args.putInt(ARG_CAMERA_TYPE, cameraType);
        EditFragment fragment = new EditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUri = args.getParcelable(ARG_URI);
        mContentType = ContentType.values()[args.getInt(ARG_CONTENT_TYPE)];
//        mCameraType = args.getInt(ARG_CAMERA_TYPE);
        mReturnType = args.getInt(ARG_RETURN_TYPE);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");
        mUserToken = sharedPreferences.getString("userToken", "");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_edit_content, container, false);

        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_cancel);
        toolbar.inflateMenu(R.menu.menu_fragment_edit);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case android.R.id.home:
                        getActivity().getSupportFragmentManager().popBackStack();
                        return true;
                    case R.id.menu_item_done:
                        onDoneButtonPress();
                        return true;
                }
                return false;
            }
        });


        mFinalContentView = root.findViewById(R.id.final_content);
        mContentView = (ViewGroup)root.findViewById(R.id.base_content);
        setupMainContent(mUri, mContentType);


        ViewGroup overlaysV = (ViewGroup) root.findViewById(R.id.overlays);

        mTools = setupTools(overlaysV);
        mToolViews = new View[mTools.length];

        RecyclerView toolsListRV = (RecyclerView) root.findViewById(R.id.list_tools);
        mToolOptionsView = (ViewGroup) root.findViewById(R.id.layout_tools_menu);

        //Set up adapter that controls tool selection
        EditContentToolAdapter toolsAdapter = new EditContentToolAdapter();
        toolsAdapter.setTools(mTools);
        toolsAdapter.setOnItemSelectedListener(new EditContentToolAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                onToolSelected(i);
            }
        });

        toolsAdapter.selectItem(0);


        LinearLayoutManager toolsLLM = new LinearLayoutManager(root.getContext());
        toolsLLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        toolsListRV.setAdapter(toolsAdapter);
        toolsListRV.setLayoutManager(toolsLLM);

        return root;
    }

    protected void onToolSelected(int i) {
        mToolOptionsView.removeAllViews();
        if (mToolViews[i] == null) {
            mToolViews[i] = mTools[i].createToolOptionsView(LayoutInflater.from(mToolOptionsView.getContext()), mToolOptionsView);
        }
        mToolOptionsView.addView(mToolViews[i]);
    }


    private void setupMainContent(Uri uri, ContentType contentType){
        switch (contentType){
            case Photo:
            case UploadedPhoto:
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mContentView.addView(imageView);
                imageView.setImageURI(uri);
                break;
            case Video:
            case UploadedVideo:
                SurfaceView surface = new SurfaceView(getContext());
                break;
        }
    }

    private EditContentTool[] setupTools(ViewGroup overlay) {
        return new EditContentTool[]{
            new PrivacySettingTool(mUri, mContentType, overlay),
                new OverlaysTool(mUri, mContentType, overlay)
        };
    }

    private void onDoneButtonPress(){
        ProcessingOptions options = new ProcessingOptions();
        for(EditContentTool tool:mTools){
            tool.processContent(mUri, mContentType, options);
        }

        beginUpload(options);
    }

    private void beginUpload(ProcessingOptions options){
        switch (mContentType){
            case Video:
            case UploadedVideo:
                processVideo(options);
                return;
            case Photo:
            case UploadedPhoto:
                processPhoto(options);
                return;
        }
    }

    private void showProgress(boolean show){}

    private void processPhoto(final ProcessingOptions options){
        if (getActivity() == null) return;

        /*if (mEditText.getVisibility() == View.VISIBLE) {
            mEditText.setVisibility(View.GONE);
            if (!mEditText.getText().toString().trim().isEmpty()) {
                mTextView.setText(mEditText.getText().toString());
                mTextView.setVisibility(View.VISIBLE);
            }
            hideKeyboard();
            return;
        }
        */


        showProgress(true);
        if (getActivity() == null) return;


        if (mReturnType != CameraActivity.SEND_POST) {
            mSubscription = Observable.just(ImageUtility.savePictureToCache(getActivity(), ImageUtility.getBitmapFromView(mFinalContentView)))
                    .observeOn(io())
                    .subscribeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {
                                Intent i = new Intent()
                                        .putExtra("image", uri)
                                        .putExtra("type", CameraActivity.IMAGE)
                                        .putExtra("privacy", options.postAsAnon)
                                        .putExtra("title", options.text);
                                getActivity().setResult(Activity.RESULT_OK, i);
                                getActivity().finish();
                            } else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), "An error occured while saving the image", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }
                        }
                    });
        } else {
            mSubscription = Observable.just(ImageUtility.savePicture(getActivity(), ImageUtility.getBitmapFromView(mFinalContentView)))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {

                                PendingUploadPost pendingUploadPost =
                                        new PendingUploadPost(
                                                ObjectId.get().toString(),
                                                mCollegeId,
                                                (options.postAsAnon ? 1 : 0),
                                                options.allowAnonComments ? 0 : 1,
                                                options.text,
                                                1,
                                                uri.toString(),
                                                null,
                                                mUserId,
                                                mUserToken
                                        );

                                Intent result = new Intent();
                                result.putExtra(PendingUploadPost.PENDING_POST_KEY, pendingUploadPost);
                                Toast.makeText(getActivity(), "Uploading photo in background...", Toast.LENGTH_SHORT).show();

                                getActivity().setResult(Activity.RESULT_OK, result);
                                getActivity().finish();
                            }else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), "An error occured while saving the image", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }
                        }
                    });
        }
    }

    private void processVideo(ProcessingOptions options){

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

   /* protected void hideKeyboard() {
        mEditText.clearFocus(); //release focus from EditText and hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContentContainer.getWindowToken(), 0);
    }*/
}
