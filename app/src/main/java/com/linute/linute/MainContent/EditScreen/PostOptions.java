package com.linute.linute.MainContent.EditScreen;

/**
 * Created by mikhail on 10/28/16.
 */

public final class PostOptions {


    public enum ContentType {
        None, Photo, Video, UploadedPhoto, UploadedVideo
    }

    public enum ContentSubType {
        None, Post, Chat, Comment, Comment_No_Anon
    }

    private final ContentType type;
    private final ContentSubType subType;
    private final String trendId;

    public PostOptions(
            ContentType type,
            ContentSubType subType,
            String trendId
    ) {
        this.type = type;
        this.subType = subType;
        this.trendId = trendId;
    }

}
