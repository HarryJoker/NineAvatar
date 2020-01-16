package com.harry.joker.nine.avatar.cache;

import android.content.SharedPreferences;

import com.harry.joker.nine.avatar.helper.JokerLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: harryjoker
 * Created on: 2020-01-15 15:43
 * Description:
 */
public class RemoteCacheHelper {
    private Map<Object, String[]> mRemoteUrls = new ConcurrentHashMap<>();
    private SharedPreferences mSharedPreferences;

    private static RemoteCacheHelper instance;

    private RemoteCacheHelper() {

    }

    public static RemoteCacheHelper getInstance() {
        if (instance == null) {
            synchronized (RemoteCacheHelper.class) {
                if (instance == null) {
                    instance = new RemoteCacheHelper();
                }
            }
        }
        return instance;
    }


    public void putCache(Object obj, String[] urls) {
        if (obj != null && urls != null) {
            JokerLog.d(this.getClass().getSimpleName() + "， RemoteAvatarUrls to Cache , key:" + obj + ",  urls:" + urls.length);
            mRemoteUrls.put(obj, urls);
        }
    }

    public String[] loadFromCache(Object key) {
        if (mRemoteUrls.containsKey(key)) {
            JokerLog.d(this.getClass().getSimpleName() + " load RemoteAvatarUrls from cache, key:" + key + ", value:" + mRemoteUrls.get(key).length);
            return mRemoteUrls.get(key);
        }
        return null;
    }
}
