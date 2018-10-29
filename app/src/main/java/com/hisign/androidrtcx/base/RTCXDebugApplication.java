package com.hisign.androidrtcx.base;

import android.app.Application;

import com.hisign.androidrtcx.groupchat.GroupChatActivity;
import com.hisign.androidrtcx.groupchat.SocketService;
import com.hisign.rtcx.client.RtcClient;

public class RTCXDebugApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SocketService.startService(getApplicationContext());
        RtcClient.init(getApplicationContext());
    }
}
