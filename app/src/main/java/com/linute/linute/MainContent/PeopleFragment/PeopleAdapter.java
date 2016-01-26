package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.linute.linute.R;

import java.util.List;

/**
 * Created by Arman on 1/8/16.
 */
public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEARCH_NAME = 0;
    private static final int TYPE_SEARCH_CONTACTS = 1;
    private static final int TYPE_SEARCH_FACEBOOK = 2;
    private static final int TYPE_TOP_CAMPUS_TEXT = 3;
    private static final int TYPE_ITEM = 4;

    private List<People> mPeopleList;

    private Context aContext;

    public PeopleAdapter(List<People> peopleList, Context context) {
        this.aContext = context;
        mPeopleList = peopleList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEARCH_NAME) {
            return new NameViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_people_header_name, parent, false));
        } else if (viewType == TYPE_SEARCH_CONTACTS) {
            return new ContactsViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_people_header_contacts, parent, false));
        } else if (viewType == TYPE_SEARCH_FACEBOOK) {
            return new FacebookViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_people_header_facebook, parent, false));
        } else if (viewType == TYPE_TOP_CAMPUS_TEXT) {
            return new TopCampusViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_people_header_text, parent, false));
        } else if (viewType == TYPE_ITEM) {
            return new PeopleViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_people_list_item, parent, false), aContext, mPeopleList);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PeopleViewHolder) {
            ((PeopleViewHolder) holder).bindModel(mPeopleList.get(position - 4));
        }
    }

    @Override
    public int getItemCount() {
        return mPeopleList.size() + 4;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_SEARCH_NAME;
            case 1:
                return TYPE_SEARCH_CONTACTS;
            case 2:
                return TYPE_SEARCH_FACEBOOK;
            case 3:
                return TYPE_TOP_CAMPUS_TEXT;
            default:
                return TYPE_ITEM;
        }

    }

    class NameViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vLinearLayout;

        public NameViewHolder(View itemView) {
            super(itemView);

            vLinearLayout = (LinearLayout) itemView.findViewById(R.id.people_name);
            vLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:
                }
            });
        }
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vLinearLayout;

        public ContactsViewHolder(View itemView) {
            super(itemView);

            vLinearLayout = (LinearLayout) itemView.findViewById(R.id.people_contacts);
            vLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:
                }
            });
        }
    }

    class FacebookViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vLinearLayout;

        public FacebookViewHolder(View itemView) {
            super(itemView);

            vLinearLayout = (LinearLayout) itemView.findViewById(R.id.people_facebook);
            vLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:
                }
            });
        }
    }

    class TopCampusViewHolder extends RecyclerView.ViewHolder {
        public TopCampusViewHolder(View itemView) {
            super(itemView);
        }
    }
}
