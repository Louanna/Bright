package com.movit.platform.mail.ui.color;

import android.graphics.Color;

/**
 * Created by jamison on 2016/6/12.
 */
public class ColorPicker {

    private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
            0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };


    /**
     * Get a random color.
     *
     * @return The ARGB value of a randomly selected color.
     */
    public static int getRandomColor() {
        return calculateColor((float) (Math.random() * 2 * Math.PI));
    }

    private static int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    /**
     * Calculate the color using the supplied angle.
     *
     * @param angle
     *         The selected color's position expressed as angle (in rad).
     *
     * @return The ARGB value of the color on the color wheel at the specified angle.
     */
    private static int calculateColor(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }

        if (unit <= 0) {
            return COLORS[0];
        }
        if (unit >= 1) {
            return COLORS[COLORS.length - 1];
        }

        float p = unit * (COLORS.length - 1);
        int i = (int) p;
        p -= i;

        int c0 = COLORS[i];
        int c1 = COLORS[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        return Color.argb(a, r, g, b);
    }

}
