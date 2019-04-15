package com.hisign.broadcastx.socket.ack;

import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.util.Constant;
import com.hisign.broadcastx.util.FastJsonUtil;
import com.hisign.broadcastx.util.SocketUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.socket.client.Ack;

import static com.hisign.broadcastx.util.Constant.TIMEOUT_FLAG;

public abstract class AckTimeOut implements Ack {

    private static final Logger logger = Logger.getLogger(AckTimeOut.class.getName());

    private long timeOut = 0;
    private boolean called = false;

    public AckTimeOut() {
        this(Constant.EMIT_TIMEOUT);
    }

    public AckTimeOut(long timeOut) {
        if (timeOut <= 0)
            return;
        setTimeOut(timeOut);
        start();
    }

    protected abstract void start();

    protected abstract void shutdown();

    public void reset() {
        shutdown();
        start();
    }

    protected void callback(Object... args) {
        if (called) return;
        called = true;
        shutdown();
        call(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void call(Object... args) {
        Map<String, String> responseMap = new HashMap<>();
        String response = args[0] == null ? "" : args[0].toString();
        try {
            if (args[0] != null && Map.class.isAssignableFrom(args[0].getClass())) {
                responseMap = (Map<String, String>) args[0];
            } else {
                responseMap = FastJsonUtil.parseObject(response, Map.class);
            }
        } catch (Exception e) {
            responseMap = SocketUtil.getBaseFailResponseMap(String.format("%s can not parse to %s", response, Map.class.toString()));
            logger.log(Level.SEVERE, String.format("%s can not parse to %s", response, Stuff.class.toString()));
        } finally {
            if (!TIMEOUT_FLAG.equals(responseMap.get("flag"))) {
                shutdown();
            }
        }
        responseCall(responseMap);
    }

    public abstract void responseCall(Map<String, String> responseMap);

    protected long getTimeOut() {
        return timeOut;
    }

    protected void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}