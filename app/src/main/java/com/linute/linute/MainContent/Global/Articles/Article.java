package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;

import com.linute.linute.MainContent.Global.GlobalChoiceItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mikhail on 10/25/16.
 */

public class Article extends GlobalChoiceItem{

    public final ArrayList<ArticleElement> elements = new ArrayList<>();

    public Article(JSONObject json) throws JSONException{
        super(json.getString("title"), json.getString("publisher"), json.getString("image"), json.getString("id"), TYPE_ARTICLE);

        JSONArray elementsJson = json.getJSONArray("content");
        for(int i=0;i<elementsJson.length();i++){
            elements.add(new ArticleElement(elementsJson.getJSONObject(i)));
        }

    }

    public Article(String id, Date date, String title, String publisher, String imageUrl, String[] authors, ArrayList<ArticleElement> content, int color){
        super(title, publisher, imageUrl, id);


    }

    public Article(String title, String description, String imageUrl, String id) {
        super(title, description, imageUrl, id, TYPE_ARTICLE);
    }

    public Article(String title, String id) {
        super(title, id, TYPE_ARTICLE);
    }

    public Article(Parcel in) {
        super(in);
        in.readList(elements, ArticleElement.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(elements);
    }
}
