package com.hisign.broadcastx.socket;

import java.util.Map;

public interface ISocketEmitCallBack {
    void call(String eventName, Object object,Map<String, String> responseMap);
}
