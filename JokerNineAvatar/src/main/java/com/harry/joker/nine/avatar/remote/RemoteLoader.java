package com.harry.joker.nine.avatar.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.harry.joker.nine.avatar.helper.JokerLog;
import com.harry.joker.nine.avatar.helper.ThreadPool;
import com.harry.joker.nine.avatar.helper.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteLoader {

    private Map<String, String[]> mMemoryCache;
    private SharedPreferences mSharedPreferences;

    private volatile static RemoteLoader manager;

    public static RemoteLoader getInstance(Context context) {
        if (manager == null) {
            synchronized (RemoteLoader.class) {
                if (manager == null) {
                    manager = new RemoteLoader(context);
                }
            }
        }
        return manager;
    }

    // 存储线程池中的任务
    private Map<String, Runnable> doingTasks;
    // 存储暂时不能进入线程池的任务
    private Map<String, List<Runnable>> undoTasks;

    private RemoteLoader(Context context) {
        mMemoryCache = new ConcurrentHashMap<>();
        mSharedPreferences = context.getSharedPreferences("JokerNineAvatar", Context.MODE_PRIVATE);

        doingTasks = new HashMap<>();
        undoTasks = new HashMap<>();
    }

    public void asyncLoadRemote(final String url, final String method, final Map<String, String> params, final Map<String, String> headers, final Options.OnParseCallback parser, final Options.OnRemoteCallback callback) {
        final Handler mHandler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                final String[] urls = loadRemote(makeQueryUrl(url, params), method, headers, parser);
                JokerLog.d(this.getClass().getSimpleName() + "Task for Remote:" + Thread.currentThread() + ", result:" + urls + "\n");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onRemoteReponse(urls);
                    }
                });
            }
        };

        if (collectUndoTasks(url, task)) {
            JokerLog.d(this.getClass().getSimpleName() + "Task for Remote, add to undoTasks:" + "\n");
            return;
        }

        ThreadPool.getInstance().execute(task);
    }

    private String makeQueryUrl(String url, Map<String, String> params) {
        if (params == null || params.size() == 0) return url;
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(param.getKey() + "=" + param.getValue());
        }
        return url + "?" + builder.toString();
    }


    /**
     * 加载数据
     * @param url
     * @param parser
     * @return
     */
    private String[] loadRemote(final String url, String method, Map<String, String> headers, final Options.OnParseCallback parser) {
        try {
            String[] urls = loadUrlsFromMemory(url);
            if (urls != null) {
                JokerLog.d(this.getClass().getSimpleName() + "Remote load from memory \n" + " Request: " + url + "\n Response: " + Arrays.deepToString(urls) + "\n");
                return urls;
            }
            urls = loadUrlsFromDisk(url);
            if (urls != null) {
                JokerLog.d(this.getClass().getSimpleName() + " Remote load from Disk \n" + " Request: " + url + "\n Response: " + Arrays.deepToString(urls) + "\n");
                return urls;
            }
            urls = loadUrlsFromRemote(url, method, headers, parser);
            if (urls != null) {
                JokerLog.d(this.getClass().getSimpleName() + " Remote load from Http \n" + " Request: " + url + "\n Response: " + Arrays.deepToString(urls) + "\n");
                return urls;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 内存缓存
     * @param url
     * @return
     */
    private String[] loadUrlsFromMemory(String url) {
        String key = Utils.hashKeyFormUrl(url);
        if (mMemoryCache.containsKey(key)) {
            return mMemoryCache.get(key);
        }
        return null;
    }

    /**
     * Disk缓存
     * @param url
     * @return
     */
    private String[] loadUrlsFromDisk(String url) {
        String key = Utils.hashKeyFormUrl(url);
        String content = mSharedPreferences.getString(key, "");
        if (!TextUtils.isEmpty(content)) {
            List<String> array = JSONObject.parseArray(content, String.class);
            if (array != null) {
                String[] urls = array.toArray(new String[]{});
                mMemoryCache.put(key, urls);
                return urls;
            }
        }
        return null;
    }

    /**
     * 远程Http读取
     * @param url
     * @param parseCallback
     * @return
     * @throws IOException
     */
    private String[] loadUrlsFromRemote(String url, String method, Map<String, String> headers, Options.OnParseCallback parseCallback) {
        String[] urls = null;
        if (downloadUrlToStream(url, method, headers, parseCallback)) {
            urls = loadUrlsFromMemory(url);
        }
        executeUndoTasks(url);
        return urls;
    }

    private boolean downloadUrlToStream(String urlString, String method, Map<String, String> headers, Options.OnParseCallback parseCallback) {
        HttpURLConnection urlConnection = null;
        InputStream in = null;

        try {
            if (TextUtils.isEmpty(urlString)) {

                return false;
            }
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            //设置请求类型
            urlConnection.setRequestMethod(method);
            //设置header
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    urlConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            urlConnection.connect();

            int code = urlConnection.getResponseCode();

            if (code != 200) return false;

            in = urlConnection.getInputStream();

            String string = Utils.is2String(in);

            if (TextUtils.isEmpty(string)) {
                return false;
            }

            if (parseCallback == null) {
               return false;
            }

            String[] urls = parseCallback.parseResponse(string);


            if (urls == null || urls.length == 0) {
                return false;
            }

            remote2Cache(urlString, urls);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JokerLog.d(this.getClass().getSimpleName() + " Request Remote failed." + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Utils.close(in);
        }
        return false;
    }

    /**
     * 缓存到Disk和Memory
     * @param url
     * @param urls
     */
    private void remote2Cache(String url, String[] urls) {
        String key = Utils.hashKeyFormUrl(url);

        mMemoryCache.put(key, urls);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, JSONObject.toJSONString(urls)) ;
        editor.apply();
    }

    private boolean collectUndoTasks(String url, Runnable task) {
        String key = Utils.hashKeyFormUrl(url);

//        if (lruCacheHelper.getBitmapFromMemCache(key) != null) {
//            return false;
//        }
        //存在缓存
        if (mMemoryCache.get(key) != null) {
            return false;
        }

//        DiskLruCache.Snapshot snapShot = null;
//        try {
//            snapShot = mDiskLruCache.get(key);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (snapShot != null) {
//            return false;
//        }

        String content = mSharedPreferences.getString(key, "");
//        if (!TextUtils.isEmpty(content)) {
        if (content != null) {
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
