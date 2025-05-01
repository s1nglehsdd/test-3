package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.Product;
import com.example.shop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    @Override
    public Order createOrder(Order order) {
        if (order.getProduct() == null || order.getUser() == null) {
            System.err.println("Заказ должен содержать пользователя и продукт.");
            return null;
        }

        Optional<Product> productOptional = productService.getProductById(order.getProduct().getId());
        if (productOptional.isEmpty()) {
            System.err.println("Продукт с ID " + order.getProduct().getId() + " не найден.");
            return null;
        }

        Product product = productOptional.get();
        if (product.getQuantity() >= 0) {
            order.setOrderDate(LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);
            productService.save(product);

            return savedOrder;
        } else {
            System.out.println("Товар " + product.getName() + " отсутствует в наличии.");
            return null;
        }
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Order updateOrderStatus(Long id, String status) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        orderOptional.ifPresent(o -> {
            o.setStatus(status);
            o.setOrderDate(LocalDateTime.now());
            orderRepository.save(o);
        });
        return orderOptional.orElse(null);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getOrdersByUser(com.example.shop.model.User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    @Override
    public long getNewOrdersCount(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        java.time.LocalDateTime startOfDay = startDate.atStartOfDay();
        java.time.LocalDateTime endOfDay = endDate.atTime(java.time.LocalTime.MAX);
        return orderRepository.countByOrderDateBetween(startOfDay, endOfDay);
    }

    @Override
    public long getTotalCompletedOrderCount() {
        return orderRepository.countByStatus("Выполнен");
    }

    @Override
    public double getCompletedOrderPercentage() {
        long totalOrders = getTotalOrderCount();
        if (totalOrders == 0) {
            return 0;
        }
        return (double) getTotalCompletedOrderCount() / totalOrders * 100;
    }

    @Override
    public Map<String, Long> getOrderStatusCounts() {
        return orderRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(Order::getStatus, java.util.stream.Collectors.counting()));
    }
}