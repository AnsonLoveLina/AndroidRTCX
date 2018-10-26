package com.hisign.androidrtcx.groupchat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
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

    public static void startService(Context context){
        EventBus.getDefault().register(context);
        Intent intent = new Intent(context,SocketService.class);
        context.startService(intent);
    }

    public static void stopService(Context context){
        EventBus.getDefault().unregister(context);
        Intent intent = new Intent(context,SocketService.class);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                socketIOClient.connection();
                socketIOClient.onConnection(new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        socketIOClient.register(Sets.newHashSet(groupCustomer, userCustomer));
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
                        });
                        socketIOClient.onListener(EVENT_STUFF, new Emitter.Listener() {
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
                    }
                });
            }
        }).start();

    }

    private void unRegister() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socketIOClient.unRegister(groupCustomer);
            }
        }).start();
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
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
