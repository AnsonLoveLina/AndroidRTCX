package com.hisign.broadcastx.pj;

/**
 * 后台存到是fieldName而非stringValue
 */
public enum StuffEvent {
    TEXT_EVENT("textEvent"), CALL_EVENT("callEvent"), HANGUP_EVENT("hangupEvent");
    private String eventName;

    StuffEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
