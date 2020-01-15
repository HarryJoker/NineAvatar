package com.harry.joker.nine.avatar.layout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class WechatLayoutManager extends PlaceholderLayoutManager {

    @Override
    public Bitmap makeNineAvatar(int imageWidth, int itemWidth, int dividerWidth, int dividerColor, Bitmap[] bitmaps) {
        Bitmap result = Bitmap.createBitmap(imageWidth, imageWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        if (dividerColor == 0) {
            dividerColor = Color.WHITE;
        }
        canvas.drawColor(dividerColor);

        int count = bitmaps.length;
        Bitmap subBitmap;

        for (int i = 0; i < count; i++) {
            if (bitmaps[i] == null) {
                continue;
            }
            subBitmap = Bitmap.createScaledBitmap(bitmaps[i], itemWidth, itemWidth, true);

            float x = 0;
            float y = 0;

            if (count == 2) {
                x = dividerWidth + i * (itemWidth + dividerWidth);
                y = (imageWidth - itemWidth) / 2.0f;
            } else if (count == 3) {
                if (i == 0) {
                    x = (imageWidth - itemWidth) / 2.0f;
                    y = dividerWidth;
                } else {
                    x = dividerWidth + (i - 1) * (itemWidth + dividerWidth);
                    y = itemWidth + 2 * dividerWidth;
                }
            } else if (count == 4) {
                x = dividerWidth + (i % 2) * (itemWidth + dividerWidth);
                if (i < 2) {
                    y = dividerWidth;
                } else {
                    y = itemWidth + 2 * dividerWidth;
                }
            } else if (count == 5) {
                if (i == 0) {
                    x = y = (imageWidth - 2 * itemWidth - dividerWidth) / 2.0f;
                } else if (i == 1) {
                    x = (imageWidth + dividerWidth) / 2.0f;
                    y = (imageWidth - 2 * itemWidth - dividerWidth) / 2.0f;
                } else if (i > 1) {
                    x = dividerWidth + (i - 2) * (itemWidth + dividerWidth);
                    y = (imageWidth + dividerWidth) / 2.0f;
                }
            } else if (count == 6) {
                x = dividerWidth + (i % 3) * (itemWidth + dividerWidth);
                if (i < 3) {
                    y = (imageWidth - 2 * itemWidth - dividerWidth) / 2.0f;
                } else {
                    y = (imageWidth + dividerWidth) / 2.0f;
                }
            } else if (count == 7) {
                if (i == 0) {
                    x = (imageWidth - itemWidth) / 2.0f;
                    y = dividerWidth;
                } else if (i < 4) {
                    x = dividerWidth + (i - 1) * (itemWidth + dividerWidth);
                    y = itemWidth + 2 * dividerWidth;
                } else {
                    x = dividerWidth + (i - 4) * (itemWidth + dividerWidth);
                    y = dividerWidth + 2 * (itemWidth + dividerWidth);
                }
            } else if (count == 8) {
                if (i == 0) {
                    x = (imageWidth - 2 * itemWidth - dividerWidth) / 2.0f;
                    y = dividerWidth;
                } else if (i == 1) {
                    x = (imageWidth + dividerWidth) / 2.0f;
                    y = dividerWidth;
                } else if (i < 5) {
                    x = dividerWidth + (i - 2) * (itemWidth + dividerWidth);
                    y = itemWidth + 2 * dividerWidth;
                } else {
                    x = dividerWidth + (i - 5) * (itemWidth + dividerWidth);
                    y = dividerWidth + 2 * (itemWidth + dividerWidth);
                }
            } else if (count == 9) {
                x = dividerWidth + (i % 3) * (itemWidth + dividerWidth);
                if (i < 3) {
                    y = dividerWidth;
                } else if (i < 6) {
                    y = itemWidth + 2 * dividerWidth;
                } else {
                    y = dividerWidth + 2 * (itemWidth + dividerWidth);
                }
            }

            canvas.drawBitmap(subBitmap, x, y, null);
        }
        return result;
    }
}
