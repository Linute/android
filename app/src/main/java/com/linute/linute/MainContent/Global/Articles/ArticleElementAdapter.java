package com.linute.linute.MainContent.Global.Articles;

import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Article article;
//    private ArrayList<ArticleElement> elements;

    public ArticleElementAdapter(Article article) {
        this.article = article;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case -1:
                return new ArticleHeaderVH(inflater.inflate(R.layout.article_header, parent, false));
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
        if(position == 0){
            return -1;
        }else{
            return getElement(position).type;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ElementVH){
            ((ElementVH)holder).bind(getElement(position));
        }else
        if(holder instanceof ArticleHeaderVH){
            ((ArticleHeaderVH)holder).bind(article);
        }
    }

    @Override
    public int getItemCount() {
        return article.elements.size()+1;
    }


    public ArticleElement getElement(int position){
        return article.elements.get(position-1);
    }



    //View Holders


    static abstract class ElementVH extends RecyclerView.ViewHolder{
        ElementVH(View itemView) {super(itemView);}
        public abstract void bind(ArticleElement element);
    }

    static class ArticleHeaderVH extends RecyclerView.ViewHolder{

        final TextView vTitle;
        final TextView vAuthor;
        final TextView vDate;
        final ToggleImageView vLikeIcon;
        final TextView vLikeCount;
        final ImageView vCommentIcon;
        final TextView vCommentCount;

        final int mFilterColor;



        ArticleHeaderVH(View itemView){
            super(itemView);
            mFilterColor = ContextCompat.getColor(itemView.getContext(), R.color.inactive_grey);

            vTitle = (TextView)itemView.findViewById(R.id.text_title);
            vAuthor = (TextView)itemView.findViewById(R.id.text_author);
            vDate = (TextView)itemView.findViewById(R.id.text_date);
            vLikeIcon = (ToggleImageView)itemView.findViewById(R.id.icon_like);
            vLikeIcon.setImageViews(R.drawable.ic_fire_off, R.drawable.ic_fire);
            vLikeCount = (TextView)itemView.findViewById(R.id.text_like_count);
            vCommentIcon = (ImageView)itemView.findViewById(R.id.icon_comment);
            vCommentCount = (TextView)itemView.findViewById(R.id.text_comment_count);

        }

        public void bind(Article article){
            vTitle.setText(article.title);
            vAuthor.setText(article.author);
            vDate.setText(article.date);
            vLikeCount.setText(String.valueOf(article.getNumberOfLikes()));
            vCommentCount.setText(String.valueOf(article.getNumberOfComments()));
            Log.d("AAA", article.isPostLiked()+"");
            vLikeIcon.setActive(article.isPostLiked());
            if (article.hasComments()) {
                vCommentIcon.clearColorFilter();
            } else {
                vCommentIcon.setColorFilter(mFilterColor, PorterDuff.Mode.SRC_ATOP);
            }

//            vCommentIcon.setImageResource(article.hasComments() ? R.drawable.ic_comment, );
        }
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

    private class ImageElementVH extends ElementVH{

        private final ImageView vImage;
        private final ProgressBar vProgressBar;

        ImageElementVH(View itemView) {
            super(itemView);
            vImage = (ImageView)itemView.findViewById(R.id.image);
            vProgressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
        }

        @Override
        public void bind(ArticleElement element) {
            /*vImage.setVisibility(View.GONE);
            vProgressBar.setVisibility(View.VISIBLE);*/



            Glide.with(itemView.getContext())
                    .load(element.content)
                    .placeholder(R.drawable.image_loading_background)
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
