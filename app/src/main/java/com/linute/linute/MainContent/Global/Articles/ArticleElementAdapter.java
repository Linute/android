package com.linute.linute.MainContent.Global.Articles;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import java.util.ArrayList;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElementAdapter extends RecyclerView.Adapter<ArticleElementAdapter.ElementVH> {

    private ArrayList<ArticleElement> elements;

    public ArticleElementAdapter(ArrayList<ArticleElement> elements) {
        this.elements = elements;
    }

    @Override
    public ElementVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case ArticleElement.ElementTypes.TITLE:
                return new TextElementVH(inflater.inflate(R.layout.article_element_title, parent, false));
            case ArticleElement.ElementTypes.IMAGE:
                return new ImageElementVH(inflater.inflate(R.layout.article_element_image, parent, false));
            case ArticleElement.ElementTypes.PARAGRAPH:
                return new TextElementVH(inflater.inflate(R.layout.article_element_paragraph, parent, false));
            case ArticleElement.ElementTypes.GIF:
                return new GifElementVH(inflater.inflate(R.layout.article_element_gif, parent, false));
            case ArticleElement.ElementTypes.CAPTION:
                return new TextElementVH(inflater.inflate(R.layout.article_element_caption, parent, false));
            case ArticleElement.ElementTypes.VIDEO:
                return new VideoElementVH(inflater.inflate(R.layout.article_element_video, parent, false));
            case ArticleElement.ElementTypes.ATTRIBUTION:
                return new TextElementVH(inflater.inflate(R.layout.article_element_attribution, parent, false));
            case ArticleElement.ElementTypes.HEADER:
                return new TextElementVH(inflater.inflate(R.layout.article_element_header, parent, false));
            case ArticleElement.ElementTypes.QUOTE:
                return new TextElementVH(inflater.inflate(R.layout.article_element_quote, parent, false));
            case ArticleElement.ElementTypes.DATE:
                return new TextElementVH(inflater.inflate(R.layout.article_element_date, parent, false));
            case ArticleElement.ElementTypes.AUTHOR:
                return new TextElementVH(inflater.inflate(R.layout.article_element_author, parent, false));
            default:
                return new InvalidElementVH(inflater.inflate(R.layout.article_element_invalid, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return elements.get(position).type;
    }

    @Override
    public void onBindViewHolder(ElementVH holder, int position) {
        holder.bind(elements.get(position));
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }





    //View Holders

    static abstract class ElementVH extends RecyclerView.ViewHolder{
        ElementVH(View itemView) {super(itemView);}
        public abstract void bind(ArticleElement element);
    }

    private static class TextElementVH extends ElementVH{

        TextView vText;
        TextElementVH(View itemView) {
            super(itemView);
            vText = (TextView)itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(ArticleElement element) {
            vText.setText(element.content);
        }
    }

    private static class VideoElementVH extends ElementVH implements View.OnClickListener{

        private final ImageView vPreview;
        private final TextureVideoView vVideo;
        private final ImageView vPause;

        VideoElementVH(View itemView) {
            super(itemView);
            vPreview = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
            vVideo = (TextureVideoView) itemView.findViewById(R.id.video);
            vPause = (ImageView) itemView.findViewById(R.id.cinema_icon);
            vPause.setOnClickListener(this);
        }

        @Override
        public void bind(ArticleElement element) {
            vVideo.setVideoURI(Uri.parse(element.content));
        }

        @Override
        public void onClick(View v) {
            if(vVideo.isPlaying()){
                vVideo.pause();
            }else {
                vVideo.start();
            }
        }
    }

    private static class ImageElementVH extends ElementVH{

        private final ImageView vImage;
        ImageElementVH(View itemView) {
            super(itemView);
            vImage = (ImageView)itemView.findViewById(R.id.image);
        }

        @Override
        public void bind(ArticleElement element) {
            Glide.with(itemView.getContext())
                    .load(element.content)
                    .into(vImage);
        }
    }

    private static class GifElementVH extends ElementVH{
        private final WebView vWeb;
        public GifElementVH(View itemView) {
            super(itemView);
            vWeb = (WebView)itemView.findViewById(R.id.gif);

        }

        @Override
        public void bind(ArticleElement element) {
            String data = "<html><body><img style='display:block; width:100%' src=\""+element.content+"\"></body></html>";
            vWeb.loadData(data, "text/html", null);
        }
    }

    private static class InvalidElementVH extends ElementVH{

        private final TextView vText;
        InvalidElementVH(View itemView) {
            super(itemView);
            vText = (TextView)itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(ArticleElement element) {
            vText.setText("ERROR [" + element.type + "] is not a valid element \n content: " + element.content);
        }
    }

}
