package com.hisign.broadcastx.socket;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Queues;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.pj.StuffEvent;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.socket.emitter.Emitter;

import static org.junit.Assert.*;

public class SocketIOClientUtilTest {

    @Test
    public void LinkedBlockingQueue() {
        final BlockingQueue<String> ackQueue = Queues.newSynchronousQueue();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
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
        SocketIOClient socketIOClient = SocketIOClientUtil.socketConnect("http://192.168.1.13:3000", null, new String[]{"110"});
        System.out.println(Thread.currentThread());
        socketIOClient.onListener(StuffEvent.TEXT_EVENT.getEventName(), new Emitter.Listener() {
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
        try {
            Thread.sleep(999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}