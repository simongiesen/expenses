/*
 * Copyright © 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import ro.expectations.expenses.R;

public class DrawableUtils {

    public static Drawable tint(Context context, @DrawableRes int resId, @ColorRes int colorId) {
        return tint(context, ContextCompat.getDrawable(context, resId), colorId);
    }

    public static Drawable tint(Context context, Drawable drawable, @ColorRes int colorId) {
        return tintWithColor(drawable, ContextCompat.getColor(context, colorId));
    }

    public static Drawable tintWithColor(Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static @DrawableRes int getIdentifier(Context context, String iconName, @DrawableRes int defaultResourceId) {
        int iconResourceId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        if (iconResourceId == 0) {
            iconResourceId = defaultResourceId;
        }

        return iconResourceId;
    }

    public static @DrawableRes int getIdentifier(Context context, String iconName) {
        return getIdentifier(context, iconName, R.drawable.ic_question_mark_black_24dp);
    }
}
