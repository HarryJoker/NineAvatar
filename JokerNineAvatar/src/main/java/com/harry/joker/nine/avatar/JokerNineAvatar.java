package com.harry.joker.nine.avatar;

import android.content.Context;

import com.harry.joker.nine.avatar.helper.Builder;
import com.harry.joker.nine.avatar.helper.JokerLog;

public class JokerNineAvatar {

    public static Builder init(Context context) {
        return new Builder(context);
    }

    public static void setDebug(boolean debug) {
        JokerLog.setDebug(debug);
    }
}
