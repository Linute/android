package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElement implements Parcelable{


    public static final class ElementTypes {
        private ElementTypes() {}

        public static final int TEXT = 0;
        public static final int IMAGE = 1;
    }

    public static final Map<String, Integer> ELEMENT_TYPES = new HashMap<>();

    static {
        ELEMENT_TYPES.put("text", ElementTypes.TEXT);
        ELEMENT_TYPES.put("image", ElementTypes.IMAGE);
    }


    public final int type;
    public final String content;

    protected ArticleElement(JSONObject object) throws JSONException{
        this.type = ELEMENT_TYPES.get(object.getString("type"));
        this.content = object.getString("content");
    }

    protected ArticleElement(int type, String content) {
        this.type = type;
        this.content = content;
    }

    protected ArticleElement(Parcel in) {
        type = in.readInt();
        content = in.readString();
    }

    public static final Creator<ArticleElement> CREATOR = new Creator<ArticleElement>() {
        @Override
        public ArticleElement createFromParcel(Parcel in) {
            return new ArticleElement(in);
        }

        @Override
        public ArticleElement[] newArray(int size) {
            return new ArticleElement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(content);
    }




}
