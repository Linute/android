package com.linute.linute.MainContent.EditScreen;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/22/16.
 */
public class EditContentToolAdapter extends RecyclerView.Adapter<EditContentToolAdapter.ToolHolder>{



    private EditContentTool[] tools;

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setTools(EditContentTool[] tools) {
        this.tools = tools;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener){
        mOnItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public ToolHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ToolHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_tool, parent, false));
    }

    @Override
    public void onBindViewHolder(final ToolHolder holder, int position) {
        tools[position].bindMenuItem(holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemSelectedListener != null);
                mOnItemSelectedListener.onItemSelected(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tools.length;
    }

    public static class ToolHolder extends RecyclerView.ViewHolder{

        public ImageView vIcon;
        public TextView vLabel;

        public ToolHolder(View itemView) {
            super(itemView);
            vIcon = (ImageView)itemView.findViewById(R.id.image_icon);
            vLabel = (TextView) itemView.findViewById(R.id.text_label);

        }
    }

    interface OnItemSelectedListener {
        void onItemSelected(int i);
    }

}
