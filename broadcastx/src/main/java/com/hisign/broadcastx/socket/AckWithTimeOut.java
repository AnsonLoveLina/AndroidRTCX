package com.hisign.broadcastx.socket;

import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.util.Constant;
import com.hisign.broadcastx.util.FastJsonUtil;
import com.hisign.broadcastx.util.SocketUtil;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import io.socket.client.Ack;

import static com.hisign.broadcastx.util.Constant.TIMEOUT_FLAG;

public abstract class AckWithTimeOut implements Ack {

    private static final Logger logger = Logger.getLogger(AckWithTimeOut.class.getName());

    private Timer timer;
    private long timeOut = 0;
    private boolean called = false;

    public AckWithTimeOut() {
        this(Constant.EMIT_TIMEOUT);
    }

    public AckWithTimeOut(long timeout_after) {
        if (timeout_after <= 0)
            return;
        this.timeOut = timeout_after;
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callback(Constant.timeoutFailResponseMap);
            }
        }, timeOut);
    }

    public void resetTimer() {
        if (timer != null) {
            timer.cancel();
            startTimer();
        }
    }

    private void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

    private void callback(Object... args) {
        if (called) return;
        called = true;
        cancelTimer();
        call(args);
    }

    @Override
    public void call(Object... args) {
        String response = args[0] == null ? "" : args[0].toString();
        Map<String, String> responseMap = null;
        try {
            responseMap = FastJsonUtil.parseObject(response, Map.class);
        } catch (Exception e) {
            responseMap = SocketUtil.getBaseFailResponseMap(String.format("%s can not parse to %s", response, Stuff.class.toString()));
            logger.fine(String.format("%s can not parse to %s", response, Stuff.class.toString()));
        } finally {
            if (!TIMEOUT_FLAG.equals(responseMap.get("flag"))) {
                cancelTimer();
            }
        }
        responseCall(responseMap);
    }

    public abstract void responseCall(Map<String, String> responseMap);

}