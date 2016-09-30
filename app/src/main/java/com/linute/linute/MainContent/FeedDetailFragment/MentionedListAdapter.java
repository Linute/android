package com.linute.linute.MainContent.FeedDetailFragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by QiFeng on 9/30/16.
 */
public class MentionedListAdapter extends RecyclerView.Adapter<MentionedListAdapter.MentionedVH> {

    private ArrayList<MentionedPerson> mPeopleMentioned;
    private RequestManager mRequestManager;

    public MentionedListAdapter(ArrayList<MentionedPerson> peopleMentioned, RequestManager manager){
        mPeopleMentioned = peopleMentioned;
        mRequestManager = manager;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public MentionedVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MentionedVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_create_chat_selected_users, parent, false));
    }

    @Override
    public void onBindViewHolder(MentionedVH holder, int position) {
        holder.bindView(mPeopleMentioned.get(position));
    }

    @Override
    public int getItemCount() {
        return mPeopleMentioned.size();
    }

    protected class MentionedVH extends RecyclerView.ViewHolder{
        MentionedPerson mMentionedPerson;
        ImageView vProfile;

        public MentionedVH(View itemView) {
            super(itemView);
            vProfile = (ImageView) itemView.findViewById(R.id.image_user);
        }

        public void bindView(MentionedPerson person){
            mMentionedPerson = person;
            Log.i("test", "bindView: "+person.getProfileImage());
            mRequestManager.load(person.isAnon() ? Utils.getAnonImageUrl(person.getProfileImage()) : Utils.getImageUrlOfUser(person.getProfileImage()))
                    .placeholder(R.color.seperator_color)
                    .into(vProfile);
        }
    }
}
