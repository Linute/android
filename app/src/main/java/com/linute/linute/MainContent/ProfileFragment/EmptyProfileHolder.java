package com.linute.linute.MainContent.ProfileFragment;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyProfileHolder extends RecyclerView.ViewHolder {

    public static MainActivity activity;

    public EmptyProfileHolder(View itemView) {
        super(itemView);
        ((ImageView) itemView.findViewById(R.id.empty_cell_bg_image)).setImageResource(R.drawable.ic_drama);
        ((TextView) itemView.findViewById(R.id.empty_cell_bg_text)).setText(R.string.profile_no_posts);
        ((Button) itemView.findViewById(R.id.empty_cell_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // activity.getSupportFragmentManager().popBackStack();
                activity.openDrawer();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.FEED);
                        // activity.findViewById(R.id.create_menu).performClick();
                    }
                }, 600);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawer();
                        // activity.findViewById(R.id.create_menu).performClick();
                        final View fragmentLayout = activity.findViewById(R.id.mainActivity_fragment_holder);
                        fragmentLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                if(activity.findViewById(R.id.create_menu) != null) {
                                    fragmentLayout.removeOnLayoutChangeListener(this);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            View createFab = activity.findViewById(R.id.create_menu);
                                            createFab.performClick();
                                        }
                                    }, 200);
                                }
                            }
                        });
                    }
                }, 1200);




            }
        });
    }

}
