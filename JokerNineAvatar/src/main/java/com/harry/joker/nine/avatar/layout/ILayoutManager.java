package com.harry.joker.nine.avatar.layout;

import android.graphics.Bitmap;

public interface ILayoutManager {

    Bitmap makeNineAvatar(int imageWidth, int itemWidth, int dividerWidth, int dividerColor, Bitmap[] bitmaps);

    Bitmap makePlaceholderAvatar(int imageWidth, int itemWidth, int dividerWidth, int dividerColor, int count, Bitmap placeholder);
}
