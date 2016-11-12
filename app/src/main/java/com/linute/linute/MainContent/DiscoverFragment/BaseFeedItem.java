package com.linute.linute.MainContent.DiscoverFragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.linute.linute.MainContent.SendTo.SendToActivity;
import com.linute.linute.R;

/**
 * Created by QiFeng on 10/22/16.
 */

public class BaseFeedItem implements Parcelable{

    private static final String TAG = BaseFeedItem.class.getSimpleName();
    private String mId;         // id of post

    public BaseFeedItem(){
        mId = null;
    }
    public BaseFeedItem(String id){
        mId = id;
    }

    protected BaseFeedItem(Parcel in) {
        mId = in.readString();
    }

    public static final Creator<BaseFeedItem> CREATOR = new Creator<BaseFeedItem>() {
        @Override
        public BaseFeedItem createFromParcel(Parcel in) {
            return new BaseFeedItem(in);
        }

        @Override
        public BaseFeedItem[] newArray(int size) {
            return new BaseFeedItem[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
    }


    /**
     * static function to share a BaseFeedItem
     * used to keep share code in one place
     *
     * @param bfi BaseFeedItem to be shared
     * @param context
     */
    public static void share(final BaseFeedItem bfi, final Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_DENIED){
//            throw new IllegalArgumentException("This method needs WRITE_EXTERNAL_STORAGE permission to work");
            Log.e(TAG, BaseFeedItem.class.getSimpleName() + ".share() called without WRITE_EXTERNAL_STORAGE permission!!!");
            Log.e(TAG, "aborting share");
            return;
        }

        bfi.getShareUri(context, new BaseFeedItem.OnUriReadyListener() {
            @Override
            public void onUriReady(Uri uri) {
                final Intent sendIntent = new Intent(Intent.ACTION_SEND);
                //adds "Share over Messenger" option to choose
                final Intent taptShareIntent = new Intent(context, SendToActivity.class);
                taptShareIntent.putExtra(SendToActivity.EXTRA_POST_ID, bfi.getId());


                if(bfi instanceof Post && ((Post)bfi).getType() == Post.POST_TYPE_VIDEO){
                    //videos need to be scanned
                MediaScannerConnection.scanFile(context, new String[]{uri.toString()}, new String[]{"video/mp4"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        sendIntent.setType("video/mp4");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        Intent shareIntent = Intent.createChooser(sendIntent, "Share");
                        shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{new LabeledIntent(taptShareIntent, "com.linute.linute", "Messenger", R.mipmap.ic_launcher)});
                        context.startActivity(shareIntent);
                    }
                });
                }else{
                    sendIntent.setType("image/jpeg");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    Intent shareIntent = Intent.createChooser(sendIntent, "Share");
                    shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{new LabeledIntent(taptShareIntent, "com.linute.linute", "Messenger", R.mipmap.ic_launcher)});
                    context.startActivity(shareIntent);
                }
            }
            @Override
            public void onUriFail(Exception e) {
                Log.e(TAG, "getShareUri fail:",e);
            }
        });
    }



    /**
     * Attempts to save a file representing this BaseFeedItem (i.e. an image)
     * then calls the callback with a Uri pointing to the file
     * @param context
     * @param callback
     */
    public void getShareUri(Context context, OnUriReadyListener callback){}

    public interface OnUriReadyListener{
        public void onUriReady(Uri uri);
        public void onUriFail(Exception e);
    }
}
