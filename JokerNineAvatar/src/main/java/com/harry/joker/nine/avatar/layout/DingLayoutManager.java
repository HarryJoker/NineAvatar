package com.harry.joker.nine.avatar.layout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class DingLayoutManager extends PlaceholderLayoutManager {

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

        int[][] dxy = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};

        for (int i = 0; i < count; i++) {
            if (bitmaps[i] == null) {
                continue;
            }
            subBitmap = Bitmap.createScaledBitmap(bitmaps[i], imageWidth, imageWidth, true);
            if (count == 2 || (count == 3 && i == 0)) {
                subBitmap = Bitmap.createBitmap(subBitmap, (imageWidth + dividerWidth) / 4, 0, (imageWidth - dividerWidth) / 2, imageWidth);
            } else if ((count == 3 && (i == 1 || i == 2)) || count == 4) {
                subBitmap = Bitmap.createBitmap(subBitmap, (imageWidth + dividerWidth) / 4, (imageWidth + dividerWidth) / 4, (imageWidth - dividerWidth) / 2, (imageWidth - dividerWidth) / 2);
            }

            int dx = dxy[i][0];
            int dy = dxy[i][1];

            canvas.drawBitmap(subBitmap, dx * (imageWidth + dividerWidth) / 2.0f, dy * (imageWidth + dividerWidth) / 2.0f, null);
        }
        return result;
    }
}
