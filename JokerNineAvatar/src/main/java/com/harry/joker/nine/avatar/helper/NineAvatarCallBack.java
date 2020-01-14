package com.harry.joker.nine.avatar.helper;

import android.graphics.Bitmap;
import android.os.Handler;

import com.harry.joker.nine.avatar.listener.OnHandlerListener;

public abstract class NineAvatarCallBack {

    private int i = 0;
    private Bitmap[] bitmaps;
    private Handler mHandler = new Handler();

    public NineAvatarCallBack(int count) {
         bitmaps = new Bitmap[count];
    }

    /**
     * 每个子线程loading完的回调事件
     * @param index
     * @param bitmap
     */
    public void onTaskCompeleteBitmap(int index, Bitmap bitmap) {
        bitmaps[index] = bitmap;
        i++;
        if (i == bitmaps.length) {
            final Bitmap nineAvatar = onAsyncCompelteBitmaps(bitmaps);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCompeleteNineBitmap(nineAvatar);
                }
            });
        }
    }

    /**
     * 将合成的九宫格图片通知页面刷新
     * @param avatar
     */
    public abstract void onCompeleteNineBitmap(Bitmap avatar);


    /**
     * 异步将九宫格图片合成
     * @param bitmaps
     * @return
     */
    public abstract Bitmap onAsyncCompelteBitmaps(Bitmap[] bitmaps);
}
