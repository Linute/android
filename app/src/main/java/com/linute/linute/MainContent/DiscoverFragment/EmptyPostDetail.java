package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyPostDetail extends Post {

    public EmptyPostDetail(){
    }


    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new EmptyPostDetail();
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}
