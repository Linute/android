package com.linute.linute.LoginAndSignup.SignUpFragments.CollegeSearch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.LoginAndSignup.College;
import com.linute.linute.LoginAndSignup.CollegeListAdapter;
import com.linute.linute.R;

import java.util.ArrayList;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpCollegeAdapter extends RecyclerView.Adapter<CollegeListAdapter.CollegeViewHolder> {

    private ArrayList<College> mColleges;
    private OnCollegeSelected mOnCollegeSelected;

    public SignUpCollegeAdapter(ArrayList<College> colleges){
        mColleges = colleges;
    }

    public void setOnCollegeSelected(OnCollegeSelected onCollegeSelected){
        mOnCollegeSelected = onCollegeSelected;
    }

    @Override
    public CollegeListAdapter.CollegeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CollegeListAdapter.CollegeViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.college_picker_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CollegeListAdapter.CollegeViewHolder holder, int position) {
        final College college = mColleges.get(position);
        holder.bindView(college);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCollegeSelected != null)
                    mOnCollegeSelected.onCollegeSelected(college);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColleges.size();
    }


    public interface OnCollegeSelected{
        void onCollegeSelected(College college);
    }
}
