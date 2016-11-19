package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;
import android.util.Log;

import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.Global.GlobalChoiceItem;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by mikhail on 10/25/16.
 */

public class Article extends GlobalChoiceItem{

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();

    public final String author;
    public final String date;
    public final ArrayList<ArticleElement> elements = new ArrayList<>();

    private Post mPost;

    public boolean mIsPostLiked = false;

    private int mNumberOfViews;
    private int mNumberOfShares;

    public Article(JSONObject json) throws JSONException{
        super(json.getString("title"), json.getString("description"), json.getJSONArray("images").getString(0), json.getString("id"), TYPE_ARTICLE);


        JSONArray elementsJson = json.getJSONArray("content");
        for(int i=0;i<elementsJson.length();i++){
            elements.add(new ArticleElement(elementsJson.getJSONObject(i)));
        }

        JSONObject event = json.getJSONObject("event");
        Log.d("AAA", event.toString(4));

        mPost = new Post();
        mPost.setId(event.getString("id"));
        mPost.setNumLike(event.getInt("numberOfLikes"));
        mPost.setNumOfComments(event.getInt("numberOfComments"));
        mPost.setPostLiked(event.getBoolean("isLiked"));

//        mPostId = event.getString("id");
        setNumberOfViews(event.getInt("numberOfImpressions"));
        setNumberOfShares(event.getInt("numberOfShares"));

        author = json.getString("author");
        String tDate;
        try {
            tDate = DATE_FORMAT.format(Utils.getDateFormat().parse(json.getString("date")).getTime());
        }catch (ParseException e){
            tDate = "";
            e.printStackTrace();
        }
        date = tDate;
    }

    public Post getPost(){
        return mPost;
    }

    public boolean isPostLiked(){
        return mPost.isPostLiked();
    }

    public void setPostLiked(boolean isLiked){
        mPost.setPostLiked(isLiked);
    }

    public boolean hasComments(){
        return mPost.getNumOfComments() > 0;
    }

    public String getPostId(){
        return mPost.getId();
    }

    public Article(Parcel in) {
        super(in);
        mPost = in.readParcelable(Post.class.getClassLoader());
        in.readList(elements, ArticleElement.class.getClassLoader());
        author = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mPost, 0);
        dest.writeList(elements);
        dest.writeString(author);
        dest.writeString(date);
    }

    public int getNumberOfComments() {
        return mPost.getNumOfComments();
    }

    public void setNumberOfComments(int mNumberOfComments) {
       mPost.setNumOfComments(mNumberOfComments);
    }

    public void incrementLikes(){
        mPost.incrementLikes();
    }

    public void decrementLikes(){
        mPost.decrementLikes();
    }

    public String getNumberOfLikes() {
        return mPost.getNumLike();
    }

    public int getNumberOfViews() {
        return mNumberOfViews;
    }

    public void setNumberOfViews(int mNumberOfViews) {
        this.mNumberOfViews = mNumberOfViews;
    }

    public int getNumberOfShares() {
        return mNumberOfShares;
    }

    public void setNumberOfShares(int mNumberOfShares) {
        this.mNumberOfShares = mNumberOfShares;
    }
}
