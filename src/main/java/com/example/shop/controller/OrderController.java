package com.example.shop.controller;

import com.example.shop.model.Order;
import com.example.shop.model.Product;
import com.example.shop.model.User;
import com.example.shop.repository.UserRepository;
import com.example.shop.service.OrderService;
import com.example.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Value("${admin.email}")
    private String adminEmail;

    @Autowired
    public OrderController(OrderService orderService, ProductService productService, UserRepository userRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @Transactional
    @PostMapping("/order/{productId}")
    public String orderSubmit(
            @PathVariable Long productId,
            @ModelAttribute Order order,
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<User> userOptional = userRepository.findByEmail(principal.getName());
        Optional<Product> productOptional = productService.getProductById(productId);
        if (userOptional.isPresent() && productOptional.isPresent()) {
            User user = userOptional.get();
            Product product = productOptional.get();
            double orderTotal = product.getPrice();

            logger.info("User found: {}", user.getEmail());
            logger.info("Product found: {}, price: {}, quantity: {}", product.getName(), orderTotal, product.getQuantity());
            if (product.getQuantity() <= 0) {
                logger.warn("Product {} is out of stock.", product.getName());
                model.addAttribute("errorMessage", "Товар \"" + product.getName() + "\" отсутствует в наличии.");
                return "out-of-stock";
            }
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                logger.error("User with ID {} has no roles", user.getId());
                model.addAttribute("errorMessage", "Ошибка: у пользователя не назначены роли.");
                return "error";
            }
            Double currentBalance = null;
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                currentBalance = user.getRoles().iterator().next().getBalance();
            }
            if (currentBalance != null && currentBalance >= orderTotal) {
                try {
                    user.getRoles().iterator().next().setBalance(currentBalance - orderTotal);
                    userRepository.save(user);
                    logger.info("User balance updated. New balance: {}", currentBalance - orderTotal);
                    userRepository.findByEmail(adminEmail).ifPresent(admin -> {
                        if (admin.getRoles() != null && !admin.getRoles().isEmpty()) {
                            Double adminBalance = admin.getRoles().iterator().next().getBalance();
                            admin.getRoles().iterator().next().setBalance(adminBalance + orderTotal);
                            userRepository.save(admin);
                            logger.info("Admin balance updated. New balance: {}", adminBalance + orderTotal);
                        } else {
                            logger.warn("Admin user {} has no roles to update balance.", admin.getEmail());
                        }
                    });
                    product.setQuantity(product.getQuantity() - 1);
                    productService.save(product);
                    logger.info("Product quantity updated. New quantity: {}", product.getQuantity());

                } catch (Exception e) {
                    logger.error("Ошибка при обработке заказа:", e);
                    model.addAttribute("errorMessage", "Произошла ошибка при обработке заказа.");
                    return "error";
                }
                order.setUser(user);
                order.setProduct(product);
                order.setStatus("Новый");
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(orderTotal);
                logger.info("Order total amount: {}", order.getTotalAmount());
                orderService.createOrder(order);
                model.addAttribute("orderNumber", order.getId());
                model.addAttribute("product", product);
                return "order-success";
            } else {
                logger.error("Insufficient funds for user: {}", user.getEmail());
                model.addAttribute("productPrice", orderTotal);
                model.addAttribute("currentBalance", currentBalance != null ? currentBalance : 0.0);
                model.addAttribute("productId", productId);
                return "insufficient-funds";
            }
        } else {
            logger.error("User or product not found.");
            return "redirect:/error";
        }
    }
}