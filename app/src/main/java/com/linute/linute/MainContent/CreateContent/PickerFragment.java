package com.linute.linute.MainContent.CreateContent;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PickerFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    public static final String PICKER_TYPE_KEY = "picker_type_key";
    public static final int PICK_IMAGE = 0;
    public static final int PICK_VIDEO = 1;
    public static final int PICK_ALL = 2;

    private int mPickerType;
    private View vProgress;
    private RecyclerView vRecyclerView;

    final String[] mProjection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
    };

    public PickerFragment() {

    }

    public static PickerFragment newInstance(int type) {
        Bundle args = new Bundle();
        PickerFragment fragment = new PickerFragment();
        args.putInt(PICKER_TYPE_KEY, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPickerType = getArguments().getInt(PICKER_TYPE_KEY, PICK_ALL);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery_picker, container);
        vProgress = rootView.findViewById(R.id.progress);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        //// TODO: 8/30/16 setup recy

        loadContent();
        return rootView;
    }

    private AsyncTask<Void, Void, Void> mLoadGalleryTask;

    private void loadContent(){
        showProgress(true);


        if (mLoadGalleryTask != null) mLoadGalleryTask.cancel(true);

        mLoadGalleryTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

                if (mPickerType != PICK_IMAGE){
                    selection += " OR "
                            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                }

                Uri queryUri = MediaStore.Files.getContentUri("external");

                if (getContext() == null) return null;


                CursorLoader cursorLoader = new CursorLoader(
                        getContext(),
                        queryUri,
                        mProjection,
                        selection,
                        null, // Selection args (none).
                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
                );



                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (isCancelled()) return;
            }
        };


    }




    private void showProgress(boolean show){
        if (show){
            vRecyclerView.setVisibility(View.INVISIBLE);
            vProgress.setVisibility(View.VISIBLE);
        }else {
            vProgress.setVisibility(View.INVISIBLE);
            vRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
