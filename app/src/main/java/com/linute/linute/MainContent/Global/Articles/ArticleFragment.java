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
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ArticleFragment.class.getSimpleName();
    public static final int MENU_ELEVATION_DP = 4;

    private Article mArticle;

    private RecyclerView mRecyclerView;
    private ArticleElementAdapter mAdapter;

    private static final String ARG_ARTICLE = "article";
    private GridLayoutManager mLayoutManager;

    private View vMenu;
    private View vLikeButton;
    private View vCommentButton;
    private View vShareButton;
    private ProgressBar vProgressBar;
    private String mUserId;

    private ToggleImageView vLikeIcon;

    public static ArticleFragment newInstance(Article article) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTICLE, article);

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
            Log.d(TAG, mArticle.elements.toString());
        }

        //Preload article images
        if (mArticle != null) {
//            RequestManager glide = Glide.with(this);
            for (ArticleElement element : mArticle.elements) {
                Log.d(TAG, element.toString());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_article, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mAdapter = new ArticleElementAdapter(mArticle);
        mRecyclerView.setAdapter(mAdapter);
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

                totalScroll = recyclerView.computeVerticalScrollOffset();// + recyclerView.computeVerticalScrollExtent();
                vProgressBar.setProgress(1000*totalScroll / (recyclerView.computeVerticalScrollRange()-recyclerView.computeVerticalScrollExtent()));


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

    @Override
    public void onClick(View v) {
        if (v == vLikeButton || v == vLikeIcon) {
            toggleLike();
        } else if (v == vCommentButton) {
            openComments();
        } else if (v == vShareButton) {
            startShare();
        }
    }

    private void toggleLike() {
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
    }
    private void openComments() {
        BaseTaptActivity activity = (BaseTaptActivity)getActivity();
        activity.addFragmentToContainer(FeedDetailPage.newInstance(mArticle.getPost(), false));
    }

    private void startShare() {

    }

}
