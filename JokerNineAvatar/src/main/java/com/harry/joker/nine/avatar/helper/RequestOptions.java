//package com.harry.joker.nine.avatar.helper;
//
//import android.os.Handler;
//
//import com.harry.joker.nine.avatar.cache.RemoteCacheHelper;
//import com.lzy.okgo.callback.StringCallback;
//import com.lzy.okgo.model.Response;
//import com.lzy.okgo.request.base.Request;
//
///**
// * Author: harryjoker
// * Created on: 2020-01-15 13:14
// * Description:
// */
//public abstract class RequestOptions<T> {
//
//    private OnUrlsResponse mOnUrlsResponse;
//
//    public abstract Request getRequest();
//
//    public abstract OnResponseParseCallback getPraseCallback();
//
//    public void apply(final Object tag) {
//        //先取缓存
//        final String[] urls = loadUrlsFromCache(tag);
//        if (urls != null) {
//            JokerLog.d("RequestOptions, " + "Async NineAvatarUrls from Cache,  tag:" + tag + ", cache:" + urls);
//            if (mOnUrlsResponse != null) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mOnUrlsResponse.onUrlsResponse(tag, urls);
//                    }
//                }, 150);
//            }
//        } else {
//            //取远程
//            JokerLog.d("RequestOptions, " + "Async NineAvatarUrls from Remote, tag:" + tag);
//            asyncLoadRemoteUrls(tag);
//        }
//    }
//
//    private void asyncLoadRemoteUrls(final Object key) {
//        final Request request = getRequest();
//        if (request == null) return ;
//        final OnResponseParseCallback callback = getPraseCallback();
//        if (callback == null) return ;
//        request.execute(new StringCallback() {
//            @Override
//            public void onSuccess(Response<String> response) {
//                if (response != null && response.code() == 200) {
//                    String[] urls = callback.onParseReponse(response.body());
//                    if (mOnUrlsResponse != null && urls != null) {
//                        RemoteCacheHelper.getInstance().putCache(key, urls);
//                        mOnUrlsResponse.onUrlsResponse(key, urls);
//                    }
//                }
//            }
//        });
//    }
//
//    private String[] loadUrlsFromCache(Object key) {
//        return RemoteCacheHelper.getInstance().loadFromCache(key);
//    }
//
//    public void setOnUrlsResponse(OnUrlsResponse onUrlsResponse) {
//        mOnUrlsResponse = onUrlsResponse;
//    }
//
//    public interface OnResponseParseCallback {
//        String[] onParseReponse(String repsone);
//    }
//
//    public interface OnUrlsResponse {
//        void onUrlsResponse(Object tag, String[] urls);
//    }
//}
