package com.hisign.broadcastx.socket;

import java.util.Map;

public interface ISocketEmitFail {
    void onEmitFail(String eventName, Object object, Map<String, String> response);
}
