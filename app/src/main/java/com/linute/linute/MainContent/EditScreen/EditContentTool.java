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

    public EditContentTool(ViewGroup overlays){
        mOverlaysView = overlays;
    };


    //Binds icon and text of item to menu item
    public abstract void bindMenuItem(EditContentToolAdapter.ToolHolder holder);

    //creates a view to display tool options
    public abstract View createToolOptionsView(LayoutInflater inflater, ViewGroup parent);

    public abstract void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options);


}
