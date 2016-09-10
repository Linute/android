package com.linute.linute.UtilsAndHelpers;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;

/**
 * Created by mikhail on 9/10/16.
 */
public class TutorialAnimations {

    private static final String KEY_EMPTY_PROFILE_PLAYED = "profile";
    private static final String KEY_EMPTY_FEED_PLAYED = "feed";

    public static boolean isEmptyProfilePlayed(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_EMPTY_PROFILE_PLAYED, false);
    }

    public static boolean isEmptyFeedPlayed(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_EMPTY_FEED_PLAYED, false);
    }

    public static void setIsEmptyProfilePlayed(Context context, boolean b){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_EMPTY_PROFILE_PLAYED, b).apply();
    }
    public static void setIsEmptyFeedPlayed(Context context, boolean b){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_EMPTY_FEED_PLAYED, b).apply();
    }

    public static void animateProfileToCreate(final MainActivity activity, View view){
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
    }


    public static void animateFeedToHottest(final MainActivity activity, View emptyButton){
        final Handler handler = new Handler();

        final int[] pos = new int[2];
        emptyButton.getLocationInWindow(pos);


        final View fairy = activity.findViewById(R.id.fairy_tutorial);
        fairy.setX(pos[0] + emptyButton.getWidth() / 2 - fairy.getWidth() / 2);
        fairy.setY(pos[1] + emptyButton.getHeight() / 2 - fairy.getHeight() / 2);
//                fairy.setX(pos[0]);
//                fairy.setY(pos[1]);
        fairy.setVisibility(View.VISIBLE);

        fairy.bringToFront();
               /* fairy.animate().x(32).y(32).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.GLOBAL);
                    }
                })*/

        Animator moveToDrawerToggleX = ObjectAnimator.ofFloat(fairy, "x", 32);
        Animator moveToDrawerToggleY = ObjectAnimator.ofFloat(fairy, "y", 32);

        moveToDrawerToggleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
                activity.lockDrawer(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                activity.openDrawer();
//                            }
//                        }, 200);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        ((NavigationView) activity.findViewById(R.id.mainActivity_navigation_view)).getMenu().getItem(0).getActionView().getLocationInWindow(pos);

        Animator moveToDrawerItemX = ObjectAnimator.ofFloat(fairy, "y", pos[1]+30);
        moveToDrawerItemX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.GLOBAL);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawer();

                        final View frag = activity.findViewById(R.id.mainActivity_fragment_holder);
                        if (frag != null)
                            frag.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                    final RecyclerView globalrv = (RecyclerView) activity.findViewById(R.id.recycler_view);
                                    final View.OnLayoutChangeListener listener = new View.OnLayoutChangeListener() {
                                        @Override
                                        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                            if (globalrv != null && globalrv.getChildCount() > 0) {
                                                final View hottestView = globalrv.getChildAt(0);
                                                hottestView.getLocationInWindow(pos);
                                                fairy.animate()
                                                        .x(pos[0] + hottestView.getWidth() / 2)
                                                        .y(pos[1])
                                                        .withEndAction(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                hottestView.setPressed(true);
                                                                globalrv.invalidate();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        hottestView.performClick();
                                                                        fairy.setVisibility(View.GONE);
                                                                        activity.lockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);
                                                                    }
                                                                }, 700);
                                                            }
                                                        }).start();
                                                globalrv.removeOnLayoutChangeListener(this);
                                            }
                                        }
                                    };
                                    globalrv.addOnLayoutChangeListener(listener);
                                    frag.removeOnLayoutChangeListener(this);
                                }
                            });
                    }
                }, 300);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


        AnimatorSet set = new AnimatorSet();
        set.play(moveToDrawerToggleX).with(moveToDrawerToggleY);
        set.play(moveToDrawerItemX).after(moveToDrawerToggleX).after(1000);
        set.start();
    }

}
