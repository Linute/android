package com.linute.linute.MainContent.ProfileFragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.TutorialAnimations;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyProfileHolder extends RecyclerView.ViewHolder {

    public static MainActivity activity;

    public EmptyProfileHolder(View itemView, boolean isOwner) {
        super(itemView);
        ((ImageView) itemView.findViewById(R.id.empty_cell_bg_image)).setImageResource(R.drawable.ic_drama);
        ((TextView) itemView.findViewById(R.id.empty_cell_bg_text)).setText(isOwner ?
                R.string.profile_owner_no_posts : R.string.profile_not_owner_no_posts);

        View button = (itemView.findViewById(R.id.empty_cell_button));

        if (isOwner) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TutorialAnimations.isEmptyProfilePlayed(activity)) {
                        TutorialAnimations.animateProfileToCreate(activity, view);
                        TutorialAnimations.setIsEmptyProfilePlayed(activity, true);
                    } else {
                        activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.FEED);
                        activity.replaceContainerWithFragment(activity.getFragment(MainActivity.FRAGMENT_INDEXES.FEED));
                    }
                }
            });
            button.setVisibility(View.VISIBLE);
        }else {
            button.setVisibility(View.GONE);
        }

    }

}
