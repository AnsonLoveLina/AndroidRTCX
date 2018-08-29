package com.hisign.rtcx.pj;

import com.hisign.rtcx.internet.SocketIOClient;

public class Stuff {
    public enum StuffType {
        TYPE_RECEIVE, TYPE_SEND, TYPE_CALL, TYPE_HANGUP
    }

    public Stuff() {
    }

    public Stuff(String content, StuffType type, SocketIOClient.Customer userCustomer) {
        this.content = content;
        this.type = type;
        this.userCustomer = userCustomer;
    }

    private SocketIOClient.Customer userCustomer;
    private String content;
    private StuffType type;

    public SocketIOClient.Customer getUserCustomer() {
        return userCustomer;
    }

    public void setUserCustomer(SocketIOClient.Customer userCustomer) {
        this.userCustomer = userCustomer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public StuffType getType() {
        return type;
    }

    public void setType(StuffType type) {
        this.type = type;
    }
}
