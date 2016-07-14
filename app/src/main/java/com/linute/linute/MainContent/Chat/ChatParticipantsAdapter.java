package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by mikhail on 7/12/16.
 */
public class ChatParticipantsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int TYPE_ADD = 0;
    private static final int TYPE_PARTICIPANT = 1;

    private ArrayList<ChatFragment.User> mParticipants;

    public void setAddPeopleListener(View.OnClickListener addPeopleListener) {
        this.mAddPeopleListener = addPeopleListener;
    }

    private View.OnClickListener mAddPeopleListener;


    public ChatParticipantsAdapter(ArrayList<ChatFragment.User> participants) {
        this.mParticipants = participants;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case TYPE_ADD:
                return new AddVH(inflater.inflate(R.layout.fragment_chat_settings_participant, parent, false));
            case TYPE_PARTICIPANT:
                return new ParticipantVH(inflater.inflate(R.layout.fragment_chat_settings_participant, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_PARTICIPANT:
                ((ParticipantVH)holder).bind(getItem(position));
                return;
            case TYPE_ADD:
                ((AddVH)holder).bind();
                holder.itemView.setOnClickListener(mAddPeopleListener);
                return;

        }
    }

    public ChatFragment.User getItem(int position){
        //-1 to accommodate for add-participant list item
        return mParticipants.get(position-1);
    }

    @Override
    public int getItemCount() {
        //+1 to accommodate for add-participant list item
        return mParticipants.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_ADD : TYPE_PARTICIPANT;
    }

    public static class ParticipantVH extends RecyclerView.ViewHolder{
        public final ImageView profileImageIV;
        public final TextView nameTV;

        public ParticipantVH(View itemView) {
            super(itemView);
            profileImageIV = (ImageView)itemView.findViewById(R.id.participant_image);
            nameTV = (TextView) itemView.findViewById(R.id.participant_name);
        }

        public void bind(ChatFragment.User user){
            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.profileImage))
                    .into(profileImageIV);
            nameTV.setText(user.name);
        }
    }

    public static class AddVH extends RecyclerView.ViewHolder{
        public final ImageView profileImageIV;
        public final TextView nameTV;

        public AddVH(View itemView) {
            super(itemView);
            profileImageIV = (ImageView)itemView.findViewById(R.id.participant_image);
            nameTV = (TextView) itemView.findViewById(R.id.participant_name);
        }

        public void bind(){
            profileImageIV.setImageResource(R.drawable.ic_action_add);
            nameTV.setText("Add People");


        }

        public interface AddClickPeopleListener{
            public void onAddPeoplClick();
        }

    }




}
