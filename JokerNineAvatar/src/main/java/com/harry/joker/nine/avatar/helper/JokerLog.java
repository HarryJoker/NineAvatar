package com.harry.joker.nine.avatar.helper;

import android.util.Log;

/**
 * Author: harryjoker
 * Created on: 2020-01-15 17:56
 * Description:
 */
public class JokerLog {

    private static boolean isDebug = true;

    private static String TAG = "JokerNineAvatar";

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }
}
