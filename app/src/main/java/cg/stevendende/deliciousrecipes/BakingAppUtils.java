package cg.stevendende.deliciousrecipes;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by STEVEN on 14/06/2017.
 */

public class BakingAppUtils {
    /**
     * Converts a given density (dp) in pixels
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts a given size in Px to dp
     *
     * @param px
     * @return
     */
    public int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
