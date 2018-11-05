package com.hisign.broadcastx;

public enum CustomerType {
    USER("user"),GROUP("group");
    private String customerType;

    CustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
}
