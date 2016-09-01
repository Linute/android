package com.linute.linute.MainContent.Global;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoiceItem {

    public final String title;
    public final String imageUrl;
    public final String key;
    private int unread;
    public final String description;

    public final int type;

    public static final int TYPE_HEADER_HOT = 0;
    public static final int TYPE_HEADER_FRIEND = 1;
    public static final int TYPE_TREND = 2;

    public GlobalChoiceItem(String title,
                            String description,
                            String imageUrl,
                            String key) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.key = key;
        this.unread = 0;
        this.type = TYPE_TREND;
        this.description = description;
    }

    public GlobalChoiceItem(String title, String key, int type){
        this.imageUrl = null;
        this.title = title;
        this.key = key;
        this.unread = Integer.MAX_VALUE;
        this.type = type;
        this.description = null;
    }


    public void setUnread(int unread) {
        this.unread = unread;
    }

    public boolean hasUnread() {
        return unread > 0;
    }

}
