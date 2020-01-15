package com.harry.joker.nine.avatar.layout;

import android.graphics.Bitmap;

/**
 * Author: harryjoker
 * Created on: 2020-01-15 11:18
 * Description:
 */
public abstract class PlaceholderLayoutManager implements ILayoutManager {

    public Bitmap makePlaceholderAvatar(int imageWidth, int itemWidth, int dividerWidth, int dividerColor, int count, Bitmap placeholder) {
        Bitmap[] placeholders = new Bitmap[count];
        for (int n = 0; n < count; n++) {
            placeholders[n] = placeholder;
        }
        return makeNineAvatar(imageWidth, itemWidth, dividerWidth, dividerColor, placeholders);
    }
}
