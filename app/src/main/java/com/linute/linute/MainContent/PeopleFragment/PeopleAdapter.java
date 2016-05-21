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
public class PeopleAdapter extends RecyclerView.Adapter<NearbyViewHolder> {

    private List<People> mPeopleList;

    private Context aContext;


    public PeopleAdapter(List<People> peopleList, Context context) {
        this.aContext = context;
        mPeopleList = peopleList;
    }

    @Override
    public NearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NearbyViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_people_nearby, parent, false)
                , aContext);
    }

    @Override
    public void onBindViewHolder(NearbyViewHolder holder, int position) {
        holder.bindView(mPeopleList.get(position));
    }

    @Override
    public int getItemCount() {
        return mPeopleList.size();
    }

}
