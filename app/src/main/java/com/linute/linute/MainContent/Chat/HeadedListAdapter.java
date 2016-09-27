package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mikhail on 9/17/16.
 */
public abstract class HeadedListAdapter extends RecyclerView.Adapter<HeadedListAdapter.VH> {

    public static final int TYPE_HEADER = -1;
    private List[] mLists;
    private int[] mHeaderPositions;


    public HeadedListAdapter() {
        this.mLists = new ArrayList[getNumLists()];
        this.mHeaderPositions = new int[getNumLists()];
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                invalidateHeaderPositions();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                invalidateHeaderPositions();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                invalidateHeaderPositions();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                invalidateHeaderPositions();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                invalidateHeaderPositions();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                invalidateHeaderPositions();
            }
        });
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderVH(inflater.inflate(R.layout.list_header, parent, false));
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final VH holder, int position) {
        ItemStatus status = getItemStatus(position);
        holder.bind(getItem(position), status);
        holder.itemView.setTag(position);

        if(status != ItemStatus.Locked) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = holder.getAdapterPosition();
                    onItemSelected(i, getItemViewType(i), getItem(i));
                }
            });
        }else{
            holder.itemView.setOnClickListener(null);
        }
    }

    public ItemStatus getItemStatus(int position){
        for(int pos:mHeaderPositions){
            if(pos == position){
                return ItemStatus.Locked;
            }
        }
        return ItemStatus.None;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (List l : mLists) {
            if (l.size() > 0) {
                count += l.size() + 1;
            }
        }
        return count;
    }

    public Object getItem(int index) {
        int section = getSection(index);
        if (index == mHeaderPositions[section]) {
            return getHeaderTitle(section);
        }
        Object o = mLists[section].get(index - mHeaderPositions[section] - 1);
        return o;
    }

    public abstract String getHeaderTitle(int i);

    @Override
    public int getItemViewType(int position) {
        for (int i = 0; i < mHeaderPositions.length; i++) {
            if(mHeaderPositions[i] == -1) continue;
            if (mHeaderPositions[i] == position) {
                return TYPE_HEADER;
            }
            if (mHeaderPositions[i] > position) {
                int l = i-1;
                while(mHeaderPositions[l] == -1 && l > 0){
                    l--;
                }
                return l;
            }
        }
        return mHeaderPositions.length - 1;
    }

    public int getSection(int position) {
        for (int i = 0; i < mHeaderPositions.length; i++) {
            if(mHeaderPositions[i] == -1) continue;
            if (mHeaderPositions[i] == position) {
                return i;
            }
            if (mHeaderPositions[i] > position) {
                int l = i-1;
                while(mHeaderPositions[l] == -1 && l > 0){
                    l--;
                }
                return l;
            }
        }
        return mHeaderPositions.length - 1;
    }

    public void invalidateHeaderPositions() {
        if (mLists[0].size() > 0) {
            mHeaderPositions[0] = 0;
        } else {
            mHeaderPositions[0] = -1;
        }

        for (int i = 1; i < mHeaderPositions.length; i++) {
            int lastHeader = -1, lastHeaderIndex = 0;
            for (int j = i - 1; j >= 0 && lastHeader == -1; j--) {
                lastHeader = mHeaderPositions[j];
                lastHeaderIndex = j;
            }

            if (lastHeader == -1) {
                if (mLists[i] != null && mLists[i].size() > 0) {
                    mHeaderPositions[i] = 0;
                } else {
                    mHeaderPositions[i] = -1;
                }
            } else {
                if (mLists[i] != null && mLists[i].size() > 0) {
                    mHeaderPositions[i] = lastHeader + mLists[lastHeaderIndex].size() + 1;
                } else {
                    mHeaderPositions[i] = -1;
                }
            }
        }
        Log.i("AAA", Arrays.toString(mHeaderPositions));
    }

    /*public int getHeaderPosition(int index) {
        return mHeaderPositions[index];
        *//*if (index == 0) {
            if (mLists[index].size() > 0) {
                return 0;
            } else {
                return -1;
            }
        }

        int lastHeader = getHeaderPosition(index - 1);
        if (lastHeader == -1) {
            if (mLists[index] != null && mLists[index].size() > 0) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return lastHeader + mLists[index - 1].size();
        }*//*
    }
*/
    public abstract void onItemSelected(int position, int type, Object item);

    public abstract int getNumLists();

    protected void setList(int index, List list) {
        mLists[index] = list;
    }


    public enum ItemStatus{
        None,
        Selected,
        Locked
    }

    public static abstract class VH<O> extends RecyclerView.ViewHolder {
        public VH(View itemView) {
            super(itemView);
        }

        public abstract void bind(O object, ItemStatus status);
    }

    public static class HeaderVH extends VH<String> {

        TextView vHeaderText;

        public HeaderVH(View itemView) {
            super(itemView);
            vHeaderText = (TextView) itemView.findViewById(R.id.text_header);
        }

        @Override
        public void bind(String object, ItemStatus status) {
            itemView.getLayoutParams().height = object == null ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
//            itemView.setVisibility(object == null ? View.GONE : View.VISIBLE);
            vHeaderText.setText(object);
        }
    }

}
