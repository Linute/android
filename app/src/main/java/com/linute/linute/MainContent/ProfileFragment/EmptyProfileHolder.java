package com.linute.linute.MainContent.ProfileFragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
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
                final View fairy = activity.findViewById(R.id.fairy_tutorial);
                final int[] pos = new int[2];
                view.getLocationOnScreen(pos);
                fairy.setX(pos[0]);
                fairy.setY(pos[1]);
                fairy.setVisibility(View.VISIBLE);
                Animator moveToDrawerX = ObjectAnimator.ofFloat(fairy, "x", 32);
                Animator moveToDrawerY = ObjectAnimator.ofFloat(fairy, "y", 32);
                moveToDrawerY.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        activity.getSupportFragmentManager().popBackStack();
                        activity.openDrawer();
                        activity.lockDrawer(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });

                ((NavigationView) activity.findViewById(R.id.mainActivity_navigation_view)).getMenu().getItem(0).getActionView().getLocationInWindow(pos);
                Animator moveToDrawerItemX = ObjectAnimator.ofFloat(fairy, "y", pos[1] - 30);
                moveToDrawerItemX.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.FEED);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {


                                final View frag = activity.findViewById(R.id.mainActivity_fragment_holder);
                                frag.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                        final FloatingActionsMenu createFab = (FloatingActionsMenu)activity.findViewById(R.id.create_menu);
                                        if(createFab != null){
                                            frag.removeOnLayoutChangeListener(this);

                                            createFab.getLocationOnScreen(pos);

                                            fairy.animate().x(pos[0]).y(pos[1]+createFab.getHeight()*3/4).withEndAction(new Runnable() {
                                                @Override
                                                public void run() {
                                                    createFab.expand();
                                                    fairy.setVisibility(View.GONE);
                                                }
                                            }).start();
                                        }
                                    }
                                });

                                activity.lockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);
                                activity.closeDrawer();

                                frag.requestLayout();

                                /*new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        View createFab = activity.findViewById(R.id.create_menu);
                                        createFab.performClick();
                                    }
                                }, 200);*/
                            }
                        }, 500);


                   /*     new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.lockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);
                                activity.closeDrawer();

                            }
                        }, 500);*/
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

                AnimatorSet set = new AnimatorSet();
                set.play(moveToDrawerX).with(moveToDrawerY);
                set.play(moveToDrawerItemX).after(moveToDrawerX).after(1000);
                set.start();

                /* activity.openDrawer();
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
                }, 1200);*/

            }
        });
    }

}
