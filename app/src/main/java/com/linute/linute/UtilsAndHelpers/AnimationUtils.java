package com.linute.linute.UtilsAndHelpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by mikhail on 11/5/16.
 */

public class AnimationUtils {

    public static double getMax(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void animateLollipop(final View v, int x, int y, float radius) {
        v.setAlpha(.33f);
        Animator animator = ViewAnimationUtils.createCircularReveal(v, x, y, 0, radius);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.animate().alpha(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                v.setVisibility(View.INVISIBLE);
            }
        });

        v.setVisibility(View.VISIBLE);
        animator.start();
    }

    public static void animatePreLollipop(final View layer) {
        AlphaAnimation a = new AlphaAnimation(0.0f, 0.75f);
        a.setDuration(400);

        final AlphaAnimation a2 = new AlphaAnimation(0.75f, 0.0f);
        a2.setDuration(200);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layer.startAnimation(a2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        layer.startAnimation(a);
    }

}
