package com.hisign.broadcastx.socket;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Queues;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.pj.StuffEvent;

import org.junit.Test;

import java.security.acl.Group;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.socket.emitter.Emitter;

import static com.hisign.broadcastx.pj.StuffEvent.CALL_EVENT;
import static com.hisign.broadcastx.pj.StuffEvent.TEXT_EVENT;
import static org.junit.Assert.*;

public class SocketIOClientUtilTest {

    @Test
    public void linkedBlockingQueue() {
        final BlockingQueue<String> ackQueue = Queues.newSynchronousQueue();
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000000);
                    System.out.println("put 1");
                    ackQueue.put("1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            System.out.println(ackQueue.poll(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void socketConnect() {
        final SocketIOClient socketIOClient = SocketIOClientUtil.socketConnect("http://192.168.1.13:3000");
        if (socketIOClient == null) {
            return;
        }
        socketIOClient.register(new SocketIOClient.Customer(CustomerType.GROUP, "110"), new ISocketEmitCallBack() {
            @Override
            public void call(String eventName, Object object, Map<String, String> responseMap) {
                System.out.println("responseMap = " + responseMap);
                if("1".equals(responseMap.get("flag"))){
                    socketIOClient.onListener(TEXT_EVENT.getEventName(), new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            for (Object object : args) {
                                Stuff stuff = null;
                                try {
                                    stuff = JSON.parseObject(object.toString(), Stuff.class);
                                    System.out.println(Thread.currentThread());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("stuff = " + stuff);
                            }
                        }
                    });
                    Stuff stuff = new Stuff(SocketIOClientUtil.getUser().getCustomerId(), "110", CustomerType.GROUP, "110", TEXT_EVENT, "111111");
                    socketIOClient.send(TEXT_EVENT.getEventName(), "110", JSON.toJSONString(stuff), new ISocketEmitCallBack() {
                        @Override
                        public void call(String eventName, Object object, Map<String, String> responseMap) {
                            System.out.println("responseMap = " + responseMap);
                        }
                    });
                }
            }
        });
        try {
            Thread.sleep(999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}