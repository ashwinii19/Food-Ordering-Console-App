package com.techlabs.foodzone.model;

import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private String paymentMethod;
    private String orderStatus;
    private Timestamp orderDate;

    public Order(int orderId, int userId, String paymentMethod, String orderStatus, Timestamp orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }
}
