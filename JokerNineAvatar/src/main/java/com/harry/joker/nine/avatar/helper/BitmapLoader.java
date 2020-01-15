package com.harry.joker.nine.avatar.helper;

import android.content.Context;
import android.graphics.Bitmap;

import com.harry.joker.nine.avatar.cache.DiskLruCacheHelper;
import com.harry.joker.nine.avatar.cache.LruCacheHelper;
import com.harry.joker.nine.avatar.listener.OnNineAvatarCallback;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitmapLoader {
    private static String TAG = BitmapLoader.class.getSimpleName();
    private static final int BUFFER_SIZE = 10 * 1024;

    private LruCacheHelper lruCacheHelper;
    private DiskLruCache mDiskLruCache;
    private CompressHelper compressHelper;

    private volatile static BitmapLoader manager;

    private Context mContext;

    public static BitmapLoader getInstance(Context context) {
        if (manager == null) {
            synchronized (BitmapLoader.class) {
                if (manager == null) {
                    manager = new BitmapLoader(context);
                }
            }
        }
        return manager;
    }

    // 存储线程池中的任务
    private Map<String, Runnable> doingTasks;
    // 存储暂时不能进入线程池的任务
    private Map<String, List<Runnable>> undoTasks;


    private BitmapLoader(Context context) {
        this.mContext = context;
        mDiskLruCache = new DiskLruCacheHelper(context).mDiskLruCache;
        lruCacheHelper = new LruCacheHelper();
        compressHelper = CompressHelper.getInstance();

        doingTasks = new HashMap<>();
        undoTasks = new HashMap<>();
    }

//    public void asyncLoadBuilder(final Builder builder) {
//        Bitmap defaultBitmap = null;
//        if (builder.placeholder != 0) {
//            defaultBitmap = CompressHelper.getInstance().compressResource(builder.context.getResources(), builder.placeholder, builder.itemWidth, builder.itemWidth);
//        }
//        ProgressHandler handler = new ProgressHandler(defaultBitmap, builder.count, new OnHandlerListener() {
//            @Override
//            public void onComplete(Bitmap[] bitmaps) {
////                saveBuildSynthesizedBitmap(builder, bitmaps);
//            }
//        });
//        for (int i = 0; i < builder.count; i++) {
//            BitmapLoader.getInstance(builder.context).asyncLoad(i, builder.urls[i],  builder.itemWidth,  builder.itemWidth, handler);
//        }
//    }

//    public Bitmap syncLoadBuilder(Builder builder) {
//
//        String urlKey = buildTargetSynthesizedId(builder.urls);
//
//        // 尝试从内存缓存中读取
//        String key = Utils.hashKeyFormUrl(urlKey);
//        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
//        if (bitmap != null) {
//            Log.e(TAG, "load from memory:" + urlKey);
//            return bitmap;
//        }
//
//        // 尝试从磁盘缓存中读取
//        bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
//        if (bitmap != null) {
//            Log.e(TAG, "load from disk:" + url);
//            return bitmap;
//        }
//
//    }

//    private void saveBuildSynthesizedBitmap(Builder builder, Bitmap[] bitmaps) {
//        Bitmap result = builder.layoutManager.combineBitmap(builder.imageWidth, builder.itemWidth, builder.dividerWidth, builder.dividerColor, bitmaps);
//        String buildSynthesizedId = buildTargetSynthesizedId(builder.urls);
//        try {
//            result = saveBitmap(buildSynthesizedId, result);
//            Log.d("NicePic", "bitmap:" + result);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 从缓存中查找合成的图片
     * @param urls
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmapForUrls(String[] urls, int reqWidth, int reqHeight) {
        String url = buildSynthesizedUrl(urls);
        // 尝试从内存缓存中读取
        String key = Utils.hashKeyFormUrl(url);
        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
        if (bitmap != null) {
            JokerLog.d(this.getClass().getSimpleName() + ", load NineAvatar image from memory");
            return bitmap;
        }

        try {
            // 尝试从磁盘缓存中读取
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            if (bitmap != null) {
                JokerLog.d(this.getClass().getSimpleName() + " load NineAvatar from disk");
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap loadBuilderFromCache(Builder builder) {
        return loadBitmapForUrls(builder.urls, builder.imageWidth, builder.imageWidth);
    }

    public void aysncLoadBuilder(final Builder builder, final OnNineAvatarCallback callback) {
        //Muilte avatar DownLoad finish Callback
        NineAvatarCallBack nineAvatarCallBack = new NineAvatarCallBack(builder.count) {
            @Override
            public void onCompeleteNineBitmap(Bitmap avatar) {
                if (callback != null) {
                    callback.onHanldeAvatar(avatar);
                }
            }

            @Override
            public Bitmap onAsyncCompelteBitmaps(Bitmap[] bitmaps) {
                Bitmap result = builder.layoutManager.makeNineAvatar(builder.imageWidth, builder.itemWidth, builder.dividerWidth, builder.dividerColor, bitmaps);
                cacheNineAvatar(builder.urls, result);
                return result;
            }
        };

        //取缓存
        Bitmap result = loadBuilderFromCache(builder);
        if (result != null) {
            callback.onHanldeAvatar(result);
            return;
        }

        //生成PlaceHolder占位符
        Bitmap placeholder = loadBitmapFromRes(builder.placeholder, builder.itemWidth, builder.itemWidth);
        result = builder.layoutManager.makePlaceholderAvatar(builder.imageWidth, builder.itemWidth, builder.dividerWidth, builder.dividerColor, builder.count, placeholder);
        if (result != null) {
            callback.onHanldeAvatar(result);
        }

        //取每个格子的图片
        for (int index = 0; index < builder.urls.length; index++) {
            asyncLoadBitmap(index, builder.urls[index], builder.placeholder, builder.itemWidth, builder.itemWidth, nineAvatarCallBack);
        }
    }

    private void asyncLoadBitmap(final int index, final String url, final int place, final int reqWidth, final int reqHeight, final NineAvatarCallBack nineAvatarCallBack) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);
                if (bitmap == null) {
                    //加载图像用占位符替换
                    bitmap = loadBitmapFromRes(place, reqWidth, reqHeight);
                }
                nineAvatarCallBack.onTaskCompeleteBitmap(index, bitmap);
            }
        };

        if (collectUndoTasks(url, task)) {
            return;
        }

        ThreadPool.getInstance().execute(task);
    }

    private void cacheNineAvatar(String[] urls, Bitmap bitmap) {
        if (urls == null || bitmap == null) return;
        String url = buildSynthesizedUrl(urls);
        String key = Utils.hashKeyFormUrl(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                editor.commit();
                mDiskLruCache.flush();

                JokerLog.d(this.getClass().getSimpleName() +", NineAvatar image to Cache ........");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String int2Url(int res) {
        return "harry_bine_avatar_load_from_res:" + res;
    }

    /**
     * 读取缓存：优先内存读取，然后本地读取
     * @param res
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmapFromRes(int res, int reqWidth, int reqHeight) {
        String url = int2Url(res);
        // 尝试从内存缓存中读取
        String key = Utils.hashKeyFormUrl(url);
        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
        if (bitmap != null) {
            JokerLog.d(this.getClass().getSimpleName() + " load Res from memory:" + url);
            return bitmap;
        }

        try {
            // 尝试从磁盘缓存中读取
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            if (bitmap != null) {
                JokerLog.d(this.getClass().getSimpleName() + " load Res from disk:" + url);
                return bitmap;
            }

            bitmap = CompressHelper.getInstance().compressResource(mContext.getResources(), res, reqWidth, reqHeight);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                editor.commit();
                mDiskLruCache.flush();
            }
            if (bitmap != null) {
                JokerLog.d(this.getClass().getSimpleName() + " load Res from Resource :" + url);
                return bitmap;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 生成合成图片的id，保证唯一性
     */
    private String buildSynthesizedUrl(String[] urls) {
        StringBuffer buffer = new StringBuffer();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                Object imageUrl = urls[i];
                buffer.append(i + "" + imageUrl);
            }
        }
        return buffer.toString();
    }

    private Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        // 尝试从内存缓存中读取
        String key = Utils.hashKeyFormUrl(url);
        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
        if (bitmap != null) {
            JokerLog.d(this.getClass().getSimpleName() + " load from memory:" + url);
            return bitmap;
        }

        try {
            // 尝试从磁盘缓存中读取
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            if (bitmap != null) {
                JokerLog.d(this.getClass().getSimpleName() + " load from disk:" + url);
                return bitmap;
            }
            // 尝试下载
            bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
            if (bitmap != null) {
                JokerLog.d(this.getClass().getSimpleName() + " load from http:" + url);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        String key = Utils.hashKeyFormUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();

            executeUndoTasks(url);
        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    private boolean downloadUrlToStream(String urlString,
                                        OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            JokerLog.d(this.getClass().getSimpleName() + " downloadBitmap failed." + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Utils.close(out);
            Utils.close(in);
        }
        return false;
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        Bitmap bitmap = null;
        String key = Utils.hashKeyFormUrl(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(0);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = compressHelper.compressDescriptor(fileDescriptor, reqWidth, reqHeight);

            if (bitmap != null) {
                lruCacheHelper.addBitmapToMemoryCache(key, bitmap);
            }
        }
        return bitmap;
    }

    private boolean collectUndoTasks(String url, Runnable task) {
        String key = Utils.hashKeyFormUrl(url);

        if (lruCacheHelper.getBitmapFromMemCache(key) != null) {
            return false;
        }

        DiskLruCache.Snapshot snapShot = null;
        try {
            snapShot = mDiskLruCache.get(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (snapShot != null) {
            return false;
        }

        // 如果当前url下载操作过程的磁盘缓存的Editor未结束，又来了一个新的url，则不能正常生成新Editor
        // 则将新url对应的任务先保存起来
        if (doingTasks.containsKey(key)) {
            if (undoTasks.containsKey(key)) {
                List<Runnable> tasks = undoTasks.get(key);
                tasks.add(task);
                undoTasks.put(key, tasks);
            } else {
                List<Runnable> tasks = new ArrayList<>();
                tasks.add(task);
                undoTasks.put(key, tasks);
            }
            return true;
        }

        doingTasks.put(key, task);
        return false;
    }

    private void executeUndoTasks(String url) {
        String key = Utils.hashKeyFormUrl(url);
        // 检查undoTasks中是否有要执行的任务
        if (undoTasks.containsKey(key)) {
            for (Runnable task : undoTasks.get(key)) {
                ThreadPool.getInstance().execute(task);
            }
            undoTasks.remove(key);
        }
        // 从doingTasks中移除已经执行完的任务
        doingTasks.remove(key);
    }
}
