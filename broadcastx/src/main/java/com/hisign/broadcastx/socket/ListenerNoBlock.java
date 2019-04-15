package com.hisign.broadcastx.socket;


import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.socket.ack.AckTimeOut;
import com.hisign.broadcastx.util.FastJsonUtil;
import com.hisign.broadcastx.util.SocketUtil;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.socket.emitter.Emitter;
import jodd.util.ReflectUtil;

import static com.hisign.broadcastx.util.Constant.TIMEOUT_FLAG;

public abstract class ListenerNoBlock<T> implements Emitter.Listener {

    private static final Logger logger = Logger.getLogger(ListenerNoBlock.class.getName());

    private Class<T> tClass;

    public ListenerNoBlock() {
        tClass = ReflectUtil.getGenericSupertype(getClass(),0);
    }

    @Override
    public void call(Object... args) {
        for (Object object : args) {
            T stuff = null;
            try {
                stuff = FastJsonUtil.parseObject(object, tClass);
            } catch (Exception e) {
                logger.log(Level.SEVERE, String.format("%s can not parse to %s", object == null ? "" : object.toString(), tClass.toString()));
            }
            if (stuff == null) {
                continue;
            }
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            final T finalStuff = stuff;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    onEventCall(finalStuff);
                }
            });
        }
    }

    abstract public void onEventCall(T object);
}
