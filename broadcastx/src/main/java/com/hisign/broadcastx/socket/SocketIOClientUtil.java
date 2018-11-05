package com.hisign.broadcastx.socket;

import java.util.HashSet;
import java.util.Set;

public class SocketIOClientUtil {

    private static SocketIOClient socketIOClient;

    private static SocketIOClient.Customer user;

    private static Set<SocketIOClient.Customer> groups = new HashSet<>();

    public static SocketIOClient getInstance(String socketUrl) {
        if (socketIOClient == null || socketUrl != null) {
            socketIOClient = new SocketIOClient(socketUrl);
        }
        return socketIOClient;
    }

    public static Set<SocketIOClient.Customer> getGroups() {
        return groups;
    }

    public static void addGroup(SocketIOClient.Customer group) {
        groups.add(group);
    }

    public static SocketIOClient.Customer getUser() {
        return user;
    }

    public static void setUser(SocketIOClient.Customer user) {
        SocketIOClientUtil.user = user;
    }
}
