package com.linute.linute.MainContent.EditScreen;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
public class EditContentToolAdapter extends RecyclerView.Adapter<EditContentToolAdapter.ToolHolder> {


    private EditContentTool[] tools;
    int mSelectedItem = 0;

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setTools(EditContentTool[] tools) {
        this.tools = tools;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
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
        holder.bind(tools[position], position == mSelectedItem);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectItem(holder.getAdapterPosition());

            }
        });
    }


    public void selectItem(int index) {
        int oldSelection = mSelectedItem;
        mSelectedItem = index;

        notifyItemChanged(oldSelection);
        notifyItemChanged(mSelectedItem);

        if (mOnItemSelectedListener != null)
            mOnItemSelectedListener.onItemSelected(index);

        tools[oldSelection].onClose();
        tools[mSelectedItem].onOpen();

    }

    @Override
    public int getItemCount() {
        return tools.length;
    }

    public static class ToolHolder extends RecyclerView.ViewHolder {

        public ImageView vIcon;
        public TextView vLabel;

        public ToolHolder(View itemView) {
            super(itemView);
            vIcon = (ImageView) itemView.findViewById(R.id.image_icon);
            vLabel = (TextView) itemView.findViewById(R.id.text_label);

        }

        public void bind(EditContentTool tool, boolean isSelected) {
            vLabel.setText(tool.getName());
            vIcon.setImageResource(tool.getDrawable());

            if (isSelected) {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.secondaryColor));
                vIcon.setColorFilter(new
                        PorterDuffColorFilter(vIcon.getResources().getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
            } else {
                vLabel.setTextColor(vLabel.getResources().getColor(R.color.pure_white));
                vIcon.setColorFilter(null);
            }
        }
    }

    interface OnItemSelectedListener {
        void onItemSelected(int i);
    }

}
