package com.hisign.broadcastx.socket;

import com.google.common.collect.Sets;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.util.FastJsonUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import io.socket.emitter.Emitter;
import jodd.util.StringUtil;

public class SocketIOClientUtil {

    private static final Logger logger = Logger.getLogger(SocketIOClientUtil.class.getName());

    private static SocketIOClient socketIOClient;

    public static SocketIOClient getInstance(){
        return socketIOClient;
    }

    private static SocketIOClient getInstance(String socketUrl) {
        if (socketIOClient == null && socketUrl != null) {
            socketIOClient = new SocketIOClient(socketUrl);
        }
        return socketIOClient;
    }

    /**
     * userid,groupids在连接成功后第一时间回调注册
     *
     * @param url
     * @param userid
     * @param groupids
     * @return
     */
    public static SocketIOClient socketConnect(String url, String userid, String[] groupids) {
        socketIOClient = SocketIOClientUtil.getInstance(url);
        if (socketIOClient == null) {
            return null;
        }
        final Set<SocketIOClient.Customer> customers = Sets.newHashSet();
        for (String groupid : groupids) {
            if (StringUtil.isNotBlank(groupid)) {
                customers.add(new SocketIOClient.Customer(CustomerType.GROUP, groupid));
            }
        }
        if (StringUtil.isNotBlank(userid)) {
            customers.add(new SocketIOClient.Customer(CustomerType.USER, userid));
        }
        socketIOClient.connection();
        socketIOClient.onConnection(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (customers.size() != 0) {
                    Map<SocketIOClient.Customer, Map<String, String>> resultMap = socketIOClient.register(customers);
                    for (Map.Entry<SocketIOClient.Customer, Map<String, String>> resultEntry : resultMap.entrySet()) {
                        if ("0".equals(resultEntry.getValue().get("flag"))) {
                            logger.fine(String.format("%s register failed\n%s", resultEntry.getKey().toString(), resultEntry.getValue()));
                        }
                    }
                }
            }
        });
        return socketIOClient;
    }

    public static Set<SocketIOClient.Customer> getGroups() {
        return socketIOClient.getCustomers();
    }

    public static SocketIOClient.Customer getUser() {
        return socketIOClient.getUserCustomer();
    }
}
