package com.example.shop.repository;

import com.example.shop.model.Order;
import com.example.shop.model.Product; // Импортируйте модель Product
import com.example.shop.model.User; // Импортируйте модель User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(String status);

    List<Order> findByStatus(String status);

    @Query("SELECT o.product, COUNT(o.product) FROM Order o GROUP BY o.product ORDER BY COUNT(o.product) DESC")
    List<Object[]> findMostPopularProductsWithNames(int limit);

    @Query("SELECT o.user, COUNT(o.user) FROM Order o GROUP BY o.user ORDER BY COUNT(o.user) DESC")
    List<Object[]> findMostActiveCustomersWithNames(int limit);
}