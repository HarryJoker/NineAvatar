package com.harry.joker.holder.avatar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.harry.joker.nine.avatar.JokerNineAvatar;
import com.harry.joker.nine.avatar.layout.DingLayoutManager;
import com.harry.joker.nine.avatar.layout.WechatLayoutManager;

public class SingleAvatarActivity extends AppCompatActivity {

    private String[] IMG_URL_ARR = {
            "http://img.hb.aicdn.com/eca438704a81dd1fa83347cb8ec1a49ec16d2802c846-laesx2_fw658",
            "http://img.hb.aicdn.com/729970b85e6f56b0d029dcc30be04b484e6cf82d18df2-XwtPUZ_fw658",
            "http://img.hb.aicdn.com/85579fa12b182a3abee62bd3fceae0047767857fe6d4-99Wtzp_fw658",
            "http://img.hb.aicdn.com/2814e43d98ed41e8b3393b0ff8f08f98398d1f6e28a9b-xfGDIC_fw658",
            "http://img.hb.aicdn.com/a1f189d4a420ef1927317ebfacc2ae055ff9f212148fb-iEyFWS_fw658",
            "http://img.hb.aicdn.com/69b52afdca0ae780ee44c6f14a371eee68ece4ec8a8ce-4vaO0k_fw658",
            "http://img.hb.aicdn.com/9925b5f679964d769c91ad407e46a4ae9d47be8155e9a-seH7yY_fw658",
            "http://img.hb.aicdn.com/e22ee5730f152c236c69e2242b9d9114852be2bd8629-EKEnFD_fw658",
            "http://img.hb.aicdn.com/73f2fbeb01cd3fcb2b4dccbbb7973aa1a82c420b21079-5yj6fx_fw658",
    };

    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    ImageView imageView6;
    ImageView imageView7;
    ImageView imageView8;
    ImageView imageView9;
    ImageView imageView10;
    ImageView imageView11;


    private int[] getResourceIds(int count) {
        int[] res = new int[count];
        for (int i = 0; i < count; i++) {
            res[i] = R.mipmap.b;
        }
        return res;
    }

    private String[] getUrls(int count) {
        String[] urls = new String[count];
        System.arraycopy(IMG_URL_ARR, 0, urls, 0, count);
        return urls;
    }

    private Bitmap[] getBitmaps(int count) {
        Bitmap[] bitmaps = new Bitmap[count];
        for (int i = 0; i < count; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), R.mipmap.a);
        }
        return bitmaps;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_avatar);

        imageView1 = findViewById(R.id.nice_iv1);
        imageView2 = findViewById(R.id.nice_iv2);
        imageView3 = findViewById(R.id.nice_iv3);
        imageView4 = findViewById(R.id.iv4);
        imageView5 = findViewById(R.id.iv5);
        imageView6 = findViewById(R.id.iv6);
        imageView7 = findViewById(R.id.iv7);
        imageView8 = findViewById(R.id.iv8);
        imageView9 = findViewById(R.id.iv9);
        imageView10 = findViewById(R.id.iv10);
        imageView11 = findViewById(R.id.iv11);

        loadNineAvatar();
    }

    private void loadNineAvatar() {
        loadDingBitmap(imageView1, 2);

        loadDingBitmap(imageView2, 3);

        loadDingBitmap(imageView3, 4);

        loadWechatBitmap(imageView4, 2);

        loadWechatBitmap(imageView5, 3);

        loadWechatBitmap(imageView6, 4);

        loadWechatBitmap(imageView7, 5);

        loadWechatBitmap(imageView8, 6);

        loadWechatBitmap(imageView9, 7);

        loadWechatBitmap(imageView10, 8);

        loadWechatBitmap(imageView11, 9);

    }


    private void loadWechatBitmap(ImageView imageView, int count) {
        JokerNineAvatar.init(this)
                .setLayoutManager(new WechatLayoutManager())
                .setImageWidth(180)
                .setDividerWidth(3)
                .setDividerColor(Color.parseColor("#E8E8E8"))
                .setUrls(getUrls(count))
                .setImageView(imageView)
                .build();
    }

    private void loadDingBitmap(final ImageView imageView, int count) {
        JokerNineAvatar.init(this)
                .setLayoutManager(new DingLayoutManager())
                .setImageWidth(180)
                .setDividerWidth(2)
                .setUrls(getUrls(count))
                .setImageView(imageView)
                .build();
    }
}
