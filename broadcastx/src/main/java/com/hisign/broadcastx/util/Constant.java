package com.hisign.broadcastx.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constant {
    public static long EMIT_TIMEOUT = 30;
    public static TimeUnit EMIT_TIMEUNIT = TimeUnit.SECONDS;
    public static Map<String, String> defaultFailResponseMap = Maps.newHashMap();
    static {
        defaultFailResponseMap.put("flag","0");
        defaultFailResponseMap.put("messageLevel","err");
        defaultFailResponseMap.put("message","unknown error!");
    }
    public static Map<String, String> timeoutFailResponseMap = Maps.newHashMap();
    static {
        timeoutFailResponseMap.put("flag","0");
        timeoutFailResponseMap.put("messageLevel","err");
        timeoutFailResponseMap.put("message","emit timeout error!");
    }
    public static Map<String, String> baseFailResponseMap = Maps.newHashMap();
    static {
        baseFailResponseMap.put("flag","0");
        baseFailResponseMap.put("messageLevel","err");
    }
}
