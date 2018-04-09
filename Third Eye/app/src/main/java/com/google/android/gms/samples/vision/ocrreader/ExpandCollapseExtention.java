package com.google.android.gms.samples.vision.ocrreader;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by root on 3/29/18.
 */

public class ExpandCollapseExtention {

    public static void expand(View view) {
        view.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        int height = 360;
        ValueAnimator mAnimator = slideAnimator(view, 0, height);
        mAnimator.setDuration(500);
        mAnimator.start();
    }


    public static void Fullexpand(View view) {
        view.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        int height = 1020;
        ValueAnimator mAnimator = slideAnimator(view, 0, height);
        mAnimator.setDuration(500);
        mAnimator.start();
    }


    public static void collapse(final View view) {
        int finalHeight = view.getHeight();

        ValueAnimator mAnimator = slideAnimator(view, finalHeight, 0);
        mAnimator.setDuration(500);
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }


            @Override
            public void onAnimationStart(Animator animation) {

            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }


            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }


    private static ValueAnimator slideAnimator(final View v, int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}