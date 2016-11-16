package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;

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

    public Article(JSONObject json) throws JSONException{
        super(json.getString("title"), json.getString("description"), json.getJSONArray("images").getString(0), json.getString("id"), TYPE_ARTICLE);

        JSONArray elementsJson = json.getJSONArray("content");
        for(int i=0;i<elementsJson.length();i++){
            elements.add(new ArticleElement(elementsJson.getJSONObject(i)));
        }

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

    public Article(Parcel in) {
        super(in);
        in.readList(elements, ArticleElement.class.getClassLoader());
        author = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(elements);
        dest.writeString(author);
        dest.writeString(date);
    }
}
