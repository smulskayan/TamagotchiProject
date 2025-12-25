package com.example.tamagotchiproject.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.tamagotchiproject.R;

public class UIHelper {
    public static void setColoredText(TextView textView, String word, Context context) {
        SpannableString spannableString = new SpannableString(word);

        int[] colorResources = {
                R.color.yellow,
                R.color.green,
                R.color.blue,
                R.color.pink
        };

        for (int i = 0; i < word.length(); i++) {
            int colorIndex = i % colorResources.length;
            int color = ContextCompat.getColor(context, colorResources[colorIndex]);
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            spannableString.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
    }
}