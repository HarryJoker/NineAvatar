package com.harry.joker.nine.avatar.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.harry.joker.nine.avatar.cache.DiskLruCacheHelper;
import com.harry.joker.nine.avatar.cache.LruCacheHelper;
import com.harry.joker.nine.avatar.cache.disklrucache.DiskLruCache;
import com.harry.joker.nine.avatar.listener.OnMuilteCompeleteListener;
import com.harry.joker.nine.avatar.listener.OnNineAvatarCallback;
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

    private Resources mResources;

    private Handler mHandler = new Handler();

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
        mResources = context.getResources();
        mDiskLruCache = new DiskLruCacheHelper(context).mDiskLruCache;
        lruCacheHelper = new LruCacheHelper();
        compressHelper = CompressHelper.getInstance();

        doingTasks = new HashMap<>();
        undoTasks = new HashMap<>();
    }


    public void aysncLoadBuilder(final Builder builder, final Builder.OnNineAvatarCallback avatarCallback) {

        //从缓存取九宫格头像
        aysncLoadNineAvatarFromCache(builder, avatarCallback);

        //九宫格头像没有缓存
        if (!isExsitNineAvatar(builder.urls)) {
            //生成九宫格占位图
//            asyncLoadNinePlaceholder(builder, avatarCallback);

            //网络取头像进行合成
            asyncNineAvatarFromRemote(builder, avatarCallback);
        }
    }

    /**
     * 生成placeholder的九宫格占位图
     * @param builder
     */
    private void asyncLoadNinePlaceholder(final Builder builder, final Builder.OnNineAvatarCallback avatarCallback) {

        Runnable task = new Runnable() {
            @Override
            public void run() {

                //生成PlaceHolder占位符
                Bitmap placeholder = loadBitmapFromRes(builder.placeholder, builder.itemWidth, builder.itemWidth);
                final Bitmap placeholderAvatar = builder.layoutManager.makePlaceholderAvatar(builder.imageWidth, builder.itemWidth, builder.dividerWidth, builder.dividerColor, builder.count, placeholder);
                if (placeholderAvatar == null) return;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        avatarCallback.onCompeleteAvatar("NineAvatar draw placehold image", placeholderAvatar);
                    }
                });
            }
        };

        ThreadPool.getInstance().execute(task);
    }

    /**
     * 取本地缓存的合成九宫格头像
     * @param builder
     */
    private void aysncLoadNineAvatarFromCache(final Builder builder, final Builder.OnNineAvatarCallback avatarCallback) {
        Runnable task = new Runnable() {
            @Override
            public void run() {

                JokerLog.d(this.getClass().getSimpleName() + "Task for NineAvatar:" + Thread.currentThread());

                //取缓存
                final Bitmap nineAvatar = loadNineAvatarFromCache(builder.urls, builder.imageWidth, builder.imageWidth);
                if (nineAvatar == null) return;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        avatarCallback.onCompeleteAvatar("NineAvatar read Cache image", nineAvatar);
                    }
                });
            }
        };

        ThreadPool.getInstance().execute(task);
    }

    /**
     * 从缓存中查找合成的图片
     * @param urls
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadNineAvatarFromCache(String[] urls, int reqWidth, int reqHeight) {
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

    /**
     * 从Remote Http取头像
     * @param builder
     */
    private void asyncNineAvatarFromRemote(final Builder builder, final Builder.OnNineAvatarCallback avatarCallback) {
        MultiImage multiImage = new MultiImage(builder.count, new OnMuilteCompeleteListener(){

            @Override
            public void onMuilteCompelete(Bitmap[] bitmaps) {
                //九宫格数据加载完毕，合成头像
                final Bitmap nineAvatar = builder.layoutManager.makeNineAvatar(builder.imageWidth, builder.itemWidth, builder.dividerWidth, builder.dividerColor, bitmaps);
                //缓存NineAvatar
                if (nineAvatar != null) {
                    saveNineAvatar2Cache(builder.urls, nineAvatar);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        avatarCallback.onCompeleteAvatar("NineAvatar drawn image", nineAvatar);
                    }
                });
            }
        });


        //取每个格子的图片
        for (int index = 0; index < builder.urls.length; index++) {
            asyncLoadSingleImageFromRemote(index, builder.urls[index], builder.placeholder, builder.itemWidth, builder.itemWidth, multiImage, avatarCallback);
        }
    }

    /**
     * 取九宫格中的单张图片
     * @param index
     * @param url
     * @param place
     * @param reqWidth
     * @param reqHeight
     * @param multiImage
     * @param avatarCallback
     */
    private void asyncLoadSingleImageFromRemote(final int index, final String url, final int place, final int reqWidth, final int reqHeight, final MultiImage multiImage, final Builder.OnNineAvatarCallback avatarCallback) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                JokerLog.d(this.getClass().getSimpleName() + "Task for SingleImage:" + Thread.currentThread());

                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);
                if (bitmap == null) bitmap = loadBitmapFromRes(place, reqWidth, reqHeight);
                multiImage.putTaskCompeleteBitmap(index, bitmap);
            }
        };

        if (collectUndoTasks(url, task)) {
            JokerLog.d(this.getClass().getSimpleName() + "Task for avatar, add to undoTasks:" + "\n");

            return;
        }

        ThreadPool.getInstance().execute(task);
    }

    /**
     * 缓存合成的九宫格头像
     *
     * @param urls
     * @param nineAvatar
     */
    private void saveNineAvatar2Cache(String[] urls, Bitmap nineAvatar) {
        if (urls == null || nineAvatar == null) return;
        String url = buildSynthesizedUrl(urls);
        String key = Utils.hashKeyFormUrl(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                nineAvatar.compress(Bitmap.CompressFormat.PNG, 100, baos);
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

    /**
     * 将Res资源id序列化为唯一索引
     * @param res
     * @return
     */
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

            bitmap = CompressHelper.getInstance().compressResource(mResources, res, reqWidth, reqHeight);
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

    /**
     * 取单张图片
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
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

    /**
     * 九宫格图片是否存在缓存
     * @param urls
     * @return
     */
    private boolean isExsitNineAvatar(String[] urls) {
        try {
            String url = buildSynthesizedUrl(urls);
            String key = Utils.hashKeyFormUrl(url);
            if (lruCacheHelper.getBitmapFromMemCache(key) != null) {
                return true;
            }

            if (mDiskLruCache.get(key) != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
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
