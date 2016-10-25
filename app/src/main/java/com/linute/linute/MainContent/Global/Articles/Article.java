package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;

import com.linute.linute.MainContent.Global.GlobalChoiceItem;

import java.util.ArrayList;

/**
 * Created by mikhail on 10/25/16.
 */

public class Article extends GlobalChoiceItem{

    public final ArrayList<ArticleElement> elements = new ArrayList<>();

    public Article(String title, String description, String imageUrl, String key) {
        super(title, description, imageUrl, key, TYPE_ARTICLE);
    }

    public Article(String title, String key) {
        super(title, key, TYPE_ARTICLE);
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
