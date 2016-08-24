package com.linute.linute.MainContent.EditScreen;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/23/16.
 */
public class TextTool extends EditContentTool{


    public enum TextMode{
        Meme, Snapchat, TopOnly, BottomOnly
    }

    private static class TextModeObj{

        int icon;
        TextMode textMode;

        public TextModeObj(int icon, TextMode textMode) {
            this.icon = icon;
            this.textMode = textMode;
        }
    }


    TextModeObj[] textModes;

    public TextTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays) {
        super(uri, type, overlays);
        textModes = new TextModeObj[]{
            new TextModeObj(R.drawable.sticker_icon, TextMode.Meme),
            new TextModeObj(R.drawable.filters_icon, TextMode.Snapchat)
        };
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        View rootView = inflater.inflate(R.layout.list_item_tool, parent, false);





        return rootView;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public int getDrawable() {
        return R.drawable.meme_icon;
    }
}
