package com.hisign.broadcastx.socket;

import com.hisign.broadcastx.util.Constant;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AckESTimeOut extends AckTimeOut {

    private static final Logger logger = Logger.getLogger(AckESTimeOut.class.getName());

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private Future future;
    protected void start() {
        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(getTimeOut());
                    logger.log(Level.SEVERE,"ACK timeout");
                    callback(Constant.timeoutFailResponseMap);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE,"ACK interrupted\n"+e.getMessage());
                } catch (Exception e) {
                    logger.log(Level.SEVERE,"ACK error\n"+e.getMessage());
                    future.cancel(true);
                }
            }
        });
    }

    protected void shutdown() {
        if (future != null){
            future.cancel(true);
        }
    }
}
