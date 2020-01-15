package com.harry.joker.nine.avatar.cache;

import com.harry.joker.nine.avatar.helper.JokerLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: harryjoker
 * Created on: 2020-01-15 15:43
 * Description:
 */
public class RemoteCacheHelper {
    private Map<Object, String[]> remoteUrls = new ConcurrentHashMap<>();

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
            JokerLog.d(this.getClass().getSimpleName() + "， RemoteAvatarUrls to Cache , key:" + obj + ",  value:" + urls.length);
            remoteUrls.put(obj, urls);
        }
    }

    public String[] getUrlsFromCache(Object key) {
        if (remoteUrls.containsKey(key)) {
            JokerLog.d(this.getClass().getSimpleName() + " load RemoteAvatarUrls from cache, key:" + key + ", value:" + remoteUrls.get(key).length);
            remoteUrls.get(key);
        }
        return null;
    }
}
