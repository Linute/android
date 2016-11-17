package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;
import android.util.Log;

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

    private final String mPostId;

    public boolean mIsPostLiked = false;

    private int mNumberOfComments;
    private int mNumberOfLikes;
    private int mNumberOfViews;
    private int mNumberOfShares;
    private JSONObject event;

    public Article(JSONObject json) throws JSONException{
        super(json.getString("title"), json.getString("description"), json.getJSONArray("images").getString(0), json.getString("id"), TYPE_ARTICLE);

        Log.d("AAA", json.toString(4));

        JSONArray elementsJson = json.getJSONArray("content");
        for(int i=0;i<elementsJson.length();i++){
            elements.add(new ArticleElement(elementsJson.getJSONObject(i)));
        }

        event = json.getJSONObject("event");
        mPostId = event.getString("id");
        setNumberOfComments(event.getInt("numberOfComments"));
        setNumberOfLikes(event.getInt("numberOfLikes"));
        setNumberOfViews(event.getInt("numberOfViews"));
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

    public boolean isPostLiked(){
        return mIsPostLiked;
    }

    public void setPostLiked(boolean isLiked){
        mIsPostLiked = isLiked;
    }

    public boolean hasComments(){
        return mNumberOfComments > 0;
    }

    public String getPostId(){
        return mPostId;
    }

    public Article(Parcel in) {
        super(in);
        mPostId = in.readString();
        in.readList(elements, ArticleElement.class.getClassLoader());
        author = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mPostId);
        dest.writeList(elements);
        dest.writeString(author);
        dest.writeString(date);
    }

    public int getNumberOfComments() {
        return mNumberOfComments;
    }

    public void setNumberOfComments(int mNumberOfComments) {
        this.mNumberOfComments = mNumberOfComments;
    }

    public void incrementLikes(){
        mNumberOfLikes++;
    }

    public void decrementLikes(){
        mNumberOfLikes--;
    }

    public int getNumberOfLikes() {
        return mNumberOfLikes;
    }

    public void setNumberOfLikes(int mNumberOfLikes) {
        this.mNumberOfLikes = mNumberOfLikes;
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
