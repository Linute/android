package com.linute.linute.MainContent.Global.Articles;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mikhail on 11/29/16.
 */

public class ArticleMediaElement extends ArticleElement{

    public final int height;
    public final int width;

    protected ArticleMediaElement(JSONObject object) throws JSONException{
        super(object);
        height = object.getJSONObject("size").getInt("height");
        width = object.getJSONObject("size").getInt("width");
    }


}
