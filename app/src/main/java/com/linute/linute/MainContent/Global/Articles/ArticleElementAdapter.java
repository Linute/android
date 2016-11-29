package com.linute.linute.MainContent.Global.Articles;

import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Article article;
    private ArticleActions mArticleActions;
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

    public void setArticleActions(ArticleActions actions){
        this.mArticleActions = actions;
    }


    //View Holders


    static abstract class ElementVH extends RecyclerView.ViewHolder{
        ElementVH(View itemView) {super(itemView);}
        public abstract void bind(ArticleElement element);
    }

    class ArticleHeaderVH extends RecyclerView.ViewHolder{

        final TextView vTitle;
        final TextView vAuthor;
        final TextView vDate;
        final ToggleImageView vLikeIcon;
        final TextView vLikeCount;
        final ImageView vCommentIcon;
        final TextView vCommentCount;
        final TextView vViewCount;

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
            vViewCount = (TextView)itemView.findViewById(R.id.text_view_count);



        }

        public void bind(final Article article){
            vTitle.setText(article.title);
            vAuthor.setText(article.author);
            vDate.setText(article.date);
            vLikeCount.setText(String.valueOf(article.getNumberOfLikes()));
            vCommentCount.setText(String.valueOf(article.getNumberOfComments()));
            vViewCount.setText(String.valueOf(article.getNumberOfViews()));
            vLikeIcon.setActive(article.isPostLiked());

            if (article.hasComments()) {
                vCommentIcon.clearColorFilter();
            } else {
                vCommentIcon.setColorFilter(mFilterColor, PorterDuff.Mode.SRC_ATOP);
            }

            vLikeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mArticleActions.toggleLike(article);
                }
            });

            vCommentIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mArticleActions.openComments(article);
                }
            });

//            vCommentIcon.setImageResource(article.hasComments() ? R.drawable.ic_comment, );
        }
    }


    public static interface ArticleActions{
        public boolean toggleLike(Article article);
        public void openComments(Article article);
        public void startShare(Article article);
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
        private final SimpleExoPlayerView vVideo;
//        private final ImageView vPause;

//        private final WebView vHTML5;

        VideoElementVH(View itemView) {
            super(itemView);
            vPreview = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
            vVideo = (SimpleExoPlayerView) itemView.findViewById(R.id.exo_player);
            Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

            LoadControl loadControl = new DefaultLoadControl();

            SimpleExoPlayer player =
                    ExoPlayerFactory.newSimpleInstance(itemView.getContext(), trackSelector, loadControl);


            vVideo.setPlayer(player);
        }

        @Override
        public void bind(ArticleElement element) {
            String url = element.content.contains("http") ? element.content : Utils.getArticleVideoUrl(element.content);
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//            retriever.setDataSource(url, API_Methods.getMainHeader(new LSDKUser(itemView.getContext()).getToken()));
//            Bitmap b = retriever.getFrameAtTime(0);
            float ratio = (float)((ArticleMediaElement)element).height/((ArticleMediaElement)element).width;
//            b.recycle();
//
            vVideo.getLayoutParams().height = (int)(ratio * itemView.getResources().getDisplayMetrics().widthPixels);
            vVideo.getPlayer().prepare(buildMediaSource(Uri.parse(url), null));
            vVideo.setControllerShowTimeoutMs(2000);
        }

        private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
            int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                    : uri.getLastPathSegment());
            DataSource.Factory manifestDataSourceFactory = buildDataSourceFactory();
            switch (type) {
                case C.TYPE_SS:
                    return new SsMediaSource(uri, manifestDataSourceFactory,
                            new DefaultSsChunkSource.Factory(manifestDataSourceFactory), new Handler(), null);
                case C.TYPE_DASH:
                    return new DashMediaSource(uri, manifestDataSourceFactory,
                            new DefaultDashChunkSource.Factory(manifestDataSourceFactory), new Handler(), null);
                case C.TYPE_HLS:
                    return new HlsMediaSource(uri, manifestDataSourceFactory, new Handler(), null);
                case C.TYPE_OTHER:
                    return new ExtractorMediaSource(uri, manifestDataSourceFactory, new DefaultExtractorsFactory(),
                            new Handler(), null);
                default: {
                    throw new IllegalStateException("Unsupported type: " + type);
                }
            }
        }

        private DataSource.Factory buildDataSourceFactory() {

            return new DefaultHttpDataSourceFactory("a");
        }

        @Override
        public void onClick(View v) {
            /*if(vVideo.isPlaying()){
                vVideo.pause();
            }else {
                vVideo.start();
            }*/
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
                    .load(Utils.getArticleImageUrl(element.content))
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
            String data = "<html><body><img style='display:block; width:100%' src=\""+Utils.getArticleImageUrl(element.content)+"\"></body></html>";
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
