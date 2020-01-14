package com.harry.joker.nine.avatar.layout;

import android.graphics.Bitmap;

public interface ILayoutManager {
    Bitmap makeNineAvatar(int size, int subSize, int gap, int gapColor, Bitmap[] bitmaps);
}
