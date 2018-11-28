package com.hisign.broadcastx.socket;

import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.util.FastJsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jodd.util.StringUtil;

public class SocketIOClient {

    private static final Logger logger = Logger.getLogger(SocketIOClient.class.getName());

    private static final String TAG = "SocketIOClient";
    private String url;
    private Set<Customer> customers = new HashSet<>();
    private Customer userCustomer = new Customer();
    private Socket socket;
    public static final String EVENT_REGISTER = "register";
    public static final String EVENT_UNREGISTER = "unRegister";
    public static final String EVENT_BROADCASTINFO = "broadcastInfo";
    public static final String EVENT_INFO = "info";
    private Emitter connectedEmitter;

    enum STATUS {
        CONNECT, REGISTER, UNREGISTER, CLOSE
    }

    private STATUS status = STATUS.CLOSE;

    public static class Customer {
        private CustomerType customerType;
        private String customerId;

        public Customer() {
        }

        public Customer(CustomerType customerType, String customerId) {
            if (customerType != null || StringUtil.isBlank(customerId)) {
                logger.info("customerType or customerId is blank!");
            }
            this.customerType = customerType;
            this.customerId = customerId;
        }

        public CustomerType getCustomerType() {
            return customerType;
        }

        public void setCustomerType(CustomerType customerType) {
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
            if ("user".equals(customer.getCustomerType().getCustomerType())) {
                userCustomer = customer;
            }
        }
    }

    private void removeCustomers(Customer... customers) {
        for (Customer customer : customers) {
            this.customers.remove(customer);
            if ("user".equals(customer.getCustomerType().getCustomerType())) {
                userCustomer = new Customer();
            }
        }
    }

    public SocketIOClient(String url, CustomerType customerType, String customerId) {
        this.url = url;
        if (customerType != null && StringUtil.isNotBlank(customerId)) {
            Customer customer = new Customer(customerType, customerId);
            addCustomers(customer);
        }
    }

    public void connection() {
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            socket = IO.socket(url, opts);
            logger.info("send socketIO connection!");
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

    public Emitter onListener(String event, Emitter.Listener listener) {
        //假如还没有人注册，则警告继续
        if (status != STATUS.REGISTER) {
            logger.warning("no one registered!");
        }
        return connectedEmitter.on(event, listener);
    }

    public void register(Collection<Customer> customers) {
        if (status.ordinal() > STATUS.REGISTER.ordinal()) {
            logger.info("status: " + status + ",register error,socketIO not connected!");
            return;
        }
        for (Customer customer : customers) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(customer.customerType.getCustomerType(), customer.customerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addCustomers(customer);
            socket.emit(EVENT_REGISTER, jsonObject);
        }
        status = STATUS.REGISTER;
    }

    public void unRegister(Customer customer) {
        if (!customers.contains(customer)) {
            logger.warning(customer + " not registered!");
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
        if (status.ordinal() > STATUS.REGISTER.ordinal()) {
            logger.info("status: " + status + ",send error,socketIO not connected!");
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
        logger.info("socketIO is connected!");
        status = STATUS.CONNECT;
        connectedEmitter = socket.on(Socket.EVENT_CONNECT, listener);
    }

    public Customer getUserCustomer() {
        return userCustomer;
    }
}