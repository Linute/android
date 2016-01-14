package com.linute.linute.LoginAndSignup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;

import java.util.List;

/**
 * Created by QiFeng on 1/12/16.
 */
public class CollegeListAdapter extends RecyclerView.Adapter<CollegeListAdapter.CollegeViewHolder> {

    private List<College> mCollegeList;
    private Context mContext;

    public CollegeListAdapter(Context context, List<College> colleges) {
        mContext = context;
        mCollegeList = colleges;
    }

    @Override
    public CollegeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CollegeViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.college_picker_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CollegeViewHolder holder, int position) {
        holder.bindView(mContext, mCollegeList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCollegeList.size();
    }



    public static class CollegeViewHolder extends RecyclerView.ViewHolder{
        public TextView mCollegeName;
        public View mAddButton;

        public CollegeViewHolder(View itemView) {
            super(itemView);

            mCollegeName = (TextView) itemView.findViewById(R.id.collegePicker_item_name_text);
            mAddButton = itemView.findViewById(R.id.collegePicker_add_icon);
        }

        public void bindView(final Context context, final College college){
            mCollegeName.setText(college.getCollegeName());

            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //show confirmation dialog
                    ((CollegePickerActivity) context)
                            .showConfirmationDialog(college.getCollegeName(), college.getCollegeId());
                }
            });
        }
    }
}
