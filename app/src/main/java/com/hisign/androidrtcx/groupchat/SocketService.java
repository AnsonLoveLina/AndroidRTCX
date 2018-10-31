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
import com.hisign.rtcx.internet.SocketIOClient;
import com.hisign.androidrtcx.groupchat.pj.Stuff;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;

import io.socket.emitter.Emitter;

import static com.hisign.androidrtcx.groupchat.GroupChatActivity.EVENT_CALL_USER;
import static com.hisign.androidrtcx.groupchat.GroupChatActivity.EVENT_STUFF;
import static com.hisign.androidrtcx.groupchat.GroupChatActivity.EVENT_STUFF_HISTORY;

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

    private Stuff parseObject(Object object) {
        Stuff stuff = null;
        if (object != null && object.getClass() == Stuff.class) {
            stuff = (Stuff) object;
        } else if (object != null && object.getClass() == String.class) {
            try {
                stuff = JSON.parseObject(object.toString(), Stuff.class);
            } catch (Exception e) {
                Log.e(TAG, object.toString() + "\ncan not parse to stuff!", e);
            }
        } else {
            Log.e(TAG, "can not parse to stuff!");
        }
        return stuff;
    }

    private void register() {
        String userId = Integer.toString((new Random()).nextInt(100000000));
        //ExecutorService executor = Executors.newSingleThreadExecutor();
        socketIOClient = SocketIOClientUtil.getInstance(getString(R.string.chat_socketio_url_default));
        groupCustomer = new SocketIOClient.Customer("group", "110");
        userCustomer = new SocketIOClient.Customer("user", userId);
        SocketIOClientUtil.addGroup(groupCustomer);
        SocketIOClientUtil.setUser(userCustomer);
        Log.i(TAG, "setUserEnd");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        socketIOClient.connection();
        socketIOClient.onConnection(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socketIOClient.register(Sets.newHashSet(groupCustomer, userCustomer));
            }
        });
        //历史消息处理完毕之后才是实时消息
        socketIOClient.onListener(EVENT_STUFF_HISTORY, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                for (Object object : args) {
                    Stuff stuff = parseObject(object);
                    if (stuff == null) {
                        return;
                    }
                    EventBus.getDefault().post(stuff);
                }
            }
        }).on(EVENT_STUFF, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                for (Object object : args) {
                    Stuff stuff = parseObject(object);
                    if (stuff == null) {
                        return;
                    }
                    stuff.setType(Stuff.StuffType.TYPE_RECEIVE);
                    EventBus.getDefault().post(stuff);
                }
            }
        });
        socketIOClient.onListener(EVENT_CALL_USER, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                for (Object object : args) {
                    Stuff stuff = parseObject(object);
                    if (stuff == null) {
                        return;
                    }
                    EventBus.getDefault().post(stuff);
                }
            }
        });
        socketIOClient.onListener(SocketIOClient.EVENT_INFO, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, args[0].toString());
            }
        });
//            }
//        }).start();

    }

    private void unRegister() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        socketIOClient.unRegister(groupCustomer);
//            }
//        }).start();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        register();
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
        unRegister();
        Log.d(TAG, "socketservice destory!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface.Stub() {
        };
    }
}
