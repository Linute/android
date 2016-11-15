package com.linute.linute.MainContent.Global;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalChoiceItem implements Parcelable {

    public String title;
    public final String imageUrl;
    public final String key;
    private int unread;
    public String description;

    public final int type;

    public static final int TYPE_HEADER_HOT = 0;
    public static final int TYPE_HEADER_FRIEND = 1;
    public static final int TYPE_TREND = 2;
    public static final int TYPE_ARTICLE = 3;

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

    public GlobalChoiceItem(String title,
                            String description,
                            String imageUrl,
                            String key,
                            int type) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.key = key;
        this.unread = 0;
        this.type = type;
        this.description = description;
    }


    public GlobalChoiceItem(String title, String key, int type) {
        this.imageUrl = null;
        this.title = title;
        this.key = key;
        this.unread = Integer.MAX_VALUE;
        this.type = type;
        this.description = null;
    }


    protected GlobalChoiceItem(Parcel in) {
        title = in.readString();
        imageUrl = in.readString();
        key = in.readString();
        unread = in.readInt();
        description = in.readString();
        type = in.readInt();
    }

    public static final Creator<GlobalChoiceItem> CREATOR = new Creator<GlobalChoiceItem>() {
        @Override
        public GlobalChoiceItem createFromParcel(Parcel in) {
            return new GlobalChoiceItem(in);
        }

        @Override
        public GlobalChoiceItem[] newArray(int size) {
            return new GlobalChoiceItem[size];
        }
    };

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public boolean hasUnread() {
        return unread > 0;
    }

    public int getUnread(){
        return unread;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(key);
        dest.writeInt(unread);
        dest.writeString(description);
        dest.writeInt(type);
    }


    public static void sort(List<GlobalChoiceItem> itemList) {
        Collections.sort(itemList, new Comparator<GlobalChoiceItem>() {
            @Override
            public int compare(GlobalChoiceItem lhs, GlobalChoiceItem rhs) {
                if (lhs.unread < rhs.unread)
                    return 1;
                else if (lhs.unread > rhs.unread)
                    return -1;
                else
                    return 0;

            }
        });
    }


}
