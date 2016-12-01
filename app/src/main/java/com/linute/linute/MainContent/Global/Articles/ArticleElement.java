package com.linute.linute.MainContent.Global.Articles;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElement implements Parcelable {


    public static final class ElementTypes {
        private ElementTypes() {
        }

        public static final int TITLE = 0;
        public static final int DATE = 1;
        public static final int AUTHOR = 2;
        public static final int HEADER = 3;
        public static final int PARAGRAPH = 4;
        public static final int CAPTION = 5;
        public static final int ATTRIBUTION = 6;
        public static final int IMAGE = 7;
        public static final int VIDEO = 8;
        public static final int GIF = 9;
        public static final int QUOTE = 10;
        public static final int LINK = 11;

    }

//    public static final Map<String, Integer> ELEMENT_TYPES = new HashMap<>();

    /*static {
        ELEMENT_TYPES.put("title", ElementTypes.TITLE);
        ELEMENT_TYPES.put("paragraph", ElementTypes.PARAGRAPH);
        ELEMENT_TYPES.put("header", ElementTypes.HEADER);
        ELEMENT_TYPES.put("caption", ElementTypes.CAPTION);
        ELEMENT_TYPES.put("image", ElementTypes.IMAGE);
        ELEMENT_TYPES.put("video", ElementTypes.VIDEO);
        ELEMENT_TYPES.put("gif", ElementTypes.GIF);
        ELEMENT_TYPES.put("attribution", ElementTypes.ATTRIBUTION);
    }*/


    public final int type;
    public final String content;

    protected ArticleElement(JSONObject object) throws JSONException {
        this.type = object.getInt("type"); //ELEMENT_TYPES.get(object.getString("type"));
        this.content = object.getString("value");
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

    @Override
    public String toString() {
        String ret;
        switch (type) {
            case 0:
                ret = "TITLE";
                break;
            case 1:
                ret = "DATE";
                break;
            case 2:
                ret = "AUTHOR";
                break;
            case 3:
                ret = "HEADER";
                break;
            case 4:
                ret = "PARAGRAPH";
                break;
            case 5:
                ret = "CAPTION";
                break;
            case 6:
                ret = "ATTRIBUTION";
                break;
            case 7:
                ret = "IMAGE";
                break;
            case 8:
                ret = "VIDEO";
                break;
            case 9:
                ret = "GIF";
                break;
            case 10:
                ret = "QUOTE";
                break;
            default:
                ret = "ERR";
        }

        return ret.substring(0, 1).toUpperCase(Locale.US) + ret.substring(1).toLowerCase(Locale.US) + ": " + content;
    }

    public static ArticleElement parseJSON(JSONObject object) throws JSONException {
        int type = object.getInt("type");
        switch (type) {
            case ElementTypes.IMAGE:
            case ElementTypes.VIDEO:
            case ElementTypes.GIF:
                return new ArticleMediaElement(object);
            case ElementTypes.LINK:
                return new ArticleLinkElement(object);
            default:
                return new ArticleElement(object);
        }


    }
}
