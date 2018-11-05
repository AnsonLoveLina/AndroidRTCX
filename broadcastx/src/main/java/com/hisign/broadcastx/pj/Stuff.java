package com.hisign.broadcastx.pj;


import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.socket.SocketIOClient;

import java.util.Date;

import jodd.util.StringUtil;

public class Stuff {
    private String pkId;
    private String source;
    private Date sourceCreateTime;
    private String target;
    private CustomerType targetType;
    private Date targetCreateTime;
    private String roomName;
    private StuffEvent eventName;
    private String context;

    public boolean isOwn(String user) {
        if (StringUtil.isBlank(user) || StringUtil.isBlank(source)) {
            return false;
        }
        return user.equals(source);
    }

    public Stuff() {
    }

    public Stuff(String source, String target, CustomerType targetType, String roomName, StuffEvent eventName, String context) {
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.roomName = roomName;
        this.eventName = eventName;
        this.context = context;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public CustomerType getTargetType() {
        return targetType;
    }

    public void setTargetType(CustomerType targetType) {
        this.targetType = targetType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public StuffEvent getEventName() {
        return eventName;
    }

    public void setEventName(StuffEvent eventName) {
        this.eventName = eventName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPkId() {
        return pkId;
    }

    public Date getSourceCreateTime() {
        return sourceCreateTime;
    }

    public Date getTargetCreateTime() {
        return targetCreateTime;
    }
//    public Stuff(String content, StuffType type, SocketIOClient.Customer userCustomer) {
//        this.content = content;
//        this.type = type;
//        this.userCustomer = userCustomer;
//    }
//
//    private SocketIOClient.Customer userCustomer;
//    private String content;
//    private StuffType type;
//
//    public SocketIOClient.Customer getUserCustomer() {
//        return userCustomer;
//    }
//
//    public void setUserCustomer(SocketIOClient.Customer userCustomer) {
//        this.userCustomer = userCustomer;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public StuffType getType() {
//        return type;
//    }
//
//    public void setType(StuffType type) {
//        this.type = type;
//    }
}
