package com.hisign.broadcastx.pj;

public enum StuffEvent {
    TYPE_TEXT("textEvent"), TYPE_CALL("call"), TYPE_HANGUP("hangup");
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
