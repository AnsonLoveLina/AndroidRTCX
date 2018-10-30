package com.hisign.androidrtcx.base;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hisign.androidrtcx.groupchat.GroupChatActivity;
import com.hisign.androidrtcx.groupchat.SocketService;
import com.hisign.rtcx.Constant;
import com.hisign.rtcx.client.RtcClient;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class RTCXDebugApplication extends Application {

    private int PERMISSION_REQUEST_CODE = 13;

    @Override
    public void onCreate() {
        super.onCreate();

        SocketService.startService(getApplicationContext());
        RtcClient.init(getApplicationContext());
    }


}
