package com.harry.joker.nine.avatar.listener;

import android.graphics.Bitmap;

public interface OnNineAvatarCallback {
    //合成的九宫格头像
    void onHanldeAvatar(Bitmap result);
    //加载过程中，展示合成的九宫格占位符头像
    void onHanldePlaceholder(Bitmap placeholder);
}
