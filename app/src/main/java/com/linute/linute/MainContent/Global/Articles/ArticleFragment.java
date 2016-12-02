package com.linute.linute.MainContent.Global.Articles;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.ImpressionHelper;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleFragment extends Fragment implements View.OnClickListener, ArticleElementAdapter.ArticleActions {

    public static final String TAG = ArticleFragment.class.getSimpleName();
    public static final int MENU_ELEVATION_DP = 4;

    private String mArticleId;
    private Article mArticle;

    private RecyclerView mRecyclerView;
    private ArticleElementAdapter mAdapter;

    private static final String ARG_ARTICLE = "article";
    private static final String ARG_ARTICLE_ID = "articleid";
    private GridLayoutManager mLayoutManager;

    private View vMenu;
    private View vLikeButton;
    private View vCommentButton;
    private View vShareButton;
    private ProgressBar vProgressBar;
    private String mUserId;

    private ToggleImageView vLikeIcon;
    private Toolbar mToolbar;

    private boolean mIsArticleLoaded = false;

    public static ArticleFragment newInstance(Article article) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTICLE, article);

        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ArticleFragment newInstance(String articleId){
        Bundle args = new Bundle();
        args.putString(ARG_ARTICLE_ID, articleId);

        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");


        Bundle args = getArguments();
        if (args != null) {
            mArticle = args.getParcelable(ARG_ARTICLE);
            if(mArticle != null){
                mArticleId = mArticle.getPostId();
                Log.d(TAG, mArticle.elements.toString());
            }else{
                mArticleId = args.getString(ARG_ARTICLE_ID);
                loadArticle(mArticleId);
            }
        }

        /*//Preload article images
        if (mArticle != null) {
            RequestManager glide = Glide.with(this);
            for (ArticleElement element : mArticle.elements) {
                Log.d(TAG, element.toString());
            }
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_article, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


//        toolbar.setTitleTextAppearance();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mAdapter.getItemViewType(position);
                if (viewType == ArticleElement.ElementTypes.AUTHOR || viewType == ArticleElement.ElementTypes.DATE) {
                    return 1;
                }
                return 2;
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem = 0;

            int totalScroll = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();


                int visibleBotSpace = 0;
                if(lastVisibleItem == mLayoutManager.getItemCount()-1) {
                    visibleBotSpace = displayMetrics.heightPixels-mLayoutManager.findViewByPosition(lastVisibleItem).getBottom();
                }
                if(visibleBotSpace < 0){visibleBotSpace = 0;}

                int maxBotSpace = 2*recyclerView.getPaddingBottom();
                int botSpace = maxBotSpace-visibleBotSpace;
                float elevation = getElevation(displayMetrics, maxBotSpace, botSpace);
                ViewCompat.setElevation(vMenu, elevation);

                /*if(mLayoutManager.findFirstCompletelyVisibleItemPosition()>0 && lastVisibleItem == 0){
                    showMenu();
                }else if(mLayoutManager.findFirstVisibleItemPosition() == 0 && lastVisibleItem != 0){
                    hideMenu();
                }*/

//                totalScroll = recyclerView.computeVerticalScrollOffset();// + recyclerView.computeVerticalScrollExtent();
//                vProgressBar.setProgress(1000*totalScroll / (recyclerView.computeVerticalScrollRange()-recyclerView.computeVerticalScrollExtent()));


//                Log.d(TAG, "Scrolled "+totalScroll + " " +recyclerView.computeVerticalScrollRange() + " " + recyclerView.computeVerticalScrollOffset() + " " + recyclerView.computeVerticalScrollExtent());

            }

            private float getElevation(DisplayMetrics displayMetrics, int maxBotSpace, int botSpace) {
//                return botSpace * MENU_ELEVATION_DP * displayMetrics.density / maxBotSpace;
                return (float)Math.pow(botSpace/maxBotSpace,2) * MENU_ELEVATION_DP * displayMetrics.density;
            }
        });

        vProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);

        //menu
        vMenu = view.findViewById(R.id.menu);
        vLikeButton = view.findViewById(R.id.menu_like);
        vCommentButton = view.findViewById(R.id.menu_comment);
        vShareButton = view.findViewById(R.id.menu_share);
        vLikeIcon = (ToggleImageView)view.findViewById(R.id.icon_like);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vShareButton.setOnClickListener(this);

       /* if(vLikeButton.getBackground() instanceof RippleDrawable){
            Log.d(TAG, vLikeButton.getBackground().toString());

//            drawable.setI
//            ShapeDrawable round = ShapeDrawable.create(R.drawable.bg_round_white);
//            ((RippleDrawable)vLikeButton.getBackground()).getLayer(0,android.R.id.mask);
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.bg_round_white);
            int index = ((RippleDrawable)vLikeButton.getBackground()).addLayer(drawable);
            ((RippleDrawable)vLikeButton.getBackground()).setId(index,android.R.id.mask);
        }*/

        if(mArticle != null)
        bindArticle(mArticle, false);


        return view;
    }


    /*private void showMenu(){
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        vMenu.clearAnimation();
        vMenu.animate().y(screenHeight-vMenu.getMeasuredHeight()-20);
    }

    private void hideMenu(){
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        vMenu.clearAnimation();
        vMenu.animate().y(screenHeight+vMenu.getMeasuredHeight()+20);
    }*/

    private void bindArticle(Article article, boolean overwrite) {
        if(!overwrite && mIsArticleLoaded){
            return;
        }
        mIsArticleLoaded = true;
        mToolbar.setTitle(article.title);
        mAdapter = new ArticleElementAdapter(article);
        mAdapter.setArticleActions(this);
        mRecyclerView.setAdapter(mAdapter);
        ImpressionHelper.sendImpressionsAsync(null, mUserId, article.getPostId());
    }

    private void loadArticle(String id){
        new LSDKGlobal(getContext()).getArticle(id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Utils.showServerErrorToast(getContext());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.toString());
                if(!response.isSuccessful()){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getContext());
                        }
                    });
                    return;
                }
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    mArticle = new Article(json);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bindArticle(mArticle, false);
                        }
                    });
                }catch (JSONException e){
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getContext());
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onClick(final View v) {
        v.setAlpha(.5f);
//        ObjectAnimator animator = new ObjectAnimator();
        v.clearAnimation();
        v.animate().alpha(1).start();

        if (v == vLikeButton || v == vLikeIcon) {
            toggleLike(mArticle);
        } else if (v == vCommentButton) {
            openComments(mArticle);
        } else if (v == vShareButton) {
            startShare(mArticle);
        }
    }

    @Override
    public boolean toggleLike(Article article) {
        ToggleImageView checkbox = vLikeIcon;
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            try {
                boolean emit = false;

                if (mArticle.isPostLiked()) {
//                    checkbox.setActive(false);
                    mArticle.setPostLiked(false);
                    mArticle.decrementLikes();
                    emit = true;
                } else {
//                    checkbox.setActive(true);
                    mArticle.setPostLiked(true);
                    mArticle.incrementLikes();
                    emit = true;
                }

                if (emit) {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", mArticle.getPostId());
                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:like", body);
                }

                mAdapter.notifyItemChanged(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mArticle.isPostLiked();
    }
    @Override
    public void openComments(Article article) {
        BaseTaptActivity activity = (BaseTaptActivity)getActivity();
        activity.addFragmentToContainer(FeedDetailPage.newInstance(mArticle.getPost(), false));
    }

    @Override
    public void startShare(Article article) {

    }




}
