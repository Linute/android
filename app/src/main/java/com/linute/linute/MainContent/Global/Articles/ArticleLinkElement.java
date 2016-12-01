package com.linute.linute.MainContent.Global.Articles;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mikhail on 12/1/16.
 */

public class ArticleLinkElement extends ArticleElement {

    public final String link;

    public ArticleLinkElement(JSONObject object) throws JSONException {
        super(object);
        link = object.getString("attribution");
    }
}
