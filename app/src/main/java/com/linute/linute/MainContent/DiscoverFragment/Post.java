package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.ImageUtility;
import com.linute.linute.UtilsAndHelpers.JsonHelpers;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arman on 12/27/15.
 */
public class Post extends BaseFeedItem implements Parcelable {

    public final static int POST_TYPE_STATUS = 0;
    public final static int POST_TYPE_IMAGE = 1;
    public final static int POST_TYPE_VIDEO = 2;
    private static final String TAG = "Post";

    private String mUserId;         // id of post owner
    private String mUserName;       // post owner's full name
    private String mUserImage;      // post owner's profile image
    private String mCollegeName;    // OP's college
    private String mAnonImage;      // anon image of user

    private String mVideoURL = "";  // video url

    private String mTitle;          // text on image or status

    private String mImage = "";     // image url
    private PostSize mImageSize;

    private int mPrivacy;           // 1 for anon, 0 for public
    private int mNumLikes;          // num likes on post
    private long mPostTime;         // post time. millisec since 1970
    private int mNumOfComments;     // num of comments

    private boolean mPostLiked;     //did viewer like the image
    private boolean mPostHidden;    //post hidden from user
    private boolean mPostMuted;     //post muted from user

    private boolean mIsDeleted;     //post has been deleted

    private boolean mCommentAnonDisabled;

    private int mType;
    public String imageBase64;
    private boolean isPrivacyChanged;

    //private ArrayList<Object> mComments = new ArrayList<>();

    public Post() {
        setPrivacyChanged(false);
    }

    public Post(String imageurl, String postid, String userid, String userName) {
        super(postid);
        mImage = imageurl;
        mPostTime = 0;
        mUserId = userid;
        mUserName = Utils.stripUnsupportedCharacters(userName);
        mUserImage = "";
        mTitle = "";
        mPrivacy = 0;
        mCommentAnonDisabled = true;
        mAnonImage = "";
        mNumLikes = 0;
        mNumOfComments = 0;
        mCollegeName = "";
        mPostLiked = false;
        mPostHidden = false;
        mPostMuted = false;
        mIsDeleted = false;
        setPrivacyChanged(false);
    }


    public Post(String postId) {
        super(postId);
        setPrivacyChanged(false);
        mCommentAnonDisabled = true;
    }

    /**
     * @param jsonObject - post json object
     */
    public Post(JSONObject jsonObject) throws JSONException {

        //Log.i("test", "Post: "+jsonObject.toString(4));
        super(jsonObject.getString("id"));
        mType = jsonObject.getInt("type");

        if (jsonObject.getJSONArray("images").length() > 0)
            mImage = Utils.getEventImageURL(jsonObject.getJSONArray("images").getString(0));

        if (mType == POST_TYPE_VIDEO && jsonObject.getJSONArray("videos").length() > 0)
            mVideoURL = Utils.getVideoURL(jsonObject.getJSONArray("videos").getString(0));

        Date myDate;

        try {
            myDate = Utils.getDateFormat().parse(jsonObject.getString("date"));
        } catch (ParseException w) {
            w.printStackTrace();
            myDate = null;
        }

        mPostTime = (myDate == null ? 0 : myDate.getTime());

        try {
            JSONObject owner = jsonObject.getJSONObject("owner");

            mUserId = owner.getString("id");
            mUserName = Utils.stripUnsupportedCharacters(owner.getString("fullName"));
            mUserImage = Utils.getImageUrlOfUser(owner.getString("profileImage"));

        } catch (JSONException e) {
            mUserId = jsonObject.getString("owner");
            mUserName = "";
            mUserImage = "";
        }

        try{
            mCollegeName = jsonObject.getJSONObject("college").getString("name");
        }catch (JSONException eq){
            mCollegeName = "";
        }

        mTitle = jsonObject.getString("title");
        mPrivacy = jsonObject.getInt("privacy");

        mCommentAnonDisabled = jsonObject.getBoolean("isAnonymousCommentsDisabled");

        String anonImage = jsonObject.getString("anonymousImage");
        mAnonImage = anonImage == null || anonImage.equals("") ? "" : Utils.getAnonImageUrl(anonImage);

        try {
            mNumLikes = jsonObject.getInt("numberOfLikes");
            mNumOfComments = jsonObject.getInt("numberOfComments");
            mPostLiked = jsonObject.getBoolean("isLiked");
        } catch (JSONException e) {
            mNumLikes = 0;
            mNumOfComments = 0;
            mPostLiked = false;
        }

        try {
            JSONObject size = jsonObject.getJSONObject("imageSizes");
            mImageSize = new PostSize(size.getInt("width"), size.getInt("height"));
        } catch (JSONException e) {
            //e.printStackTrace();
            mImageSize = null;
        }

        mPostHidden = JsonHelpers.getBoolean(jsonObject, "isHidden");
        mPostMuted = JsonHelpers.getBoolean(jsonObject, "isMuted");
        mIsDeleted = JsonHelpers.getBoolean(jsonObject, "isDeleted");

        setPrivacyChanged(JsonHelpers.getBoolean(jsonObject, "isPrivacyChanged"));

        if(jsonObject.has("preloaders") && jsonObject.getJSONArray("preloaders").length() > 0)
            imageBase64 = jsonObject.getJSONArray("preloaders").getString(0);

//        JSONArray comments = jsonObject.getJSONArray("comments");
//        for (int i = 0; i < comments.length(); i++) {
//            try {
//                mComments.add(new Comment(comments.getJSONObject(i)));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }



    public void updateInfo(JSONObject jsonObject) throws JSONException {
        setId(jsonObject.getString("id"));

        mType = jsonObject.getInt("type");

        if (jsonObject.getJSONArray("images").length() > 0)
            mImage = Utils.getEventImageURL(jsonObject.getJSONArray("images").getString(0));

        if (mType == POST_TYPE_VIDEO && jsonObject.getJSONArray("videos").length() > 0)
            mVideoURL = Utils.getVideoURL(jsonObject.getJSONArray("videos").getString(0));

        Date myDate;

        try {
            myDate = Utils.getDateFormat().parse(jsonObject.getString("date"));
        } catch (ParseException w) {
            w.printStackTrace();
            myDate = null;
        }

        mPostTime = (myDate == null ? 0 : myDate.getTime());


        if (mCollegeName == null || mCollegeName.isEmpty()) {
            try {
                mCollegeName = jsonObject.getJSONObject("college").getString("name");
            } catch (JSONException e) {
                mCollegeName = "";
            }
        }

        try {
            JSONObject owner = jsonObject.getJSONObject("owner");
            mUserId = owner.getString("id");
            mUserName = owner.getString("fullName");
            mUserImage = Utils.getImageUrlOfUser(owner.getString("profileImage"));
        } catch (JSONException e) {
            mUserId = jsonObject.getString("owner");
            mUserName = "";
            mUserImage = "";
        }

        mTitle = jsonObject.getString("title");
        mPrivacy = jsonObject.getInt("privacy");

        mCommentAnonDisabled = jsonObject.getBoolean("isAnonymousCommentsDisabled");

        String anonImage = jsonObject.getString("anonymousImage");
        mAnonImage = anonImage == null || anonImage.equals("") ? "" : Utils.getAnonImageUrl(anonImage);

        try {
            mNumLikes = jsonObject.getInt("numberOfLikes");
            mNumOfComments = jsonObject.getInt("numberOfComments");
            mPostLiked = jsonObject.getBoolean("isLiked");
        } catch (JSONException e) {
            mNumLikes = 0;
            mNumOfComments = 0;
            mPostLiked = false;
        }

        mPostHidden = JsonHelpers.getBoolean(jsonObject, "isHidden");
        mPostMuted = JsonHelpers.getBoolean(jsonObject, "isMuted");
        mIsDeleted = JsonHelpers.getBoolean(jsonObject, "isDeleted");

        try {
            JSONObject size = jsonObject.getJSONObject("imageSizes");
            mImageSize = new PostSize(size.getInt("width"), size.getInt("height"));
        } catch (JSONException e) {
            e.printStackTrace();
            mImageSize = null;
        }
    }

    public PostSize getImageSize() {
        return mImageSize;
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public String getNumLike() {
        return mNumLikes + "";
    }

    public void setNumLike(int numLike) {
        mNumLikes = numLike;
    }

    public String getUserName() {
        return mPrivacy == 1 ? "Anonymous" : mUserName;
    }

    public String getUserImage() {
        return mPrivacy == 1 ? mAnonImage : mUserImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getImage() {
        return mImage;
    }

    public int getPrivacy() {
        return mPrivacy;
    }

    public boolean isPostLiked() {
        return mPostLiked;
    }

    public void setPostLiked(boolean postLiked) {
        mPostLiked = postLiked;
    }

    public String getPostTime() {
        return Utils.getTimeAgoString(mPostTime);
    }

    public String getUserId() {
        return mUserId;
    }

    public boolean isImagePost() {
        return mImage != null && !mImage.equals("");
    }

    public boolean isVideoPost() {
        return mVideoURL != null && !mVideoURL.equals("");
    }

    public int getType() {
        return mType;
    }

    public String getVideoUrl() {
        return mVideoURL;
    }

    public int getNumOfComments() {
        return mNumOfComments;
    }

    public void setNumOfComments(int comments) {
        mNumOfComments = comments;
    }

    public String getAnonImage() {
        return mAnonImage;
    }

    @Override
    public String toString() {
        return getImage().equals("") ? getTitle() : "Content: Image - " + getTitle();
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setProfileImage(String profileImage) {
        mUserImage = profileImage;
    }

    public void setPostPrivacy(int privacy) {
        mPrivacy = privacy;
    }

    public void setAnonImage(String anonImage) {
        mAnonImage = anonImage;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setUserImage(String userImage) {
        mUserImage = userImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public void setType(int type) {
        mType = type;
    }

    public boolean isPostHidden() {
        return mPostHidden;
    }

    public void setPostHidden(boolean postHidden) {
        mPostHidden = postHidden;
    }

    public boolean isPostMuted() {
        return mPostMuted;
    }

    public void setPostMuted(boolean postMuted) {
        mPostMuted = postMuted;
    }

    public boolean isCommentAnonDisabled() {
        return mCommentAnonDisabled;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Post && ((Post) o).getId().equals(getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

//    public ArrayList<Object> getComments() {
//        return mComments;
//    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mUserId);
        dest.writeString(mUserName);
        dest.writeString(mUserImage);
        dest.writeString(mTitle);
        dest.writeString(mImage);
        dest.writeInt(mPrivacy);
        dest.writeInt(mNumLikes);
        dest.writeLong(mPostTime);
        dest.writeInt(mNumOfComments);
        dest.writeString(mAnonImage);
        dest.writeByte((byte) (mPostLiked ? 1 : 0)); //boolean
        dest.writeString(mVideoURL);
        dest.writeString(mCollegeName);

        dest.writeParcelable(mImageSize, 0);
        dest.writeByte((byte) (mIsDeleted ? 1 : 0));
        dest.writeByte((byte) (isPrivacyChanged() ? 1 : 0));
        dest.writeString(imageBase64);
        dest.writeInt(mType);
        //dest.writeList(mComments);
    }

    private Post(Parcel in) {
        super(in);
        mUserId = in.readString();
        mUserName = in.readString();
        mUserImage = in.readString();
        mTitle = in.readString();
        mImage = in.readString();
        mPrivacy = in.readInt();
        mNumLikes = in.readInt();
        mPostTime = in.readLong();
        mNumOfComments = in.readInt();
        mAnonImage = in.readString();
        mPostLiked = in.readByte() != 0; //true if byte != 0
        mVideoURL = in.readString();
        mCollegeName = in.readString();

        mImageSize = in.readParcelable(PostSize.class.getClassLoader());
        mIsDeleted = in.readByte() != 0;
        setPrivacyChanged(in.readByte()!=0);
        imageBase64 = in.readString();
        mType = in.readInt();
       // mComments = new ArrayList<>();
        //in.readList(mComments, Object.class.getClassLoader());
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public boolean isPrivacyChanged() {
        return isPrivacyChanged;
    }

    public void setPrivacyChanged(boolean privacyChanged) {
        isPrivacyChanged = privacyChanged;
    }


    /**
     * Attempts to save a file representing this BaseFeedItem (and image or a video)
     * then calls the callback with a Uri pointing to the file
     * @param mContext
     * @param listener callback to which Uri is passed
     */
    @Override
    public void getShareUri(final Context mContext, final OnUriReadyListener listener) {

        if(listener == null){throw new IllegalArgumentException("listener can not be null");}

        switch (mType) {
            case POST_TYPE_IMAGE:
            case POST_TYPE_STATUS:
                shareImagePost(mContext, listener);
                break;
            case POST_TYPE_VIDEO:
                shareVideoPost(mContext, listener);
                break;
        }
    }

    private void shareImagePost(final Context mContext, final OnUriReadyListener listener) {
        listener.onUriProgress(10);

        Glide.with(mContext).load(getImage()).asBitmap().into(new SimpleTarget<Bitmap>(){
            @Override
            public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                Glide.with(mContext).load(getUserImage()).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap profileImage, GlideAnimation<? super Bitmap> glideAnimation) {
                        listener.onUriProgress(50);

                        View header = setupShareHeader(profileImage, mContext);

                        int bitmapHeight = (int) ((float) header.getWidth() / resource.getWidth() * resource.getHeight());
                        Bitmap returnedBitmap = Bitmap.createBitmap(header.getWidth(), header.getHeight() + bitmapHeight, Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(returnedBitmap);
                        Drawable bgDrawable = header.getBackground();
                        if (bgDrawable != null)
                            bgDrawable.draw(canvas);
                        else
                            canvas.drawColor(Color.WHITE);
                        header.draw(canvas);

                        listener.onUriProgress(75);


//                        int savecount = canvas.save();
//                        canvas.translate(0, header.getHeight());
                        Rect src = new Rect(0, 0, resource.getWidth(), resource.getHeight());

                        Rect dest = new Rect(0, header.getHeight(), canvas.getWidth(), canvas.getHeight());
                        canvas.drawBitmap(resource, src, dest, null);
//                        canvas.restoreToCount(savecount);

                        Uri uri = ImageUtility.savePicture(mContext, returnedBitmap);
//                        Log.i(TAG, uri.getPath());
                        returnedBitmap.recycle();
                        //for some reason Glide seems to reuse these bitmap references
//                        resource.recycle();
//                        profileImage.recycle();
                        listener.onUriReady(uri);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        listener.onUriFail(e);
                    }
                });
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                listener.onUriFail(e);
            }
        });
    }

    @NonNull
    private View setupShareHeader(Bitmap profileImage, Context mContext) {
        View header = LayoutInflater.from(mContext).inflate(R.layout.share_name_header, null, false);
        ((ImageView) header.findViewById(R.id.feedDetail_profile_image)).setImageBitmap(profileImage);
        ((TextView) header.findViewById(R.id.feedDetail_user_name)).setText(getUserName());
        ((TextView) header.findViewById(R.id.college_name)).setText(getCollegeName());
//        View watermark = header.findViewById(R.id.image_watermark);

        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        header.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        header.layout(0, 0, width, header.getMeasuredHeight());

        return header;
    }

    private void shareVideoPost(final Context mContext, final OnUriReadyListener listener) {
        //todo get an ffmpeg instance and overlay a post header


        final Uri videoUri = Uri.parse(getVideoUrl());


        final String videoInput = getVideoUrl();
        final String outputFile = ImageUtility.getVideoUri();

        Glide.with(mContext).load(getUserImage()).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap profileImage, GlideAnimation<? super Bitmap> glideAnimation) {
                //todo don't make an entire holder, inflate the header and draw that alone, then the bitmap

                View header = setupShareHeader(profileImage, mContext);





                Bitmap returnedBitmap = Bitmap.createBitmap(header.getWidth(), header.getHeight(), Bitmap.Config.ARGB_8888);

                final Canvas canvas = new Canvas(returnedBitmap);
                canvas.drawColor(Color.WHITE);
                header.draw(canvas);


                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getVideoUrl(), API_Methods.getMainHeader(new LSDKUser(mContext).getToken()));
                int vidWidth = retriever.getFrameAtTime(0).getWidth();
                int vidHeight = retriever.getFrameAtTime(0).getHeight();
                final int totalFrames = (int)(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000.0 * 24);

                int headWidth = vidWidth;
                int headHeight = (int)((float)vidWidth/returnedBitmap.getWidth() * returnedBitmap.getHeight());

//                Log.i("ffmpeg", Arrays.toString(new int[]{vidWidth, vidHeight, headWidth, headHeight}));

                final String headerInput = ImageUtility.savePicture(mContext, returnedBitmap).getPath();

                final String[] cmds = new String[]{
//                        "-s",String.format(Locale.US, "%dx%d",  vidWidth, vidHeight),
                        "-i", videoInput,
                        "-i", headerInput,
                        "-filter_complex",
                        String.format(Locale.US, "[1:v]scale=%d:%d[header];",headWidth, headHeight)+
                                String.format(Locale.US, "[0:v]pad=iw:ih+%d:0:%d[vid];",headHeight,headHeight)+
                        "[vid][header]overlay[out]",
//                        String.format(Locale.US, "[out]scale=%d:%d[out1]", vidWidth, headHeight+vidHeight), //newWidth, newHeight));

                        "-map", "[out]",
//                        "-s",String.format(Locale.US, "%dx%d",  vidWidth, headHeight+vidHeight),
                        "-r","24",
                        "-preset",
                        "superfast", //ultrafast
                        "-c:a",
                        "copy",
                        outputFile


                };
               /* String[] cmds = new String[]{
//                        "-i", videoInput,
                        "-i", headerInput,
//                        "-filter_complex", "'[0:v][1:v]overlay[out]'",
                        "-r","24",outputFile
                };*/
                final FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
                try {
                    ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                        @Override
                        public void onFailure() {

                        }

                        @Override
                        public void onSuccess() {
                            try {
                                ffmpeg.execute(cmds, new FFmpegExecuteResponseHandler() {
                                    @Override
                                    public void onSuccess(String message) {
//                                        Log.i("ffmpeg", message);
                                        listener.onUriReady(Uri.parse(outputFile));
//                                        listener.onUriFail(new Exception("hi"));
                                    }

                                    @Override
                                    public void onProgress(String message) {
                                        Log.i("ffmpeg", message);
                                        int frames = message.indexOf("frame=") + "frame=".length();
                                        if(frames == "frame=".length()-1){return;}
                                        String currentFrame = message.substring(frames,
                                                message.indexOf("fps="));
                                        int currentFrameIndex = Integer.parseInt(currentFrame.trim());
                                        listener.onUriProgress((int)(100.0*currentFrameIndex/totalFrames));
                                    }

                                    @Override
                                    public void onFailure(String message) {
//                                        Log.i("ffmpeg", message);
                                    }

                                    @Override
                                    public void onStart() {
                                        Log.i("ffmpeg", "start");
                                    }

                                    @Override
                                    public void onFinish() {
//                                        Log.i("ffmpeg", "fin");
                                        new File(headerInput).delete();
                                    }
                                });
                            } catch (FFmpegCommandAlreadyRunningException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                }catch(FFmpegNotSupportedException e){
                    e.printStackTrace();
                }


            }
        });
    }
}
