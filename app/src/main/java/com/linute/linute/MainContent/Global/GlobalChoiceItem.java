package com.linute.linute.MainContent.Global;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoiceItem {

    private String mTitle;
    private String mImageUrl;
    private String mKey;

    public GlobalChoiceItem(String title, String imageUrl, String key){
        mImageUrl = imageUrl;
        mTitle = title;
        mKey = key;
    }


    public String getImageUrl() {
        return mImageUrl;
    }


    public String getKey(){
        return mKey;
    }

    public String getTitle() {
        return mTitle;
    }

}
