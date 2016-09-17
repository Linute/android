package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

import com.linute.linute.UtilsAndHelpers.JsonHelpers;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Arman on 12/27/15.
 */
public class Post implements Parcelable {

    public final static int POST_TYPE_STATUS = 0;
    public final static int POST_TYPE_IMAGE = 1;
    public final static int POST_TYPE_VIDEO = 2;

    private String mUserId;         // id of post owner
    private String mUserName;       // post owner's full name
    private String mUserImage;      // post owner's profile image
    private String mCollegeName;    // OP's college
    private String mAnonImage;      // anon image of user

    private String mPostId;         // id of post

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

    public Post() {

    }

    public Post(String imageurl, String postid, String userid, String userName){
        mImage  = imageurl;
        mPostId = postid;
        mPostTime = 0;
        mUserId = userid;
        mUserName = userName;
        mUserImage = "";
        mTitle="";
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
    }


    public Post(String postId){
        mPostId = postId;
    }

    /**
     * @param jsonObject  - post json object
     */
    public Post(JSONObject jsonObject) throws JSONException {

        //Log.i("test", "Post: "+jsonObject.toString(4));
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
            mUserName = owner.getString("fullName");
            mUserImage = Utils.getImageUrlOfUser(owner.getString("profileImage"));

            try {
                mCollegeName = owner.getJSONObject("college").getString("name");
            }catch (JSONException e){
                mCollegeName = "";
            }
        } catch (JSONException e) {
            mUserId = jsonObject.getString("owner");
            mUserName = "";
            mUserImage = "";
        }


        mTitle = jsonObject.getString("title");
        mPrivacy = jsonObject.getInt("privacy");

        mCommentAnonDisabled = jsonObject.getBoolean("isAnonymousCommentsDisabled");
        mPostId = jsonObject.getString("id");

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
        }catch (JSONException e){
            //e.printStackTrace();
            mImageSize = null;
        }

        mPostHidden = JsonHelpers.getBoolean(jsonObject, "isHidden");
        mPostMuted = JsonHelpers.getBoolean(jsonObject, "isMuted");
        mIsDeleted = JsonHelpers.getBoolean(jsonObject, "isDeleted");
    }


    public void updateInfo(JSONObject jsonObject) throws JSONException{
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
            mUserName = owner.getString("fullName");
            mUserImage = Utils.getImageUrlOfUser(owner.getString("profileImage"));

            if (mCollegeName == null || mCollegeName.isEmpty()) {
                try {
                    mCollegeName = owner.getJSONObject("college").getString("name");
                } catch (JSONException e) {
                    mCollegeName = "";
                }
            }
        } catch (JSONException e) {
            mUserId = jsonObject.getString("owner");
            mUserName = "";
            mUserImage = "";
        }

        mTitle = jsonObject.getString("title");
        mPrivacy = jsonObject.getInt("privacy");

        mCommentAnonDisabled = jsonObject.getBoolean("isAnonymousCommentsDisabled");
        mPostId = jsonObject.getString("id");

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
        }catch (JSONException e){
            e.printStackTrace();
            mImageSize = null;
        }
    }

    public PostSize getImageSize() {
        return mImageSize;
    }

    public void setImageSize(PostSize imageSize) {
        mImageSize = imageSize;
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
        return mUserName;
    }

    public String getUserImage() {
        return mUserImage;
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

    public String getPostId() {
        return mPostId;
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

    public void setUserName(String name) {
        mUserName = name;
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

    public void setVideoURL(String videoURL) {
        mVideoURL = videoURL;
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
        return mPostId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Post && ((Post) o).getPostId().equals(mPostId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mUserName);
        dest.writeString(mUserImage);
        dest.writeString(mTitle);
        dest.writeString(mImage);
        dest.writeInt(mPrivacy);
        dest.writeInt(mNumLikes);
        dest.writeLong(mPostTime);
        dest.writeString(mPostId);
        dest.writeInt(mNumOfComments);
        dest.writeString(mAnonImage);
        dest.writeByte((byte) (mPostLiked ? 1 : 0)); //boolean
        dest.writeString(mVideoURL);
        dest.writeString(mCollegeName);

        dest.writeParcelable(mImageSize, 0);
        dest.writeByte((byte) (mIsDeleted ? 1 : 0));
    }

    private Post(Parcel in) {
        mUserId = in.readString();
        mUserName = in.readString();
        mUserImage = in.readString();
        mTitle = in.readString();
        mImage = in.readString();
        mPrivacy = in.readInt();
        mNumLikes = in.readInt();
        mPostTime = in.readLong();
        mPostId = in.readString();
        mNumOfComments = in.readInt();
        mAnonImage = in.readString();
        mPostLiked = in.readByte() != 0; //true if byte != 0
        mVideoURL = in.readString();
        mCollegeName = in.readString();

        mImageSize = in.readParcelable(PostSize.class.getClassLoader());
        mIsDeleted = in.readByte() != 0;
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
}
