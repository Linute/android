package com.linute.linute.SquareCamera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;



public class GalleryFragment extends Fragment {
    public static final String TAG = GalleryFragment.class.getSimpleName();

    private int SELECT_IMAGE_OR_VID = 9;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasCameraAndWritePermission()) {
            getImageOrVideo();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (!hasCameraAndWritePermission()) { //pop if we don't have permissions
            getFragmentManager().popBackStack();
            return;
        }

        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_IMAGE_OR_VID) { //got an image
            Uri uri = data.getData();
            if (uri.toString().contains("image")) { //selected image
                if (getActivity() != null) {
                    try {
                        getFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.fragment_container,
                                        EditSavePhotoFragment.newInstance(uri, true),
                                        EditSavePhotoFragment.TAG)
                                .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                                .commit();

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    return;
                }

            } else if (data.getData().toString().contains("video")) { //selected video
                try {
                    if (getActivity() == null) return;
                    Uri path = Uri.parse(getPath(getActivity(), uri));


                    if (path != null) {
                        MediaMetadataRetriever info = new MediaMetadataRetriever();
                        info.setDataSource(getContext(), uri);
                        long length = Long.parseLong(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                        Log.i(TAG, "onActivityResult: rotation "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
//                        Log.i(TAG, "onActivityResult: bitrate "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//                        Log.i(TAG, "onActivityResult: frame "+ info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));

                        if (length > 3000 && length < 15500) {
                            try {
                                getFragmentManager()
                                        .beginTransaction()
                                        .replace(
                                                R.id.fragment_container,
                                                EditSaveVideoFragment.newInstance(
                                                        path,
                                                        new EditSaveVideoFragment.VideoDimen(
                                                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
                                                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)),
                                                                Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
                                                        ),
                                                        true
                                                ),
                                                EditSaveVideoFragment.TAG)
                                        .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                                        .commit();

                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Video too short or long")
                                    .setMessage("Sorry, videos must be longer than 3 seconds and shorter than 15 seconds")
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }

                        info.release();
                    }
                }catch (IllegalArgumentException e){
                    //trying to get video info of video that doesn't exist throws this exception
                    e.printStackTrace();
                }
            }
        }

        //didnt get image, so popbackstack
        getFragmentManager().popBackStack();

    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void getImageOrVideo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        if (Build.VERSION.SDK_INT >= 19) {
            String[] mimetypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        }
        startActivityForResult(Intent.createChooser(intent,
                "Select image or video"), SELECT_IMAGE_OR_VID);
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.RECORD_AUDIO);
    }
}
