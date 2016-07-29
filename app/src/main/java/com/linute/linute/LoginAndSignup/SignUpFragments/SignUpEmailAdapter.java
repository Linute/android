package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;


import java.util.ArrayList;

/**
 * Created by QiFeng on 7/29/16.
 */
public class SignUpEmailAdapter extends RecyclerView.Adapter<SignUpEmailAdapter.SuffixViewHolder> {

    ArrayList<String> mEmails;
    EmailSelected mEmailSelected;

    public SignUpEmailAdapter(ArrayList<String> emails, EmailSelected emailSelected){
        mEmails= emails;
        mEmailSelected = emailSelected;
    }

    @Override
    public SuffixViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new SuffixViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.email_suffix_view, parent, false));
    }

    @Override
    public void onBindViewHolder(SuffixViewHolder holder, int position) {
        holder.onBind(mEmails.get(position));
    }

    @Override
    public int getItemCount() {
        return mEmails.size();
    }

    public class SuffixViewHolder extends RecyclerView.ViewHolder {

        private String mEmail;

        private TextView mTextView;

        public SuffixViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmail != null && mEmailSelected != null){
                        mEmailSelected.onEmailSelected(mEmail);
                    }
                }
            });


            mTextView = (TextView) itemView.findViewById(R.id.text);
        }

        public void onBind(String email){
            mTextView.setText(email);
            mEmail = email;
        }

    }

    public interface EmailSelected{
        void onEmailSelected(String email);
    }
}
