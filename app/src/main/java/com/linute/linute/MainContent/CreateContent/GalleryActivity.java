package com.linute.linute.MainContent.CreateContent;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.linute.linute.MainContent.EditScreen.Dimens;
import com.linute.linute.MainContent.EditScreen.EditFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.EditSavePhotoFragment;
import com.linute.linute.SquareCamera.EditSaveVideoFragment;
import com.linute.linute.UtilsAndHelpers.FileUtils;

import java.io.File;

/**
 * Created by mikhail on 8/20/16.
 */
public class GalleryActivity extends AppCompatActivity {
    public static final String TAG = GalleryActivity.class.getSimpleName();
    public static final int REQ_READ_EXT_STORAGE = 52;

    private int SELECT_IMAGE_OR_VID = 9;


    private int mGalleryType;
    private int mReturnType;
    public static final String ARG_GALLERY_TYPE = "gallery_type";
    public static final String ARG_RETURN_TYPE = "return_type";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mGalleryType = getIntent().getIntExtra(ARG_GALLERY_TYPE, CameraActivity.ALL);
        mReturnType = getIntent().getIntExtra(ARG_RETURN_TYPE, CameraActivity.RETURN_URI_AND_PRIVACY);
        if (hasReadPermission()) {
            getImageOrVideo();
        } else {
            getReadPermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (!hasReadPermission()) {
            finish();
            return;
        }
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_IMAGE_OR_VID) { //got an image
            Uri uri = data.getData();
            Log.i(TAG, "onActivityResult: "+uri.getPath());
            Log.i(TAG, "onActivityResult: "+uri);
            String type = FileUtils.getMimeType(this, uri);

            if (type != null) {
                //Log.i(TAG, "onActivityResult: "+uri.toString());
                if (type.startsWith("image")) { //selected image
                    try {
                        String path = FileUtils.getPath(this, uri);
                        Log.i(TAG, "onActivityResult: "+path);
                        if (path != null) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(path, options);
                            Dimens dimens = new Dimens(options.outWidth, options.outHeight, 0);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(
                                            R.id.fragment_container,
                                            EditFragment.newInstance(Uri.parse(path), EditFragment.ContentType.UploadedPhoto, mReturnType, dimens),
                                            EditFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                        }

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    return;
                } else if (type.startsWith("video")) { //selected video
                    try {
                        Uri path = Uri.parse(FileUtils.getPath(this, uri));

                        if (path != null) {
                            MediaMetadataRetriever info = new MediaMetadataRetriever();
                            info.setDataSource(this, uri);
                            long length = Long.parseLong(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                        Log.i(TAG, "onActivityResult: rotation "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
//                        Log.i(TAG, "onActivityResult: bitrate "+info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//                        Log.i(TAG, "onActivityResult: frame "+ info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));

                            if (length > 2500 && length < 15000) {
                                try {
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(
                                                    R.id.fragment_container,
                                                    EditFragment.newInstance(
                                                            path,
                                                            EditFragment.ContentType.UploadedVideo,
                                                            mReturnType,
                                                            new Dimens(
                                                                    Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
                                                                    Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)),
                                                                    Integer.parseInt(info.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
                                                            )
                                                    ),
                                                    EditFragment.TAG)
                                            .addToBackStack(null)
                                            .commit();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                return;
                            } else {
                                new AlertDialog.Builder(this)
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
                    } catch (IllegalArgumentException e) {
                        //trying to get video info of video that doesn't exist throws this exception
                        e.printStackTrace();
                    }
                }
            }
        }


        //didnt get image, so popbackstack
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_READ_EXT_STORAGE:
                if (hasReadPermission()) {
                    getImageOrVideo();
                } else {
                    finish();
                }
                break;
        }
    }

    private void getImageOrVideo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (mGalleryType == CameraActivity.IMAGE) {
            intent.setType("image/*");
            if (Build.VERSION.SDK_INT >= 19) {
                String[] mimetypes = {"image/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            }

            startActivityForResult(Intent.createChooser(intent,
                    "Select image"), SELECT_IMAGE_OR_VID);
        } else {
            intent.setType("image/* video/*");
            if (Build.VERSION.SDK_INT >= 19) {
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            }
            startActivityForResult(Intent.createChooser(intent,
                    "Select image or video"), SELECT_IMAGE_OR_VID);
        }
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasReadPermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void getReadPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ_EXT_STORAGE);
    }
}
