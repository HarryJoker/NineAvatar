package com.harry.joker.nine.avatar.helper;

import android.graphics.Bitmap;

import com.harry.joker.nine.avatar.layout.ILayoutManager;
import com.harry.joker.nine.avatar.listener.OnMuilteCompeleteListener;

import java.util.List;
import java.util.Map;

/**
 * Author: harryjoker
 * Created on: 2020-01-16 22:01
 * Description:
 */
public class MultiImage {

    final static int maxSize = 9;
    private int i = 0;
    private Bitmap[] bitmaps;
    private OnMuilteCompeleteListener mMuilteCompeleteListener;

    public MultiImage(int count, OnMuilteCompeleteListener listener) {
        this.bitmaps = new Bitmap[count > 9 ? 9 : count];
        this.mMuilteCompeleteListener = listener;
    }


    /**
     * 每个Task loading到的Image
     * @param index
     * @param bitmap
     */
    public void putTaskCompeleteBitmap(int index, Bitmap bitmap) {
        bitmaps[index] = bitmap;
        i++;

        if (i == bitmaps.length && mMuilteCompeleteListener != null) {
            mMuilteCompeleteListener.onMuilteCompelete(bitmaps);
        }
    }

    public boolean isCompeleteMuilte() {
        return i == bitmaps.length;
    }
}
