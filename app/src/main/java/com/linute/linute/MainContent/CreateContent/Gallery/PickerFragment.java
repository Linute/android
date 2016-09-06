package com.linute.linute.MainContent.CreateContent.Gallery;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.MainContent.EditScreen.Dimens;
import com.linute.linute.MainContent.EditScreen.EditFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.GridSpacingItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import static com.linute.linute.SquareCamera.CameraActivity.RETURN_URI;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PickerAdapter.GalleryItemSelected, AdapterView.OnItemSelectedListener {

    public static final String TAG = PickerFragment.class.getSimpleName();
    public static final String PICKER_TYPE_KEY = "picker_type_key";
    public static final String RETURN_TYPE_KEY = "return_type";
    public static final String KEY_CONTENT_SUBTYPE = "return_type";
    public static final int PICK_IMAGE = 0;
    public static final int PICK_VIDEO = 1;
    public static final int PICK_ALL = 2;

    public ArrayList<GalleryItem> mUnfilteredGalleryItems = new ArrayList<>();
    public ArrayList<GalleryItem> mFiltedGalleryItems = new ArrayList<>();
    public ArrayList<BucketItem> mBucketList = new ArrayList<>(); //list of directories

    private int mPickerType;
    private View vProgress;
    private RecyclerView vRecyclerView;

    private PickerAdapter mPickerAdapter;

    private Handler mHandler = new Handler();

    private static final int LOADER_ID = 0;

    final String[] mProjection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
    };

    private int mReturnType;

    private AlertDialog mAlertDialog;
    private ArrayAdapter mSpinnerAdapter;
    private AppCompatSpinner vSpinner;
    private EditFragment.ContentSubType mContentSubType;

    public PickerFragment() {

    }

    public static PickerFragment newInstance(int type, int returntype, EditFragment.ContentSubType contentSubType) {
        Bundle args = new Bundle();
        PickerFragment fragment = new PickerFragment();
        args.putInt(PICKER_TYPE_KEY, type);
        args.putInt(RETURN_TYPE_KEY, returntype);
        args.putSerializable(KEY_CONTENT_SUBTYPE, contentSubType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPickerType = getArguments().getInt(PICKER_TYPE_KEY, PICK_ALL);
            mReturnType = getArguments().getInt(RETURN_TYPE_KEY, RETURN_URI);
            mContentSubType = (EditFragment.ContentSubType)getArguments().getSerializable(KEY_CONTENT_SUBTYPE);
            if(mContentSubType == null){
                mContentSubType = EditFragment.ContentSubType.None;
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery_picker, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });


        mBucketList.add(new BucketItem("", "Gallery"));
        vSpinner = (AppCompatSpinner) toolbar.findViewById(R.id.spinner);
        vSpinner.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.pure_white), PorterDuff.Mode.SRC_ATOP);
        vSpinner.setOnItemSelectedListener(this);

        mSpinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_text, mBucketList);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        vSpinner.setAdapter(mSpinnerAdapter);

        vProgress = rootView.findViewById(R.id.progress);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        vRecyclerView.setHasFixedSize(true);

        //grid layout with 3 items in each column
        vRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        vRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 4, false)); //add space decoration

        mPickerAdapter = new PickerAdapter(mFiltedGalleryItems);
        mPickerAdapter.setRequestManager(Glide.with(this));
        mPickerAdapter.setGalleryItemSelected(this);

        vRecyclerView.setAdapter(mPickerAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    private void showProgress(boolean show) {
        if (show) {
            vRecyclerView.setVisibility(View.INVISIBLE);
            vProgress.setVisibility(View.VISIBLE);
            vSpinner.setClickable(false);
        } else {
            vProgress.setVisibility(View.INVISIBLE);
            vRecyclerView.setVisibility(View.VISIBLE);
            vSpinner.setClickable(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        showProgress(true);

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
        int bucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        int bucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);


        final HashSet<BucketItem> bucketItems = new HashSet<>();

        mUnfilteredGalleryItems.clear();

        if (cursor.moveToFirst()) {
            do {
                String buckId = cursor.getString(bucketId);
                mUnfilteredGalleryItems.add(
                        new GalleryItem(
                                cursor.getString(idIndex),
                                cursor.getString(pathIndex),
                                cursor.getInt(mediaIndex),
                                buckId
                        )
                );

                bucketItems.add(new BucketItem(buckId, cursor.getString(bucketName)));

            } while (cursor.moveToNext());
        }

        mBucketList.clear();
        mBucketList.add(new BucketItem("", "Gallery")); //option for all images
        mBucketList.addAll(bucketItems);
        mSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getLoaderManager().destroyLoader(LOADER_ID);

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mPickerAdapter.getRequestManager() != null)
            mPickerAdapter.getRequestManager().onDestroy();
    }


    @Override
    public void itemClicked(GalleryItem item) {
        File file = new File(item.path);
        if (file.exists()) {
            if (item.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                goToVideoEdit(item);
            } else if (item.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                goToImageEdit(item);
            }
        } else if (getContext() != null)
            Toast.makeText(getContext(), "This file could not be opended", Toast.LENGTH_SHORT).show();
    }


    private void goToVideoEdit(GalleryItem item) {
        if (getContext() == null) return;

        try {
            Uri path = Uri.parse(item.path);

            MediaMetadataRetriever info = new MediaMetadataRetriever();
            info.setDataSource(getContext(), path);
            long length = Long.parseLong(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                        Log.i(TAG, "onActivityResult: rotation "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
//                        Log.i(TAG, "onActivityResult: bitrate "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//                        Log.i(TAG, "onActivityResult: frame "+ info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));

            if (length > 1750 && length < 15000) {
                mContentSubType = EditFragment.ContentSubType.None;
                goToFragment(EditFragment.newInstance(
                        path,
                        EditFragment.ContentType.UploadedVideo,
                        mContentSubType,
                        mReturnType,
                        new Dimens(
                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)),
                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
                        )), EditFragment.TAG
                );
            } else {
                mAlertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Video too short or long")
                        .setMessage("Sorry, videos must be longer than 2 seconds and shorter than 15 seconds")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void goToImageEdit(GalleryItem item) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(item.path, options);
        Dimens dimens = new Dimens(options.outWidth, options.outHeight, 0);


        goToFragment(
                EditFragment.newInstance(Uri.parse(item.path), EditFragment.ContentType.UploadedPhoto, mContentSubType, mReturnType, dimens),
                EditFragment.TAG
        );
    }

    private void goToFragment(Fragment fragment, String tag) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemSelected: " + position);
        showProgress(true);
        String filter = mBucketList.get(position).id;
        final ArrayList<GalleryItem> temp;

        if (filter.isEmpty()) {
            temp = mUnfilteredGalleryItems;
        } else {
            temp = new ArrayList<>();
            for (GalleryItem item : mUnfilteredGalleryItems)
                if (item.bucketId.equals(filter))
                    temp.add(item);
        }

        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFiltedGalleryItems.clear();
                mFiltedGalleryItems.addAll(temp);
                mPickerAdapter.notifyDataSetChanged();
                showProgress(false);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
