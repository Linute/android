package com.linute.linute.MainContent.CreateContent;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;

import java.util.ArrayList;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = PickerFragment.class.getSimpleName();
    public static final String PICKER_TYPE_KEY = "picker_type_key";
    public static final int PICK_IMAGE = 0;
    public static final int PICK_VIDEO = 1;
    public static final int PICK_ALL = 2;

    public ArrayList<GalleryItem> mGalleryItems = new ArrayList<>();

    private int mPickerType;
    private View vProgress;
    private RecyclerView vRecyclerView;

    private static final int LOADER_ID = 0;


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
        View rootView = inflater.inflate(R.layout.fragment_gallery_picker, container, false);
        vProgress = rootView.findViewById(R.id.progress);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        vRecyclerView.setHasFixedSize(true);

        //grid layout with 3 items in each column
        vRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        vRecyclerView.addItemDecoration(new SpaceItemDecoration(4)); //add space decoration

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootView;
    }

    private void showProgress(boolean show) {
        if (show) {
            vRecyclerView.setVisibility(View.INVISIBLE);
            vProgress.setVisibility(View.VISIBLE);
        } else {
            vProgress.setVisibility(View.INVISIBLE);
            vRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader: started");
        if (id == LOADER_ID) {
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

            if (mPickerType != PICK_IMAGE) {
                selection += " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            }

            Uri queryUri = MediaStore.Files.getContentUri("external");

            if (getContext() == null) return null;

            return new CursorLoader(
                    getContext(),
                    queryUri,
                    mProjection,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        int idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int mediaIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);

        if (cursor.moveToFirst()) {
            do {
                mGalleryItems.add(
                        new GalleryItem(
                                cursor.getString(idIndex),
                                cursor.getString(pathIndex),
                                cursor.getType(mediaIndex)
                        )
                );
            } while (cursor.moveToNext());
        }
        for (GalleryItem item : mGalleryItems) {
            Log.i(TAG, "onLoadFinished: " + item.type + " " + item.path + " " + item.id);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(TAG, "onLoaderReset: cursor reset");
    }
}
