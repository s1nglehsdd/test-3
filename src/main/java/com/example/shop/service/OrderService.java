package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {

    Order createOrder(Order order);

    Optional<Order> getOrderById(Long id);

    List<Order> getAllOrders();

    List<Order> getOrdersByUserId(Long userId);

    Order updateOrderStatus(Long id, String status);

    void deleteOrder(Long id);

    List<Order> getOrdersByUser(User user);

    long getTotalOrderCount();

    long getNewOrdersCount(LocalDate startDate, LocalDate endDate);

    long getTotalCompletedOrderCount();

    double getCompletedOrderPercentage();

    Map<String, Long> getOrderStatusCounts();
}