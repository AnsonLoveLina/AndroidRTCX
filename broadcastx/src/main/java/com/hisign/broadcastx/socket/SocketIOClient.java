package com.hisign.broadcastx.socket;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Queues;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.broadcastx.util.Constant;
import com.hisign.broadcastx.util.FastJsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jodd.util.StringUtil;

public class SocketIOClient {

    private static final Logger logger = Logger.getLogger(SocketIOClient.class.getName());

    private static final String TAG = "SocketIOClient";
    private final int socketFailRetrySum = 3;
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
            onConnection(new Emitter.Listener() {
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

    public Map<Customer, Map<String, String>> register(Collection<Customer> customers) {
        if (status.ordinal() > STATUS.REGISTER.ordinal()) {
            logger.info("status: " + status + ",register error,socketIO not connected!");
            return null;
        }
        Map<Customer, Map<String, String>> result = new HashMap<>();
        for (Customer customer : customers) {
            if (customer == null) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(customer.customerType.getCustomerType(), customer.customerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addCustomers(customer);
            Map<String, String> response = emit(EVENT_REGISTER, jsonObject, null);
            result.put(customer, response);
        }
        status = STATUS.REGISTER;
        return result;
    }

    public Map<String, String> unRegister(Customer customer) {
        if (!customers.contains(customer)) {
            logger.warning(customer + " not registered!");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(customer.customerType.getCustomerType(), customer.customerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        removeCustomers(customer);
        Map<String, String> response = emit(EVENT_UNREGISTER, jsonObject, null);
        if (customers.isEmpty()) {
            status = STATUS.UNREGISTER;
        }
        return response;
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
        emit(EVENT_BROADCASTINFO, jsonObject, new RetrySocketEmitFail(socketFailRetrySum));
    }

    /**
     * ThreadSafe
     */
    private class RetrySocketEmitFail implements ISocketEmitFail {
        private int retrySum;
        private AtomicInteger retryCount = new AtomicInteger(0);

        public RetrySocketEmitFail(int retrySum) {
            this.retrySum = retrySum;
        }

        @Override
        public void onEmitFail(String eventName, Object object, Map<String, String> response) {
            if (retryCount.compareAndSet(retrySum, retrySum)) {
                retryCount.getAndIncrement();
                emit(eventName, object, this);
            }
        }
    }

    private Map<String, String> emit(final String eventName, final Object object, final ISocketEmitFail iSocketEmitFail) {
        final BlockingQueue<Map<String, String>> ackQueue = Queues.newSynchronousQueue();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread.currentThread() = " + Thread.currentThread());
                System.out.println("eventName = " + eventName);
                System.out.println("object = " + object);
                socket.emit(eventName, object, new Ack() {
                    @Override
                    public void call(Object... args) {
                        System.out.println("call Thread.currentThread() = " + Thread.currentThread());
                        String response = args[0] == null ? "" : args[0].toString();
                        Map<String, String> responseMap = null;
                        try {
                            responseMap = FastJsonUtil.parseObject(args[0], Map.class);
                        } catch (Exception e) {
                            logger.fine(String.format("%s can not parse to %s", response, Stuff.class.toString()));
                        }
                        try {
                            ackQueue.put(responseMap);
                        } catch (InterruptedException e) {
                            logger.fine(e.getMessage());
                        }
                        if ("0".equals(responseMap.get("flag"))) {
                            if (iSocketEmitFail != null) {
                                iSocketEmitFail.onEmitFail(eventName, object, responseMap);
                            }
                        }
                    }
                });
            }
        });
        Map<String, String> responseMap = null;
        try {
            responseMap = ackQueue.poll(Constant.EMIT_TIMEOUT,Constant.EMIT_TIMEUNIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return responseMap;
    }

    public void onConnection(final Emitter.Listener listener) {
        connectedEmitter = socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("socketIO is connected!");
                status = STATUS.CONNECT;
                listener.call(args);
                socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        logger.warning("socketIO disconnect!");
                    }
                });
            }
        });
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public Customer getUserCustomer() {
        return userCustomer;
    }
}
