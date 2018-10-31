package com.hisign.androidrtcx.groupchat;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.hisign.androidrtcx.IMyAidlInterface;

public class PSocketService extends Service {
    private static final String TAG = "PSocketService";
    public PSocketService() {
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, PSocketService.class);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface.Stub() {
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        //绑定建立链接
        bindService(new Intent(this, SocketService.class), mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d(TAG, "PSocketService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(PSocketService.this, SocketService.class));
            bindService(new Intent(PSocketService.this, SocketService.class),
                    mServiceConnection, Context.BIND_IMPORTANT);
        }

    };
}
