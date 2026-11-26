package com.harsh.pdfstudio.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public class Dp_to_Px {

    public  static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }
}
