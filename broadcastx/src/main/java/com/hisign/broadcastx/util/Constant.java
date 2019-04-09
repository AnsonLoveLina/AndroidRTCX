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
}
