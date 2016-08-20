package com.linute.linute.MainContent.Global;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoiceItem {

    public final String title;
    public final String imageUrl;
    public final String key;
    private boolean hasUnread;


    public GlobalChoiceItem(String title, String imageUrl, String key){
        this.imageUrl = imageUrl;
        this.title = title;
        this.key = key;
        this.hasUnread = false;
    }


    public void setHasUnread(boolean hasUnread){
        this.hasUnread = hasUnread;
    }

    public boolean hasUnread(){
        return hasUnread;
    }

}
