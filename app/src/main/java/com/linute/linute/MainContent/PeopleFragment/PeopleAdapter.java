package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.R;

import java.util.List;

/**
 * Created by Arman on 1/8/16.
 */
public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<People> mPeopleList;

    private Context aContext;

    private boolean mIsNearbyFragment;

    public PeopleAdapter(List<People> peopleList, Context context, boolean isNearbyFragment) {
        this.aContext = context;
        mPeopleList = peopleList;
        mIsNearbyFragment = isNearbyFragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mIsNearbyFragment) return new NearbyViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_people_nearby, parent, false)
            , aContext);

        else return new PeopleViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_people_popular_item_cell, parent, false), aContext);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PeopleViewHolder)
            ((PeopleViewHolder) holder).bindModel(mPeopleList.get(position));
        else
            ((NearbyViewHolder) holder).bindView(mPeopleList.get(position));

    }

    @Override
    public int getItemCount() {
        return mPeopleList.size();
    }


}
