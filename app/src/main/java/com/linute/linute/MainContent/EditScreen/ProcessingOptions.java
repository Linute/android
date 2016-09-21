package com.linute.linute.MainContent.EditScreen;

import java.util.ArrayList;

/**
 * Created by mikhail on 8/22/16.
 */
public class ProcessingOptions {

    public static final boolean DEFAULT_POST_AS_ANON = false;
    public static final boolean DEFAULT_ANON_COMMENTS_DISABLED = true;
    public static final String DEFAULT_TEXT = "";


    public int topInset = 0;
    public int bottomInset = 0;
    public boolean postAsAnon = DEFAULT_POST_AS_ANON;
    public boolean isAnonCommentsDisabled = DEFAULT_ANON_COMMENTS_DISABLED;
    public ArrayList<String> filters = new ArrayList<>();
    public ArrayList<String> stickers = new ArrayList<>();

    public String text = DEFAULT_TEXT;
}
