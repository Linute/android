package com.linute.linute.MainContent.EditScreen;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mikhail on 8/22/16.
 */
public abstract class EditContentTool {

    //View where all content should be added
    protected ViewGroup mOverlaysView;
    protected Uri mUri;
    protected EditFragment.ContentType mContentType;

    public EditContentTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays){
        mOverlaysView = overlays;
        mUri = uri;
        mContentType = type;
    }

    public void onPause(){}

    //Binds icon and text of item to menu item
//    public abstract void bindMenuItem(EditContentToolAdapter.ToolHolder holder, boolean isSelected);

    //creates a view to display tool options
    public abstract View createToolOptionsView(LayoutInflater inflater, ViewGroup parent);

    public abstract void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options);

    public abstract String getName();
    public abstract int getDrawable();

    public void onOpen(){}
    public void onClose(){}
    public void onDisable(){}
    public void onEnable(){}

    public void onDestroy(){}

}
