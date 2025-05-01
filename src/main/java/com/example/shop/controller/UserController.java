package com.example.shop.controller;

import com.example.shop.model.Order;
import com.example.shop.model.User;
import com.example.shop.service.OrderService;
import com.example.shop.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/profile/{userId}/orders")
    public String orderHistory(@PathVariable Long userId, Model model, Principal principal) {
        logger.info("Retrieving order history for user ID: {}", userId);

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found"));

        try {
            List<Order> orders = orderService.getOrdersByUser(user);
            model.addAttribute("orders", orders);
            model.addAttribute("viewedUserId", userId);
            logger.info("Successfully retrieved order history for user ID: {}", userId);
            return "order_history";
        } catch (Exception e) {
            logger.error("Failed to retrieve order history for user ID: {}", userId, e);
            model.addAttribute("errorMessage", "Failed to retrieve order history. Please try again later.");
            return "order_history";
        }
    }
}