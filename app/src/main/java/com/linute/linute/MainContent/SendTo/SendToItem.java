package com.linute.linute.MainContent.SendTo;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToItem {

    public static final short TYPE_PERSON = 0;
    public static final short TYPE_TREND = 1;
    public static final short TYPE_CAMPUS = 2;

    private String mName;
    private String mId;
    private String mImage;
    private boolean mChecked;
    private short mType;

    public SendToItem(short type, String name, String id, String image) {
        mName = name;
        mId = id;
        mImage = image;
        mChecked = false;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public String getImage() {
        return mImage;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public short getType() {
        return mType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SendToItem && ((SendToItem) o).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }
}
