package com.harry.joker.nine.avatar.helper;

import android.content.Context;
import android.graphics.Bitmap;
import com.harry.joker.nine.avatar.remote.Options;
import com.harry.joker.nine.avatar.remote.RemoteLoader;

public class NineAvatarHelper {

    public static NineAvatarHelper init() {
        return NineAvatarHelper.SingletonHolder.instance;
    }

    private NineAvatarHelper(){

    }

    private static class SingletonHolder {
        private static final NineAvatarHelper instance = new NineAvatarHelper();
    }

    /**
     * 通过builder加载
     *
     * @param builder
     */
    private void loadNineAvatarByUrls(final Builder builder) {
        BitmapLoader.getInstance(builder.context).aysncLoadBuilder(builder, new Builder.OnNineAvatarCallback(){
            @Override
            public void onCompeleteAvatar(String tip, Bitmap bitmap) {
                refreshNineAvatar(builder, bitmap, tip);
            }
        });
    }

    public void load(Builder builder) {
        if (builder.urls != null && builder.urls.length > 0) {
            loadNineAvatarByUrls(builder);
        }
    }

    public void loadOptions(Context context, Options options, Options.OnRemoteCallback callback) {
        RemoteLoader.getInstance(context).asyncLoadRemote(options.mUrl, options.mMethod, options.mParams, options.mHeaders, options.mParser, callback);
    }

    /**
     * RecycleView通过Tag找到在屏幕中显示的ViewHolder进行设置Bitmap
     * 避免ViewHolder数据已经更新，异步的ImageLoad仍然处理返回的是已经更换数据源的ViewHolder中的ImageView
     *
     * @param b
     * @param result
     */
    private void refreshNineAvatar(Builder b, Bitmap result, String tip) {
         // 给ImageView设置最终的组合Bitmap
        StringBuilder builder = new StringBuilder();
        builder.append("position: " + b.tag + ", " + tip + "， ");
        if (b != null && b.tag != null && b.imageView != null && b.imageView.getTag() != null) {
            builder.append("ImageTag: " + b.imageView.getTag() + ", urls:" + b.count + ", Task Tag: " + b.tag);
            if (b.tag.equals(b.imageView.getTag())) {
                b.imageView.setImageBitmap(result);
            }
        } else {
            builder.append("ImageTag: null  , Task Tag: " + b.tag + "\n");
        }
        JokerLog.d(this.getClass().getSimpleName() + ", " + builder.toString());
    }

    /**
     * 独立的ImageView直接设置
     * @param b
     * @param bitmaps
     */
//    private void setBitmap(final Builder b, Bitmap[] bitmaps) {
//        Bitmap result = b.layoutManager.combineBitmap(b.imageWidth, b.itemWidth, b.dividerWidth, b.dividerColor, bitmaps);
//
//        // 返回最终的组合Bitmap
//        if (b.progressListener != null) {
//            b.progressListener.onComplete(result);
//        }
//
//        // 给ImageView设置最终的组合Bitmap
//        if (b.imageView != null) {
//            b.imageView.setImageBitmap(result);
//        }
//    }

    /**
     * RecycleView通过Tag找到在屏幕中显示的ViewHolder进行设置Bitmap
     * 避免ViewHolder数据已经更新，异步的ImageLoad仍然处理返回的是已经更换数据源的ViewHolder中的ImageView
     *
     * @param b
     * @param bitmaps
     */
//    private void setBitmapForRecycleView(final Builder b, Bitmap[] bitmaps) {
//        Bitmap result = b.layoutManager.combineBitmap(b.imageWidth, b.itemWidth, b.dividerWidth, b.dividerColor, bitmaps);
//
//        // 返回最终的组合Bitmap
//        if (b.progressListener != null) {
//            b.progressListener.onComplete(result);
//        }
//
//        // 给ImageView设置最终的组合Bitmap
//        StringBuilder builder = new StringBuilder();
//        builder.append("position: " + b.forPosistion + "\n");
////        RecyclerView.ViewHolder viewHolder = ((RecyclerView)b.targetRoot).findViewWithTag(b.forPosistion);
////        builder.append("ViewHolder:" + viewHolder + "\n");
//        RecyclerView.ViewHolder viewHolder = ((RecyclerView)b.targetRoot).findViewHolderForAdapterPosition(b.forPosistion);
//        builder.append("findViewHolderForAdapterPosition, position:" + b.forPosistion + ", " + (viewHolder == null ? "null" : viewHolder) +"\n");
//
//        if (viewHolder == null) {
//            LinearLayoutManager layoutManager = (LinearLayoutManager) ((RecyclerView)b.targetRoot).getLayoutManager();
//            int position = b.forPosistion - (layoutManager.findLastVisibleItemPosition()  - layoutManager.findFirstVisibleItemPosition());
//            viewHolder = ((RecyclerView)b.targetRoot).findViewHolderForAdapterPosition(position);
//            builder.append("First Visible Position:" + layoutManager.findFirstVisibleItemPosition() + ", last Visible Position:" + layoutManager.findLastVisibleItemPosition() + "\n");
//            builder.append("Visible for position:" + position + ", viewHolder:" + viewHolder  + "\n");
//        }
//        if (viewHolder != null && viewHolder instanceof BaseAvatarViewHolder) {
//            BaseAvatarViewHolder avatarViewHolder = (BaseAvatarViewHolder)viewHolder;
//            builder.append("Combine:  Position:" + b.forPosistion + ", Size:" + b.urls.length + ", bitmaps:" + bitmaps.length + "\n " +
//                              "  Text:  " + avatarViewHolder.getText(b.forPosistion) + "\n");
//            builder.append("AvatarViewHolder ImageView: " + avatarViewHolder.getAvatarImageView() + "\n");
////        builder.append("findViewHolderForLayoutPosition: " +  ((RecyclerView)b.targetRoot).findViewHolderForLayoutPosition(b.forPosistion) + "\n");
////        builder.append("findViewHolderForAdapterPosition: " +  ((RecyclerView)b.targetRoot).findViewHolderForAdapterPosition(b.forPosistion) + "\n");
////        builder.append("findViewHolderForItemId: " +  ((RecyclerView)b.targetRoot).findViewHolderForItemId(b.forPosistion) + "\n");
//            if (avatarViewHolder.getAvatarImageView() != null) {
//                avatarViewHolder.getAvatarImageView().setImageBitmap(result);
//            }
//
////            ((RecyclerView)b.targetRoot).getAdapter().notifyDataSetChanged();
//
//        }
//        Log.d("Tag", builder.toString());
//    }
}
