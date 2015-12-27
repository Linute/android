package com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters;

/**
 * Created by Arman on 12/27/15.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

public class ParcelableSparseBooleanArray extends SparseBooleanArray
        implements Parcelable {
    public static Creator<ParcelableSparseBooleanArray> CREATOR
            = new Creator<ParcelableSparseBooleanArray>() {
        @Override
        public ParcelableSparseBooleanArray createFromParcel(Parcel source) {
            return (new ParcelableSparseBooleanArray(source));
        }

        @Override
        public ParcelableSparseBooleanArray[] newArray(int size) {
            return (new ParcelableSparseBooleanArray[size]);
        }
    };

    public ParcelableSparseBooleanArray() {
        super();
    }

    private ParcelableSparseBooleanArray(Parcel source) {
        int size = source.readInt();

        for (int i = 0; i < size; i++) {
            put(source.readInt(), (Boolean) source.readValue(null));
        }
    }

    @Override
    public int describeContents() {
        return (0);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size());

        for (int i = 0; i < size(); i++) {
            dest.writeInt(keyAt(i));
            dest.writeValue(valueAt(i));
        }
    }
}