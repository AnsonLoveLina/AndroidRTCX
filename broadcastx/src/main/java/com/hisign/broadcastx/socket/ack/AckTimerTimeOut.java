package com.hisign.broadcastx.socket.ack;

import com.hisign.broadcastx.util.Constant;

import java.util.Timer;
import java.util.TimerTask;

@Deprecated
/**
 *too much memory,see com.hisign.broadcastx.socket.SocketIOClientUtilTest.testAckTimeOut()
 */
public abstract class AckTimerTimeOut extends AckTimeOut {

    private Timer timer;
    protected void start() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callback(Constant.timeoutFailResponseMap);
            }
        }, getTimeOut());
    }

    protected void shutdown() {
        if (timer != null){
            timer.cancel();
        }
    }
}
