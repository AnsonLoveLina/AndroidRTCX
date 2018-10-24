package com.hisign.androidrtcx.internet;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jodd.util.StringUtil;

public class SocketIOClient {
    private static final String TAG = "SocketIOClient";
    private String url;
    private Set<Customer> customers = new HashSet<>();
    private Customer userCustomer = new Customer();
    private Socket socket;
    public static final String EVENT_REGISTER = "register";
    public static final String EVENT_UNREGISTER = "unRegister";
    public static final String EVENT_BROADCASTINFO = "broadcastInfo";
    public static final String EVENT_INFO = "info";

    enum STATUS {
        READY, CONNECT, REGISTER, UNREGISTER, CLOSE
    }

    private STATUS status = STATUS.CLOSE;

    public static class Customer {
        private String customerType;
        private String customerId;

        public Customer() {
        }

        public Customer(String customerType, String customerId) {
            if (StringUtil.isBlank(customerType) || StringUtil.isBlank(customerId)) {
                Log.e(TAG, "customerType or customerId is blank!");
            }
            this.customerType = customerType;
            this.customerId = customerId;
        }

        public String getCustomerType() {
            return customerType;
        }

        public void setCustomerType(String customerType) {
            this.customerType = customerType;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }
    }

    public SocketIOClient(String url) {
        this(url, null, null);
    }

    private void addCustomers(Customer... customers) {
        for (Customer customer : customers) {
            this.customers.add(customer);
            if ("user".equals(customer.getCustomerType())) {
                userCustomer = customer;
            }
        }
    }

    private void removeCustomers(Customer... customers) {
        for (Customer customer : customers) {
            this.customers.remove(customer);
            if ("user".equals(customer.getCustomerType())) {
                userCustomer = new Customer();
            }
        }
    }

    public SocketIOClient(String url, String customerType, String customerId) {
        this.url = url;
        if (StringUtil.isNotBlank(customerType) && StringUtil.isNotBlank(customerId)) {
            Customer customer = new Customer(customerType, customerId);
            addCustomers(customer);
        }
        status = STATUS.READY;
    }

    public void connection() {
        try {
            socket = IO.socket(url);
            Log.i(TAG, "send socketIO connection!");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (!customers.isEmpty()) {
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    register(customers);
                }
            });
        }
        socket.connect();
    }

    public void onListener(String event, Emitter.Listener listener) {
        //假如还没有人注册，则警告继续
        if (status != STATUS.REGISTER) {
            Log.w(TAG, "no one registered!");
        }
        socket.on(event, listener);
    }

    public void register(Collection<Customer> customers) {
        //假如链接尚未成功则报错返回
        if (status != STATUS.CONNECT) {
            Log.e(TAG, "socketIO not connected!");
            return;
        }
        for (Customer customer : customers) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(customer.customerType, customer.customerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addCustomers(customer);
            socket.emit(EVENT_REGISTER, jsonObject);
        }
        status = STATUS.REGISTER;
    }

    public void unRegister(Customer customer) {
        //假如注销人员压根没注册，则警告返回
        if (!customers.contains(customer)) {
            Log.w(TAG, customer + " not registered!");
            return;
        }
        removeCustomers(customer);
        socket.emit(EVENT_UNREGISTER, customer);
        if (customers.isEmpty()) {
            status = STATUS.UNREGISTER;
        }
    }

    public void disconnect() {
        socket.disconnect();
        status = STATUS.CLOSE;
    }

    public void send(String event, String customer, Object object) {
        if (status != STATUS.CONNECT && status != STATUS.REGISTER) {
            Log.e(TAG, "socketIO not connected!");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("roomName", customer);
            jsonObject.put("eventName", event);
            jsonObject.put("text", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit(EVENT_BROADCASTINFO, jsonObject);
    }

    public void onConnection(Emitter.Listener listener) {
        Log.i(TAG, "socketIO is connected!");
        status = STATUS.CONNECT;
        socket.on(Socket.EVENT_CONNECT, listener);
    }

    public Customer getUserCustomer() {
        return userCustomer;
    }
}
