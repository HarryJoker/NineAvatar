package com.harry.joker.nine.avatar.remote;

import android.content.Context;
import android.text.TextUtils;

import com.harry.joker.nine.avatar.cache.RemoteCacheHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: harryjoker
 * Created on: 2020-01-16 15:48
 * Description:
 */
public class Options {
    public static final String HTTP_METHOD_GET = "GET";

    public static final String HTTP_METHOD_POST = "POST";

    public Context context;

    public String mUrl;

    public boolean forceUpdate;

    public String mMethod = HTTP_METHOD_GET;

    public Map<String, String> mParams;

    public Map<String, String> mHeaders;

    public OnParseCallback mParser;

    private Options(Context context) {
        this.context = context;
    }

    public static Options with(Context context) {
        return new Options(context);
    }

    public Options load(String url) {
        this.mUrl = url;
        return this;
    }

    public Options method(String method) {
        if (method.equals(HTTP_METHOD_GET) || method.equals(HTTP_METHOD_POST)) {
            this.mMethod = method;
        } else {
            throw new IllegalArgumentException("Http Request can support Get and Post, but do not know your method: " + method);
        }
        return this;
    }

    public Options param(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            if (mParams == null) {
                mParams = new HashMap<>();
            }
            mParams.put(key, value);
        }
        return this;
    }

    public Options forceUpdate(boolean force) {
        this.forceUpdate = force;
        return this;
    }

    public Options params(Map<String, String> params) {
        if (params != null) {
            if (mParams == null) {
                mParams = new HashMap<>();
            }
            mParams.putAll(params);
        }
        return this;
    }

    public Options header(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            if (mHeaders == null) {
                mHeaders = new HashMap<>();
            }
            mHeaders.put(key, value);
        }
        return this;
    }

    public Options headers(Map<String, String> headers) {
        if (headers != null) {
            if (mHeaders == null) {
                mHeaders = new HashMap<>();
            }
            mHeaders.putAll(headers);
        }
        return this;
    }


    public Options apply(OnParseCallback parser) {
        this.mParser = parser;
        if (TextUtils.isEmpty(mUrl)) {
            throw new NullPointerException("Http Request url null ");
        }

        if (mParser == null) {
            throw new NullPointerException("apply methed must ensure the ParseCallbck .......");
        }
        return this;
    }

    public interface OnParseCallback {
        String[] parseResponse(String content);
    }

    public interface OnRemoteCallback {
        void onRemoteReponse(String[] urls);
    }
}
