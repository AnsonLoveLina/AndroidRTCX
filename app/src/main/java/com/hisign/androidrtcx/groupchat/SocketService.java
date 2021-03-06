package com.hisign.androidrtcx.groupchat;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.hisign.androidrtcx.IMyAidlInterface;
import com.hisign.androidrtcx.R;
import com.hisign.androidrtcx.common.EventBusManager;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.socket.ISocketEmitCallBack;
import com.hisign.broadcastx.socket.ListenerNoBlock;
import com.hisign.broadcastx.socket.SocketIOClient;
import com.hisign.broadcastx.socket.SocketIOClientUtil;
import com.hisign.broadcastx.util.Constant;
import com.hisign.broadcastx.util.FastJsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

import io.socket.emitter.Emitter;

import static com.hisign.broadcastx.pj.StuffEvent.CALL_EVENT;
import static com.hisign.broadcastx.pj.StuffEvent.HANGUP_EVENT;
import static com.hisign.broadcastx.pj.StuffEvent.TEXT_EVENT;
import static com.hisign.broadcastx.socket.SocketIOClient.EVENT_REGISTER;
import static com.hisign.broadcastx.util.Constant.SUCCESS_FLAG;

public class SocketService extends Service {
    private static final String TAG = "SocketService";

    private SocketIOClient socketIOClient;

    private SocketIOClient.Customer groupCustomer;

    private SocketIOClient.Customer userCustomer;

    public SocketService() {
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.stopService(intent);
    }

    private void socketConnect() {
        String userId = Integer.toString((new Random()).nextInt(100000000));
        //ExecutorService executor = Executors.newSingleThreadExecutor();
        socketIOClient = SocketIOClientUtil.socketConnect(getString(R.string.chat_socketio_url_default));
        if (socketIOClient == null) {
            return;
        }
        socketIOClient.register(new SocketIOClient.Customer(CustomerType.GROUP, "110"), new ISocketEmitCallBack() {
            @Override
            public void call(Map<String, String> responseMap) {
                if (!SUCCESS_FLAG.equals(responseMap.get("flag"))) {
                    Log.e(TAG, String.format("%s error!\n%s", EVENT_REGISTER, responseMap.toString()));
                }
            }
        });
        socketIOClient.register(new SocketIOClient.Customer(CustomerType.USER, userId), new ISocketEmitCallBack() {
            @Override
            public void call(Map<String, String> responseMap) {
                if (!SUCCESS_FLAG.equals(responseMap.get("flag"))) {
                    Log.e(TAG, String.format("%s error!\n%s", EVENT_REGISTER, responseMap.toString()));
                }
            }
        });
        final EventBus defaultEB = EventBusManager.getEventBus();
        socketIOClient.onListener(TEXT_EVENT.getEventName(), new ListenerNoBlock<Stuff>() {
            @Override
            public void onEventCall(Stuff stuff) {
                defaultEB.post(stuff);
            }
        });
        socketIOClient.onListener(CALL_EVENT.getEventName(), new ListenerNoBlock<Stuff>() {
            @Override
            public void onEventCall(Stuff stuff) {
                defaultEB.post(stuff);
            }
        });
        final EventBus hangupEB = EventBusManager.getEventBus(HANGUP_EVENT.getEventName());
        socketIOClient.onListener(HANGUP_EVENT.getEventName(), new ListenerNoBlock<Stuff>() {
            @Override
            public void onEventCall(Stuff stuff) {
                hangupEB.post(stuff);
            }
        });
        socketIOClient.onListener(SocketIOClient.EVENT_INFO, new ListenerNoBlock<String>() {
            @Override
            public void onEventCall(String string) {
                Log.i(TAG, string);
            }
        });
//            }
//        }).start();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        socketConnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        bindService(new Intent(this, PSocketService.class), pServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    private ServiceConnection pServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d(TAG, "SocketService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "SocketService:链接失效");
            //断开链接
            startService(new Intent(SocketService.this, PSocketService.class));
            //重新绑定
            bindService(new Intent(SocketService.this, PSocketService.class),
                    pServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "socketservice destory!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface.Stub() {
        };
    }
}
