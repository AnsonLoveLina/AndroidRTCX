package com.hisign.androidrtcx.common;


import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class EventBusManager {
    private static Map<String, EventBus> eventBusMap = new HashMap<>();

    public static EventBus getEventBus() {
        return getEventBus(null);
    }

    public static EventBus getEventBus(String eventName) {
        if (eventName == null) {
            return EventBus.getDefault();
        }
        if (eventBusMap.get(eventName) == null) {
            eventBusMap.put(eventName, EventBus.builder().build());
        }
        return eventBusMap.get(eventName);
    }
}
