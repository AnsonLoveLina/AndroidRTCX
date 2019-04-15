package com.hisign.broadcastx.socket;

import com.alibaba.fastjson.JSON;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.socket.ack.AckESTimeOut;
import com.hisign.broadcastx.socket.ack.AckTimerTimeOut;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

import static com.hisign.broadcastx.pj.StuffEvent.TEXT_EVENT;
import static com.hisign.broadcastx.util.Constant.SUCCESS_FLAG;

public class SocketIOClientUtilTest {
    @Test
    public void test() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future future;
        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(8000);
                    System.out.println("future");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Future future1;
        future1 = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    System.out.println("future1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        future1.cancel(true);
        try {
            future.get(10000,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void newESTimeOut(){
        Ack ackESTimeOut = new AckESTimeOut() {
            @Override
            public void responseCall(Map<String, String> responseMap) {

            }
        };
    }

    private void newTimerTimeOut(){
        Ack ackTimerTimeOut = new AckTimerTimeOut() {
            @Override
            public void responseCall(Map<String, String> responseMap) {

            }
        };
    }

    @Test
    public void testAckTimeOut() {
        for (int i=0;i<9999;i++){
//            newTimerTimeOut();
            newESTimeOut();
        }
        Runtime run = Runtime.getRuntime();
        long startMem = run.totalMemory()-run.freeMemory();
        System.out.println("memory> total:" + run.totalMemory() + " free:" + run.freeMemory() + " used:" + startMem );
        try {
            Thread.sleep(999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListenerNoBlock() {
        new ListenerNoBlock<Stuff>() {
            @Override
            public void onEventCall(Stuff object) {

            }
        };
    }

    @Test
    public void socketConnect() {
        final SocketIOClient socketIOClient = SocketIOClientUtil.socketConnect("http://192.168.1.13:3000");
        if (socketIOClient == null) {
            return;
        }
        socketIOClient.register(new SocketIOClient.Customer(CustomerType.GROUP, "110"), new ISocketEmitCallBack() {
            @Override
            public void call(Map<String, String> responseMap) {
                System.out.println("responseMap = " + responseMap);
                if(SUCCESS_FLAG.equals(responseMap.get("flag"))){
                    socketIOClient.onListener(TEXT_EVENT.getEventName(), new ListenerNoBlock<Stuff>() {
                        @Override
                        public void onEventCall(Stuff object) {
                            System.out.println("stuff = " + object);
                        }
                    });
                    Stuff stuff = new Stuff(SocketIOClientUtil.getUser().getCustomerId(), "110", CustomerType.GROUP, "110", TEXT_EVENT, "1");
                    socketIOClient.send(TEXT_EVENT.getEventName(), "110", JSON.toJSONString(stuff), new ISocketEmitCallBack() {
                        @Override
                        public void call(Map<String, String> responseMap) {
                            System.out.println("send responseMap = " + responseMap);
                        }
                    });
                    Stuff stuff1 = new Stuff(SocketIOClientUtil.getUser().getCustomerId(), "111", CustomerType.GROUP, "111", TEXT_EVENT, "111111");
                    socketIOClient.send(TEXT_EVENT.getEventName(), "111", JSON.toJSONString(stuff1), new ISocketEmitCallBack() {
                        @Override
                        public void call(Map<String, String> responseMap) {
                            System.out.println("send responseMap = " + responseMap);
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