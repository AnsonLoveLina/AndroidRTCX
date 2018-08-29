package com.hisign.androidrtcx.base;

import android.app.Application;

import com.hisign.rtcx.client.RtcClient;

public class RTCXDebugApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RtcClient.init(getApplicationContext());
    }
}
