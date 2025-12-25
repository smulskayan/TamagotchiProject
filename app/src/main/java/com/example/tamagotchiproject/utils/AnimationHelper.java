package com.example.tamagotchiproject.utils;

import android.animation.ValueAnimator;
import android.widget.ProgressBar;

public class AnimationHelper {
    public static void animateProgress(ProgressBar progressBar, int targetValue) {
        int currentProgress = progressBar.getProgress();

        if (currentProgress == targetValue) {
            return;
        }

        ValueAnimator animator = ValueAnimator.ofInt(currentProgress, targetValue);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });
        animator.start();
    }
}