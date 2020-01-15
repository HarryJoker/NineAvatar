package com.harry.joker.nine.avatar.helper;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

import androidx.annotation.ColorInt;

import com.harry.joker.nine.avatar.R;
import com.harry.joker.nine.avatar.layout.DingLayoutManager;
import com.harry.joker.nine.avatar.layout.ILayoutManager;
import com.harry.joker.nine.avatar.layout.WechatLayoutManager;


public class Builder {
    private static final int DEFAULT_AVATAR              = R.mipmap.ic_avatar;
//    private static final int DEFAULT_NINE_AVATAR         = R.mipmap.ic_nine_avatar;
    private static final int DEFAULT_NINE_AVATAR_WIDTH   = 180;
    private static final int DEFAULT_SINGLE_AVATAR_WIDTH = 0;
    private static final int DEFAULT_DIVIDER_WIDTH       = 10;
    private static final int DEFAFLT_DIVIDER_COLOR       = Color.LTGRAY;

    public Context context;

    public ImageView imageView;

    public Object tag;

    //合成的九宫格图片宽度
    public int imageWidth  = DEFAULT_NINE_AVATAR_WIDTH;

    // 每个小bitmap之间的分割线宽
    public int dividerWidth = DEFAULT_DIVIDER_WIDTH;

    //分割线的颜色
    public int dividerColor = DEFAFLT_DIVIDER_COLOR;

    //获取图片失败时的默认图片
    public int placeholder  = DEFAULT_AVATAR;

    //九宫格的的资源数量
    public int count;

    // 单个bitmap的尺寸
    public int itemWidth = DEFAULT_SINGLE_AVATAR_WIDTH;

    // bitmap的布局样式
    public ILayoutManager layoutManager;

    public String[] urls;

    public RequestOptions mRequestOptions;

    public Builder(Context context) {
        this.context = context;
    }

    public Builder setImageView(ImageView imageView) {
        this.imageView = imageView;
        if (tag != null) {
            this.imageView.setTag(tag);
        }
        return this;
    }

    public Builder setImageWidth(int imageWidth) {
        this.imageWidth = Utils.dp2px(context, imageWidth);
        return this;
    }

    public Builder setDividerWidth(int dividerWidth) {
        this.dividerWidth = Utils.dp2px(context, dividerWidth);
        return this;
    }

    public Builder setDividerColor(@ColorInt int dividerColor) {
        this.dividerColor = dividerColor;
        return this;
    }

    public Builder setPlaceholder(int placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public Builder setLayoutManager(ILayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return this;
    }

    public Builder setUrls(String... urls) {
        this.urls = urls;
        this.count = urls.length;
        return this;
    }

    public Builder setUniqueTag(Object obj) {
        this.tag = obj;
        if (imageView != null) {
            this.imageView.setTag(tag);
        }
        return this;
    }

    public Builder setUrls(RequestOptions requestOptions) {
        this.mRequestOptions = requestOptions;
        if (mRequestOptions != null) {
            this.mRequestOptions.setOnUrlsResponse(mUrlsResponse);
        }
        return this;
    }

    private RequestOptions.OnUrlsResponse mUrlsResponse = new RequestOptions.OnUrlsResponse() {
        @Override
        public void onUrlsResponse(Object tag, String[] urls) {
            resetBuilerForUrls(urls);
        }
    };

    /**
     * 异步请求图片回来，根据urls重新初始化builder进行build加载合成图片
     * 1，更新urls属性
     * 2，更新count属性
     * 3，RequestOptions设置null属性
     * @param urls
     */
    private void resetBuilerForUrls(String[] urls) {
        this.urls = urls;
        this.count = urls == null ? 0 : urls.length;
        mRequestOptions = null;

        JokerLog.d(this.getClass().getSimpleName() + ", Setup async loaded remoteUrls， urls：" + count + "  contine next build to async nineAvatar");

        build();
    }


    public void build() {
        if (imageView == null) {
            throw new NullPointerException("Async JokerBine must ensure the ImageView can not null");
        }

        if (layoutManager == null) {
            layoutManager = new WechatLayoutManager();
        }

        //计算效验单个小头像的宽度
        itemWidth = makeItemWidth(imageWidth, dividerWidth, layoutManager, count);

        if (mRequestOptions == null) {
            NineAvatarHelper.init().load(this);
        } else {
            mRequestOptions.apply(tag);
        }

        JokerLog.d(this.getClass().getSimpleName() + ", Builder build done");
    }


    /**
     * 根据最终生成bitmap的尺寸，计算单个bitmap尺寸
     *
     * @param imageWidth
     * @param dividerWidth
     * @param layoutManager
     * @param count
     * @return
     */
    private int makeItemWidth(int imageWidth, int dividerWidth, ILayoutManager layoutManager, int count) {
        int width = 0;
        if (layoutManager instanceof DingLayoutManager) {
            width = imageWidth;
        } else if (layoutManager instanceof WechatLayoutManager) {
            if (count < 2) {
                width = imageWidth;
            } else if (count < 5) {
                width = (imageWidth - 3 * dividerWidth) / 2;
            } else if (count < 10) {
                width = (imageWidth - 4 * dividerWidth) / 3;
            }
        } else {
            throw new IllegalArgumentException("Must use DingLayoutManager or WechatRegionManager!");
        }
        if (width <= 0 || width > imageWidth) {
            throw new IllegalArgumentException("calucate item width error....");
        }
        return width;
    }
}
