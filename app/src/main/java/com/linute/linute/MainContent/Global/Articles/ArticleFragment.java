package com.linute.linute.MainContent.Global.Articles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleFragment extends Fragment implements View.OnClickListener{

    private Article mArticle;

    private RecyclerView mRecyclerView;
    private ArticleElementAdapter mAdapter;

    private static final String ARG_ARTICLE = "article";
    private GridLayoutManager mLayoutManager;
    private View vLikeButton;
    private View vCommentButton;
    private View vShareButton;

    public static ArticleFragment newInstance(Article article){
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTICLE, article);

        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            mArticle = args.getParcelable(ARG_ARTICLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_article, container, false);

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mAdapter = new ArticleElementAdapter(mArticle.elements);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mAdapter.getItemViewType(position);
                if(viewType == ArticleElement.ElementTypes.AUTHOR || viewType == ArticleElement.ElementTypes.DATE){
                    return 1;
                }
                return 2;
            }
        });


        //menu
        vLikeButton = view.findViewById(R.id.menu_like);
        vCommentButton = view.findViewById(R.id.menu_comment);
        vShareButton = view.findViewById(R.id.menu_share);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vShareButton.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        if(v == vLikeButton){
            like();
        }else if()
    }

    private void like(){

    }

    private void openComments(){

    }

    private void


}
