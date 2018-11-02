package com.hisign.androidrtcx.groupchat.http;

import android.content.Context;

import com.hisign.androidrtcx.BuildConfig;
import com.hisign.androidrtcx.R;

public class IMServiceManager {

    protected static IMService businessService;

    public static void init(Context context) {
        businessService = RetrofitManager.getRetrofitByAddress(context.getString(R.string.chat_socketio_url_default)).create(IMService.class);
    }

    public static IMService getInstance() {
        return businessService;
    }
}
